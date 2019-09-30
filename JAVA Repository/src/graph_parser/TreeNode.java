/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author MSI
 */
public class TreeNode {

    public static int treeNodeIdCounter = 0;

    private int cohesionFactor;
    private int kMax;
    private TreeNode parent;
    private int treeNodeId;

    //computed first, updated later from child
    Set<Integer> vertexSet = new LinkedHashSet<>();

    Set<Integer> fullVertexSet = new LinkedHashSet<>();

    ArrayList<TreeNode> childNodes = new ArrayList<>();

    Map<Integer, iListElement> iList = new HashMap<>();

    public TreeNode() {
        //constructor for root node
        this.cohesionFactor = 0;
        this.parent = null;
        this.treeNodeId = TreeNode.treeNodeIdCounter++;

        for (int i = Constants.MIN_AUTH_ID; i <= Constants.MAX_AUTH_ID; i++) {
            this.vertexSet.add(i);
        }
    }

    public TreeNode(int nodeId) {
        this.treeNodeId = nodeId;
        this.parent = null;
    }

    public TreeNode(TreeNode parent) {
        //constructor for other nodes
        this.parent = parent;
    }

    public void attach() {
        //this.attachChildNodes();
        //this.compressVertices(this);
        this.attachAndCompressChildNodes();
    }

    public int getkMax() {
        return kMax;
    }

    public void setkMax(int kMax) {
        this.kMax = kMax;
    }

    public void compressVertices(TreeNode parent) {
        /**
         * Assign maxOutScore for each keyword: only requires the scores of
         * vertices in child nodes
         */
        for (int keyword : parent.iList.keySet()) {
            double maxOutScore = 0.0;
            for (TreeNode child : parent.childNodes) {
                double outScore = 0.0;
                if (child.iList.containsKey(keyword)) {
                    for (int vid : child.iList.get(keyword).getRelVertices()) {
                        outScore += Main.authors[vid].getKeywordScore(keyword);
                    }
                }

                if (outScore > maxOutScore) {
                    maxOutScore = outScore;
                }
            }

            parent.iList.get(keyword).setMaxOutScore(maxOutScore);
        }

        /**
         * First, compress node vertices, then relVertices in iList if
         * relVertices is empty, maxInScore will be zero
         */
        for (TreeNode child : parent.childNodes) {
            //compress node vertices
            parent.vertexSet.removeAll(child.vertexSet);

            //compress inverted list
            for (int keyword : parent.iList.keySet()) {
                Set<Integer> relVertices = parent.iList.get(keyword).getRelVertices();
                relVertices.removeAll(child.vertexSet);
                if (relVertices.size() == 0) {
                    //maxInScore is set to 0
                    parent.iList.get(keyword).setMaxInScore(0.0);
                }
            }

            compressVertices(child);
        }
    }

    public void attachChildNodes() {
        Set<Integer> vSet = CLTree.findMaxCore(this.vertexSet, this.cohesionFactor + 1);
        if (vSet.size() != 0) {
            for (Set<Integer> componentNodes : CLTree.findConnectedComponents(vSet)) {
                TreeNode tnode = new TreeNode(this);
                tnode.setCohesionFactor(tnode.getParent().getCohesionFactor() + 1);
                tnode.vertexSet = componentNodes;
                tnode.setkMax(tnode.getCohesionFactor());
                /**
                 * Form iList
                 */
                Map<Integer, iListElement> tempIList = new HashMap<>();
                for (int v : tnode.vertexSet) {
                    for (int keyword : Main.authors[v].getKeywordCounts().keySet()) {
                        if (Main.authors[v].getKeywordCitationCount(keyword) == 0) {
                            continue;
                        }
                        if (tempIList.containsKey(keyword)) {
                            tempIList.get(keyword).addRelVertex(v);
                        } else {
                            iListElement newElement = new iListElement();
                            newElement.addRelVertex(v);
                            tempIList.put(keyword, newElement);
                        }
                    }
                }

                /**
                 * Assign maxInScore, zero values are updated later
                 */
                for (int keyword : tempIList.keySet()) {
                    iListElement tempEl = tempIList.get(keyword);
                    double maxInScore = 0.0;
                    for (int vid : tempEl.getRelVertices()) {
                        maxInScore += Main.authors[vid].getKeywordScore(keyword);
                    }
                    tempEl.setMaxInScore(maxInScore);
                }

                tnode.treeNodeId = TreeNode.treeNodeIdCounter++;
                tnode.iList = tempIList;
                tnode.attachChildNodes();

                this.childNodes.add(tnode);
                if (this.kMax < tnode.kMax) {
                    this.kMax = tnode.kMax;
                }
            }
        }
    }

