/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import static graph_parser.Main.authors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class QEG {

    Set<Integer> V = new HashSet<Integer>();
    HashMap<Integer, Node> idNodeMap = new HashMap<Integer, Node>();
    HashMap<Integer, Integer> vertexDegree = new HashMap<Integer, Integer>();

    int maxDegree = 0;

    public QEG(KICQ kicq) {
        int n;
        n = kicq.keywords.length;

        for (int i = 0; i < n; i++) {
            Set<Integer> termVertices = new HashSet<Integer>();
            for (int j = 0; j < kicq.keywords[i].size(); j++) {
                int keywordId = kicq.keywords[i].get(j);
                Set<Integer> keywordVertices = GlobalInvertedList.IL[keywordId];
                termVertices.addAll(keywordVertices);
            }

            if (kicq.predicate == Constants.OR_PREDICATE) {
                this.V.addAll(termVertices);
            } else {
                if (i == 0) {
                    this.V = new LinkedHashSet<Integer>(termVertices);
                } else {
                    this.V.retainAll(termVertices);
                }
            }
        }

        //System.out.println(this.V);
        for (int nodeId : this.V) {
            Node node = new Node(nodeId, kicq);
            node.adjList.retainAll(this.V);
            int deg = node.adjList.size();
            if (deg > maxDegree) {
                maxDegree = deg;
            }
            idNodeMap.put(nodeId, node);
            //System.out.println(node);
        }

        this.coreDecomposition();

        if (Constants.SHOW_OUTPUT) {
            System.out.println("Max Degree: " + maxDegree);
            System.out.println("Total Nodes: " + this.V.size());
        }

        if (Constants.DEBUG_MODE) {
            for (int i : this.V) {
                for (int j : idNodeMap.get(i).adjList) {
                    if (!idNodeMap.get(j).adjList.contains(i)) {
                        System.err.println("Adjacency matrix not symmetric");
                    }
                }
            }
        }

    }

    public ArrayList<Set> findConnectedComponents(Set<Integer> vSet) {
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

    public void bfsVisit(Set<Integer> connectedComponent, int nodeId, Set<Integer> vSet, HashMap<Integer, Boolean> visited) {
        LinkedList<Integer> queue = new LinkedList<Integer>();
        visited.put(nodeId, true);
        queue.add(nodeId);

        while (queue.size() != 0) {
            nodeId = queue.poll();
            connectedComponent.add(nodeId);
            
            Node node = idNodeMap.get(nodeId);
            Set<Integer> adj = new LinkedHashSet(node.adjList);
            adj.retainAll(vSet);
            
            for (int v : adj) {
                if(visited.get(v)==false)
                {
                    visited.put(v, true);
                    queue.add(v);
                }
            }
        }
    }

    public void dfsVisit(Set<Integer> connectedComponent, int nodeId, Set<Integer> vSet, HashMap<Integer, Boolean> visited) {
        visited.put(nodeId, true);
        connectedComponent.add(nodeId);

        Node node = idNodeMap.get(nodeId);
        Set<Integer> adj = new LinkedHashSet(node.adjList);
        adj.retainAll(vSet);

        for (int v : adj) {
            if (!visited.get(v)) {
                dfsVisit(connectedComponent, v, vSet, visited);
            }
        }

    }

    public void coreDecomposition() {
        /*
        Also remove zero-degree nodes
         */
        int dMax = this.maxDegree;

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

        Set<Integer> zeroDegreeNodes = new LinkedHashSet<>();

        for (int nodeId : this.V) {
            int deg = idNodeMap.get(nodeId).adjList.size();
            if (deg == 0) {
                zeroDegreeNodes.add(nodeId);
            } else {
                count[deg]++;
                vertexDegree.put(nodeId, deg);
            }
        }

        //remove nodes with degree 0
        this.V.removeAll(zeroDegreeNodes);
        //System.out.println("Size of V after removal: "+V.size());

        b[1] = 0;
        currPos[1] = 0;
        for (int i = 2; i <= dMax; i++) {
            b[i] = b[i - 1] + count[i - 1];
            currPos[i] = b[i];
            //System.out.println("Deg "+i+" starts from "+b[i]);
        }

        //List of nodes sorted by degree
        int D[] = new int[V.size()];
        //p[i]: where is node i in D? 
        int p[] = new int[Constants.MAX_AUTH_ID + 1];

        for (int nodeId : this.V) {
            int deg = vertexDegree.get(nodeId);
            D[currPos[deg]] = nodeId;
            p[nodeId] = currPos[deg];
            currPos[deg]++;
        }

        for (int i = 0; i < D.length; i++) {
            int vId = D[i];
            Node node = idNodeMap.get(vId);
            node.adjList.retainAll(V);

            for (int uId : node.adjList) {
                int du = vertexDegree.get(uId);
                int dv = vertexDegree.get(vId);
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
                    vertexDegree.put(uId, du);
                }

            }
        }
    }

    public Set<Integer> findMaxCore(Set<Integer> subQEGNodes, int k) {
        Set<Integer> Vk = new LinkedHashSet<Integer>();

        for (int nodeId : subQEGNodes) {
            if (vertexDegree.get(nodeId) >= k) {
                Vk.add(nodeId);
            }
        }

        return Vk;
    }

    /**
     * Needs to be implemented again
     *
     * @param subQEGNodes
     * @param k
     * @return
     */
    public Set<Integer> findMaxCoreOld(Set<Integer> subQEGNodes, int k) {
        Set<Integer> Vk = new LinkedHashSet<Integer>();

        int minDeg = Integer.MAX_VALUE;
        int startVertex = Constants.INVALID_INT;

        HashMap<Integer, Integer> vDegree = new HashMap<Integer, Integer>();
        HashMap<Integer, Boolean> visited = new HashMap<Integer, Boolean>();

        for (int nodeId : subQEGNodes) {
            Set<Integer> temp = new LinkedHashSet<Integer>(idNodeMap.get(nodeId).adjList);
            temp.retainAll(subQEGNodes);
            int degree = temp.size();
            vDegree.put(nodeId, degree);

            visited.put(nodeId, false);

            if (degree < minDeg) {
                minDeg = degree;
                startVertex = nodeId;
            }

        }

        this.updateDegree(subQEGNodes, startVertex, vDegree, visited, k);

        for (int nodeId : subQEGNodes) {
            if (!visited.get(nodeId)) {
                updateDegree(subQEGNodes, nodeId, vDegree, visited, k);
            }
        }

        for (int nodeId : subQEGNodes) {
            if (vDegree.get(nodeId) >= k) {
                Vk.add(nodeId);
            }
        }

        return Vk;
    }

    public double score(Set<Integer> vSet, int k) {
        double s = 0.0;
        s += ((Constants.BETA * k) / maxDegree);

        double sum = 0.0;
        for (int v : vSet) {
            Node node = idNodeMap.get(v);
            sum += node.score;
        }
        s += ((1 - Constants.BETA) * (sum / this.V.size()));
        return s;
    }

    public Boolean updateDegree(Set<Integer> subQEGNodes, int nodeId, HashMap<Integer, Integer> vDegree, HashMap<Integer, Boolean> visited, int k) {
        if (nodeId == Constants.INVALID_INT) {
            System.err.println("Invalid input in QEG.updateDegree()");
        }

        visited.put(nodeId, true);

        Node vertex = idNodeMap.get(nodeId);
        Set<Integer> adj = new LinkedHashSet<Integer>(vertex.adjList);
        adj.retainAll(subQEGNodes);

        for (int adjId : adj) {
            //if vertex has degree smaller than k, adjacent vertices will be affected
            if (vDegree.get(nodeId) < k) {
                int prevDeg = vDegree.get(adjId);
                vDegree.put(adjId, prevDeg - 1);
            }

            Boolean isVisited = visited.get(adjId);
            if (!isVisited) {
                Boolean initLessThanK = false;
                if (vDegree.get(adjId) < k) {
                    initLessThanK = true;
                }
                //if after processing, adjacent vertices' degree is less than k, the cureent vertex will be affected
                if (updateDegree(subQEGNodes, adjId, vDegree, visited, k) && !initLessThanK) {
                    int prevDeg = vDegree.get(nodeId);
                    vDegree.put(nodeId, prevDeg - 1);
                }
            }
        }

        return (vDegree.get(nodeId) < k);
    }

    public void printSubgraph(Set<Integer> vertices) {
        for (int nodeId : vertices) {
            if (!idNodeMap.containsKey(nodeId)) {
                System.err.println("Error inside QEG.printSubgraph(): Vertex not in QEG");
            }
            Node vertex = idNodeMap.get(nodeId);

            String nodeStr = "[" + nodeId + ":" + vertex.score + "]->";

            Set<Integer> temp = new HashSet<Integer>(vertex.adjList);
            temp.retainAll(vertices);
            nodeStr += temp;
            System.out.println(nodeStr);
        }
    }
}
