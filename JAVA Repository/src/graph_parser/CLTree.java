/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import static graph_parser.GlobalInvertedList.IL;
import static graph_parser.GlobalInvertedList.fileName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
public class CLTree {

    public static TreeNode root;
    public static int vertexDegree[] = new int[Constants.MAX_AUTH_ID + 1];
    public static int maxCohesionFactor = 0;

    public static Set<Integer>[] invertedList = new LinkedHashSet[Constants.NUM_KEYWORDS + 1];
    public static String invertedListFileName = "CLTree-InvertedList.txt";

    public static void buildTree() {
        if (Constants.COMPUTE_CL_TREE) {
            long startTime, endTime, totalTime;
            int runs = Constants.RUNS;

            startTime = System.nanoTime();
            coreDecomposition();
            root = new TreeNode();
            root.attach();
            endTime = System.nanoTime();
            totalTime = (endTime - startTime) / (1000000);
            System.out.println("CL-tree with iList: " + totalTime + " ms");
            
            try {
                writeSubtree("CL_TREE.txt", "CL-TREE-EDGES.txt", root);
            } catch (IOException ex) {
                Logger.getLogger(CLTree.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            readTreeFromFile();

        }

        //traverseTree(root);
    }

    public static void updateInvertedList(TreeNode u) {
        int uid = u.getTreeNodeId();
        for (int keyword : u.iList.keySet()) {
            invertedList[keyword].add(uid);
        }
        for (TreeNode v : u.childNodes) {
            updateInvertedList(v);
        }
    }

    public static void storeIntoFile() {
        try {
            FileWriter fw = new FileWriter(new File(invertedListFileName));
            for (int i = 1; i <= Constants.NUM_KEYWORDS; i++) {
                JSONObject jo = new JSONObject();
                jo.put("keyword", i);
                jo.put("total-nodes", invertedList[i].size());
                jo.put("nodes", invertedList[i]);
                fw.write(jo.toJSONString());
                fw.write("\n");
                fw.flush();
            }
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(GlobalInvertedList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void loadInvertedList() {
        if (Constants.COMPUTE_CL_TREE) {
            //compute inverted list
            //initiate
            for (int i = 0; i <= Constants.NUM_KEYWORDS; i++) {
                invertedList[i] = new LinkedHashSet<>();
            }
            updateInvertedList(root);
            //write to file
            storeIntoFile();
        } else {
            //read from file
            try {
                Scanner input = new Scanner(new File(invertedListFileName));
                while (input.hasNext()) {
                    Object obj = new JSONParser().parse(input.nextLine());
                    JSONObject jo = (JSONObject) obj;

                    int keywordId = (int) (long) jo.get("keyword");
                    JSONArray jNodes = (JSONArray) jo.get("nodes");

                    ArrayList<Integer> keywordNodes = new ArrayList<Integer>();
                    for (Object item : jNodes) {
                        keywordNodes.add((int) (long) item);
                    }
                    invertedList[keywordId] = new LinkedHashSet<Integer>(keywordNodes);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GlobalInvertedList.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(GlobalInvertedList.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public static void readTreeFromFile() {
        try {
            /**
             * First, read Nodes
             */
            Scanner input = new Scanner(new File("CL_TREE.txt"));
            int totalTreeNodes = Integer.parseInt(input.nextLine());
            TreeNode treeNodes[] = new TreeNode[totalTreeNodes];

            for (int i = 0; i < totalTreeNodes; i++) {
                Object obj = new JSONParser().parse(input.nextLine());
                JSONObject jo = (JSONObject) obj;
                int tNodeId = (int) (long) jo.get("node-id");
                treeNodes[tNodeId] = new TreeNode(tNodeId);

                int cohesionFactor = (int) (long) jo.get("cohesion-factor");
                treeNodes[tNodeId].setCohesionFactor(cohesionFactor);
                int kMax = (int) (long) jo.get("kmax");
                treeNodes[tNodeId].setkMax(kMax);

                JSONArray vertexSet = (JSONArray) jo.get("vertices");
                for (int j = 0; j < vertexSet.size(); j++) {
                    int v = (int) (long) vertexSet.get(j);
                    treeNodes[tNodeId].vertexSet.add(v);
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

                    treeNodes[tNodeId].iList.put(keywordId, newElement);
                }
            }

            input.close();

            root = treeNodes[0];

            /**
             * Link edges
             */
            input = new Scanner(new File("CL-TREE-EDGES.txt"));
            while (input.hasNext()) {
                String str[] = input.nextLine().split("\t");

                int parentNodeId = Integer.parseInt(str[0]);
                int childNodeId = Integer.parseInt(str[1]);

                treeNodes[childNodeId].setParent(treeNodes[parentNodeId]);
                treeNodes[parentNodeId].childNodes.add(treeNodes[childNodeId]);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(CLTree.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CLTree.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writeSubtree(String NODE_FILENAME, String EDGE_FILENAME, TreeNode root) throws IOException {
        FileWriter NODE_FW = new FileWriter(NODE_FILENAME);
        FileWriter EDGE_FW = new FileWriter(EDGE_FILENAME);

        NODE_FW.write(Integer.toString(TreeNode.treeNodeIdCounter));
        NODE_FW.write("\n");
        
        recursiveWrite(NODE_FW, EDGE_FW, root);
        
        NODE_FW.flush();
        EDGE_FW.flush();
        
        NODE_FW.close();
        EDGE_FW.close();
    }

    public static void recursiveWrite(FileWriter NODE_FW, FileWriter EDGE_FW, TreeNode tnode) throws IOException {
        NODE_FW.write(tnode.toJSON().toJSONString());
        NODE_FW.write("\n");
        //NODE_FW.flush();

        for (TreeNode child : tnode.childNodes) {
            EDGE_FW.write(tnode.getTreeNodeId() + "\t" + child.getTreeNodeId() + "\n");
            //EDGE_FW.flush();
            recursiveWrite(NODE_FW, EDGE_FW, child);
        }
    }

    public static void traverseTree(TreeNode tnode) {
        System.out.println(tnode);

        for (TreeNode child : tnode.childNodes) {
            traverseTree(child);
        }
    }

    public static void printSubgraph(Set<Integer> vertices) {
        for (int nodeId : vertices) {
            String nodeStr = "[" + nodeId + "]->";

            Set<Integer> temp = new HashSet<Integer>(Main.authors[nodeId].getCoAuthorPaperCounts().keySet());
            temp.retainAll(vertices);
            nodeStr += temp;
            System.out.println(nodeStr);
        }
    }

    public static ArrayList<Set> findConnectedComponents(Set<Integer> vSet) {
        ArrayList<Set> components = new ArrayList<>();
        HashMap<Integer, Boolean> visited = new HashMap<>();

        for (int nodeId : vSet) {
            visited.put(nodeId, false);
        }

        for (int nodeId : vSet) {
            if (!visited.get(nodeId)) {
                Set<Integer> C = new LinkedHashSet<>();
                bfsVisit(C, nodeId, vSet, visited);
                components.add(C);
            }
        }

        return components;
    }

    public static void dfsVisit(Set<Integer> connectedComponent, int nodeId, Set<Integer> vSet, HashMap<Integer, Boolean> visited) {
        visited.put(nodeId, true);
        connectedComponent.add(nodeId);

        Set<Integer> adj = new LinkedHashSet(Main.authors[nodeId].getCoAuthorPaperCounts().keySet());
        adj.retainAll(vSet);

        for (int v : adj) {
            if (!visited.get(v)) {
                dfsVisit(connectedComponent, v, vSet, visited);
            }
        }

    }

    public static void bfsVisit(Set<Integer> connectedComponent, int nodeId, Set<Integer> vSet, HashMap<Integer, Boolean> visited) {
        LinkedList<Integer> queue = new LinkedList<Integer>();
        visited.put(nodeId, true);
        queue.add(nodeId);

        while (queue.size() != 0) {
            nodeId = queue.poll();
            connectedComponent.add(nodeId);

            Set<Integer> adj = new LinkedHashSet(Main.authors[nodeId].getCoAuthorPaperCounts().keySet());
            adj.retainAll(vSet);

            for (int v : adj) {
                if (visited.get(v) == false) {
                    visited.put(v, true);
                    queue.add(v);
                }
            }
        }
    }

    public static void coreDecomposition() {
        /*
        Also remove zero-degree nodes
         */
        int dMax = Main.maxDegree;

        //count[i]: how many vertices of degree i
        int count[] = new int[dMax + 1];

        //temp variable for assigning index of D table
        int currPos[] = new int[dMax + 1];

        //b[i]: where is (in D) the first vertex with degree i
        int b[] = new int[dMax + 1];

        //System.out.println("Size of V: "+V.size());
        //init
        for (int i = 0; i <= dMax; i++) {
            count[i] = 0;
            currPos[i] = 0;
        }

        for (int nodeId = Constants.MIN_AUTH_ID; nodeId <= Constants.MAX_AUTH_ID; nodeId++) {
            int deg = Main.authors[nodeId].getCoAuthorPaperCounts().keySet().size();
            count[deg]++;
            vertexDegree[nodeId] = deg;
        }

        //System.out.println("Size of V after removal: "+V.size());
        b[0] = 0;
        currPos[0] = 0;
        b[1] = 0;
        currPos[1] = 0;
        for (int i = 2; i <= dMax; i++) {
            b[i] = b[i - 1] + count[i - 1];
            currPos[i] = b[i];
            //System.out.println("Deg "+i+" starts from "+b[i]);
        }

        //List of nodes sorted by degree
        int D[] = new int[Constants.MAX_AUTH_ID + 1];
        //p[i]: where is node i in D? 
        int p[] = new int[Constants.MAX_AUTH_ID + 1];

        for (int nodeId = Constants.MIN_AUTH_ID; nodeId <= Constants.MAX_AUTH_ID; nodeId++) {
            int deg = vertexDegree[nodeId];
            if (deg == 0) {
                continue;
            }
            D[currPos[deg]] = nodeId;
            p[nodeId] = currPos[deg];
            currPos[deg]++;
        }

        for (int i = 0; i < currPos[dMax]; i++) {
            int vId = D[i];

            for (int uId : Main.authors[vId].getCoAuthorPaperCounts().keySet()) {
                int du = vertexDegree[uId];
                int dv = vertexDegree[vId];
                if (du > dv) {
                    int pu = p[uId];
                    int pw = b[du];
                    int w = D[pw];

                    if (uId != w) {
                        D[pu] = w;
                        D[pw] = uId;
                        p[uId] = pw;
                        p[w] = pu;
                    }
                    b[du]++;
                    du--;
                    vertexDegree[uId] = du;
                }

            }
        }

        /*
        for(int i=Constants.MIN_AUTH_ID;i<=Constants.MAX_AUTH_ID;i++)
        {
            if(vertexDegree[i]>maxCohesionFactor)
            {
                maxCohesionFactor = vertexDegree[i];
            }
        }
         */
    }

    public static Set<Integer> findMaxCore(int k) {
        Set<Integer> Vk = new LinkedHashSet<Integer>();

        for (int nodeId = Constants.MIN_AUTH_ID; nodeId <= Constants.MAX_AUTH_ID; nodeId++) {
            if (vertexDegree[nodeId] >= k) {
                Vk.add(nodeId);
            }
        }

        return Vk;
    }

    public static Set<Integer> findMaxCore(Set<Integer> vSet, int k) {
        Set<Integer> Vk = new LinkedHashSet<Integer>();

        for (int nodeId : vSet) {
            if (vertexDegree[nodeId] >= k) {
                Vk.add(nodeId);
            }
        }

        return Vk;
    }
}