    public void attachAndCompressChildNodes() {
        /**
         * The TreeNode represents a connected k-core We first find (k+1)-core
         * and all of its connected components. These are the child nodes
         */
        Set<Integer> vSet = CLTree.findMaxCore(this.vertexSet, this.cohesionFactor + 1);
        if (vSet.size() != 0) {
            for (Set<Integer> componentNodes : CLTree.findConnectedComponents(vSet)) {
                TreeNode tnode = new TreeNode(this);
                tnode.setCohesionFactor(tnode.getParent().getCohesionFactor() + 1);
                tnode.vertexSet = componentNodes;
                tnode.setkMax(tnode.getCohesionFactor());   //initially, k_max = cohesion factor of this node

                /**
                 * Form iList
                 */
                Map<Integer, iListElement> tempIList = new HashMap<>();
                for (int v : tnode.vertexSet) {
                    for (int keyword : Main.authors[v].getKeywordCounts().keySet()) {
                        if (Main.authors[v].getKeywordCitationCount(keyword) == 0) {
                            continue;
                        }
                        if (tempIList.containsKey(keyword)) {
                            tempIList.get(keyword).addRelVertex(v);
                        } else {
                            iListElement newElement = new iListElement();
                            newElement.addRelVertex(v);
                            tempIList.put(keyword, newElement);
                        }
                    }
                }

                /**
                 * Assign maxInScore, zero values are updated later
                 */
                for (int keyword : tempIList.keySet()) {
                    iListElement tempEl = tempIList.get(keyword);
                    double maxInScore = 0.0;
                    for (int vid : tempEl.getRelVertices()) {
                        maxInScore += Main.authors[vid].getKeywordScore(keyword);
                    }
                    tempEl.setMaxInScore(maxInScore);
                }

                //Node is formed. Needs compression and MaxOutScore calculation later
                tnode.treeNodeId = TreeNode.treeNodeIdCounter++;
                tnode.iList = tempIList;

                this.childNodes.add(tnode);
            }

            /**
             * Parent is now linked with its children
             * Now, compress parent node. Before that, update MaxOutScore
             * After compression, update MaxInScore
             */
            
            for (int keyword : this.iList.keySet()) {
                double maxOutScore = 0.0;
                for (TreeNode child : this.childNodes) {
                    double outScore = 0.0;
                    if (child.iList.containsKey(keyword)) {
                        for (int vid : child.iList.get(keyword).getRelVertices()) {
                            outScore += Main.authors[vid].getKeywordScore(keyword);
                        }
                    }

                    if (outScore > maxOutScore) {
                        maxOutScore = outScore;
                    }
                }

                this.iList.get(keyword).setMaxOutScore(maxOutScore);
            }

            /**
             * Compress Parent Node
             * Update MaxInScore to 0 if relVertices is empty after compression
             */
            for (TreeNode child : this.childNodes) {
                //compress node vertices
                this.vertexSet.removeAll(child.vertexSet);

                //compress inverted list
                for (int keyword : this.iList.keySet()) {
                    Set<Integer> relVertices = this.iList.get(keyword).getRelVertices();
                    relVertices.removeAll(child.vertexSet);
                    if (relVertices.size() == 0) {
                        //maxInScore is set to 0
                        this.iList.get(keyword).setMaxInScore(0.0);
                    }
                }
            }
            
            for(TreeNode child: this.childNodes)
            {
                child.attachAndCompressChildNodes();
                if (this.kMax < child.kMax) {
                    this.kMax = child.kMax;
                }
            }
        }

    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public int getCohesionFactor() {
        return cohesionFactor;
    }

    public void setCohesionFactor(int cohesionFactor) {
        this.cohesionFactor = cohesionFactor;
    }

    @Override
    public String toString() {
        String str = "ID: " + this.treeNodeId + "\n";
        str += "k: " + this.cohesionFactor + ", k_max: " + this.kMax + ", vertices: " + this.vertexSet + "\n";
        str += "keywords: " + this.iList.keySet() + "\n----\n";
        for (int keyword : this.iList.keySet()) {
            str += keyword + "->" + this.iList.get(keyword).getRelVertices() + "\n";
            str += "MaxInScore: " + this.iList.get(keyword).getMaxInScore() + "\n";
            str += "MaxOutScore: " + this.iList.get(keyword).getMaxOutScore() + "\n";
        }
        str += "-------\n";
        return str; //To change body of generated methods, choose Tools | Templates.
    }

    public JSONObject toJSON() {
        JSONObject jo = new JSONObject();
        jo.put("node-id", this.treeNodeId);
        jo.put("cohesion-factor", this.cohesionFactor);
        //Requires attention
        jo.put("kmax", this.kMax);
        jo.put("vertices", this.vertexSet);

        JSONArray ja = new JSONArray();

        for (int keywordId : iList.keySet()) {
            Map m = new LinkedHashMap(4);
            m.put("keyword-id", keywordId);
            m.put("rel-vertices", iList.get(keywordId).getRelVertices());
            m.put("max-in-score", iList.get(keywordId).getMaxInScore());
            m.put("max-out-score", iList.get(keywordId).getMaxOutScore());
            ja.add(m);
        }
        jo.put("keywords", ja);

        return jo;
    }

    public int getTreeNodeId() {
        return treeNodeId;
    }

    public void setTreeNodeId(int treeNodeId) {
        this.treeNodeId = treeNodeId;
    }

}
