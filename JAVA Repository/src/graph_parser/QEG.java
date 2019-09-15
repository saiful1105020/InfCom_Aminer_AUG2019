/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class QEG {

    Set<Integer> V = new HashSet<Integer>();
    HashMap<Integer, Node> idNodeMap = new HashMap<Integer, Node>();
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
                if(i==0)
                {
                    this.V = new LinkedHashSet<Integer>(termVertices);
                }
                else
                {
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

        //System.out.println("Max Degree: " + maxDegree);
        //System.out.println("Total Nodes: " + this.V.size());
    }

    public ArrayList<Set> findConnectedComponents(Set<Integer> vSet) {
        ArrayList<Set> components = new ArrayList<Set>();
        HashMap<Integer, Boolean> visited = new HashMap<Integer, Boolean>();

        for (int nodeId : vSet) {
            visited.put(nodeId, false);
        }

        for (int nodeId : vSet) {
            if (!visited.get(nodeId)) {
                Set<Integer> C = new LinkedHashSet<Integer>();
                dfsVisit(C, nodeId, vSet, visited);
                components.add(C);
            }
        }

        return components;
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

    public Set<Integer> findMaxCore(int k) {
        Set<Integer> Vk = new LinkedHashSet<Integer>();

        int minDeg = Integer.MAX_VALUE;
        int startVertex = Constants.INVALID_INT;

        HashMap<Integer, Integer> vDegree = new HashMap<Integer, Integer>();
        HashMap<Integer, Boolean> visited = new HashMap<Integer, Boolean>();

        for (int nodeId : this.V) {
            int degree = idNodeMap.get(nodeId).adjList.size();
            vDegree.put(nodeId, degree);
            visited.put(nodeId, false);
            if (degree < minDeg) {
                minDeg = degree;
                startVertex = nodeId;
            }
        }

        this.updateDegree(startVertex, vDegree, visited, k);

        for (int nodeId : this.V) {
            if (!visited.get(nodeId)) {
                updateDegree(nodeId, vDegree, visited, k);
            }
        }

        for (int nodeId : this.V) {
            if (vDegree.get(nodeId) >= k) {
                Vk.add(nodeId);
            }
        }

        return Vk;
    }

    public Set<Integer> findMaxCore(Set<Integer> subQEGNodes, int k) {
        Set<Integer> Vk = new LinkedHashSet<Integer>();

        int minDeg = Integer.MAX_VALUE;
        int startVertex = Constants.INVALID_INT;

        HashMap<Integer, Integer> vDegree = new HashMap<Integer, Integer>();
        HashMap<Integer, Boolean> visited = new HashMap<Integer, Boolean>();

        for (int nodeId : subQEGNodes) {
            Set<Integer> temp = idNodeMap.get(nodeId).adjList;
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

    public Boolean updateDegree(int nodeId, HashMap<Integer, Integer> vDegree, HashMap<Integer, Boolean> visited, int k) {
        if (nodeId == Constants.INVALID_INT) {
            System.err.println("Invalid input in QEG.updateDegree()");
        }
        visited.put(nodeId, true);

        Node vertex = idNodeMap.get(nodeId);
        for (int adjId : vertex.adjList) {
            //if vertex has degree smaller than k, adjacent vertices will be affected
            if (vDegree.get(nodeId) < k) {
                int prevDeg = vDegree.get(adjId);
                vDegree.put(adjId, prevDeg - 1);
            }

            Boolean isVisited = visited.get(adjId);
            if (!isVisited) {
                //if after processing, adjacent vertices' degree is less than k, the cureent vertex will be affected
                if (updateDegree(adjId, vDegree, visited, k)) {
                    int prevDeg = vDegree.get(nodeId);
                    vDegree.put(nodeId, prevDeg - 1);
                }
            }
        }

        return (vDegree.get(nodeId) < k);
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
                //if after processing, adjacent vertices' degree is less than k, the cureent vertex will be affected
                if (updateDegree(adjId, vDegree, visited, k)) {
                    int prevDeg = vDegree.get(nodeId);
                    vDegree.put(nodeId, prevDeg - 1);
                }
            }
        }

        return (vDegree.get(nodeId) < k);
    }

    public void printSubgraph(Set<Integer> vertices) {
        for (int nodeId : vertices) {
            String nodeStr = "[" + nodeId + "]->";
            if (!idNodeMap.containsKey(nodeId)) {
                System.err.println("Error inside QEG.printSubgraph(): Vertex not in QEG");
            }
            Node vertex = idNodeMap.get(nodeId);
            Set<Integer> temp = new HashSet<Integer>(vertex.adjList);
            temp.retainAll(vertices);
            nodeStr += temp;
            System.out.println(nodeStr);
        }
    }
}
