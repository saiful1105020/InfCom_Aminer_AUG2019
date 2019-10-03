/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    public TreeNode(String dummy, int nodeId) {
        this.treeNodeId = nodeId;
    }

    public TreeNode()
    {
        this.cohesionFactor = 0;
        this.parent = null;
    }
    
    public TreeNode(String root) {
        //constructor for root node
        //this.cohesionFactor = Constants.K_MIN-1;
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
    
    public void preempt()
    {
        //this.childNodes stays the same
        this.fullVertexSet.clear();
        this.iList.clear();
        //this.parent stays the same
        this.vertexSet.clear();
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
                //System.out.println("Component Size: " + componentNodes.size());
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
                //System.out.println(tnode.treeNodeId);
                this.childNodes.add(tnode);
            }

            /**
             * Parent is now linked with its children Now, compress parent node.
             * Before that, update MaxOutScore After compression, update
             * MaxInScore
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
             * Compress Parent Node Update MaxInScore to 0 if relVertices is
             * empty after compression
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

            //save all the children
            for (TreeNode child : this.childNodes) {
                this.saveTreeNodeToFile(child);
            }

            //save parent node
            //this.saveTreeNodeToFile(this);
            //free-up memory
            //this.preempt();
            for (TreeNode child : this.childNodes) {
                //load child node
                loadTreeNodeFromFile(child);

                child.attachAndCompressChildNodes();
                if (this.kMax < child.kMax) {
                    this.kMax = child.kMax;
                }
            }
            //reload parent
            //this.loadTreeNodeFromFile(this);
        }
    }

    public void saveTreeNodeToFile(TreeNode child) {
        try {
            //System.out.println("Saving node in file: " + child.treeNodeId);
            FileWriter fw = new FileWriter(new File(child.getTreeNodeId() + ".txt"));
            fw.write(child.toJSON().toJSONString());
            fw.flush();
            fw.close();
            child = new TreeNode("DUMMY_NODE", child.treeNodeId);
        } catch (IOException ex) {
            Logger.getLogger(TreeNode.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Severe Error While Saving CL Tree Node");
            System.exit(0);
        }

    }

    public void loadTreeNodeFromFile(TreeNode child) {
        Scanner input;
        try {
            input = new Scanner(new File(child.treeNodeId + ".txt"));
            Object obj = new JSONParser().parse(input.nextLine());
            JSONObject jo = (JSONObject) obj;
            int tNodeId = (int) (long) jo.get("node-id");

            int cohesionFactor = (int) (long) jo.get("cohesion-factor");
            child.setCohesionFactor(cohesionFactor);
            int kMax = (int) (long) jo.get("kmax");
            child.setkMax(kMax);

            JSONArray vertexSet = (JSONArray) jo.get("vertices");
            for (int j = 0; j < vertexSet.size(); j++) {
                int v = (int) (long) vertexSet.get(j);
                child.vertexSet.add(v);
            }

            JSONArray jKeywords = (JSONArray) jo.get("keywords");
            for (int j = 0; j < jKeywords.size(); j++) {
                JSONObject jKeyword = (JSONObject) jKeywords.get(j);
                int keywordId = (int) ((long) jKeyword.get("keyword-id"));

                iListElement newElement = new iListElement();
                JSONArray relVerts = (JSONArray) jKeyword.get("rel-vertices");
                for (int k = 0; k < relVerts.size(); k++) {
                    int vert = (int) (long) relVerts.get(k);
                    newElement.addRelVertex(vert);
                }

                double maxInScore = (double) jKeyword.get("max-in-score");
                newElement.setMaxInScore(maxInScore);
                double maxOutScore = (double) jKeyword.get("max-out-score");
                newElement.setMaxOutScore(maxOutScore);

                child.iList.put(keywordId, newElement);
            }

            input.close();

            File file = new File(child.treeNodeId + ".txt");
            boolean success = file.delete();
            if (!success) {
                System.err.println("Error deleting file: " + child.treeNodeId);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TreeNode.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("SEVERE ERROR READING CL TREE NODE");
        } catch (ParseException ex) {
            Logger.getLogger(TreeNode.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("SEVERE ERROR PARSING CL TREE NODE");
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
