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

        System.out.println("Max Degree: " + maxDegree);
        System.out.println("Total Nodes: " + this.V.size());
        /*
        for(int i:this.V)
        {
            for(int j:idNodeMap.get(i).adjList)
            {
                if(!idNodeMap.get(j).adjList.contains(i))
                {
                    System.err.println("Adjacency matrix not symmetric");
                }
            }
        }
         */
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
        Set<Integer> candidateNodes = new LinkedHashSet<Integer>();

        int minDeg = Integer.MAX_VALUE;
        int startVertex = Constants.INVALID_INT;

        //HashMap<Integer, Integer> vDegree = new HashMap<Integer, Integer>();
        //HashMap<Integer, Boolean> visited = new HashMap<Integer, Boolean>();
        for (int nodeId : this.V) {
            int degree = idNodeMap.get(nodeId).adjList.size();
            if (degree >= k) {
                candidateNodes.add(nodeId);
            }
        }

        return findMaxCore(candidateNodes, k);
    }

    public Set<Integer> findMaxCore(Set<Integer> subQEGNodes, int k) {
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

            /*
            if((nodeId==1414)||((nodeId>=1979)&&(nodeId<=1984)))
            {
                System.out.println("inside max core: "+nodeId+" :: "+vDegree.get(nodeId));
            }
             */
        }
        //System.exit(0);

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

        if ((nodeId == 1414) || ((nodeId >= 1979) && (nodeId <= 1984))) {
            System.out.println("visiting: " + nodeId + " :deg: " + vDegree.get(nodeId));

            System.out.println("DBG: degrees->");
            System.out.println("1414: " + vDegree.get(1414));
            for (int tmp = 1979; tmp <= 1984; tmp++) {
                System.out.println(tmp + ": " + vDegree.get(tmp));
            }
            System.out.println("---");
        }

        for (int adjId : adj) {
            if ((nodeId == 1414) || ((nodeId >= 1979) && (nodeId <= 1984))) {
                System.out.println("visiting: " + nodeId + " :adj: " + adjId);

                System.out.println("DBG: degrees->");
                System.out.println("1414: " + vDegree.get(1414));
                for (int tmp = 1979; tmp <= 1984; tmp++) {
                    System.out.println(tmp + ": " + vDegree.get(tmp));
                }
                System.out.println("---");
            }
            //if vertex has degree smaller than k, adjacent vertices will be affected
            if (vDegree.get(nodeId) < k) {
                int prevDeg = vDegree.get(adjId);
                vDegree.put(adjId, prevDeg - 1);
            }

            Boolean isVisited = visited.get(adjId);
            if (!isVisited) {
                Boolean initLessThanK = false;
                if(vDegree.get(adjId)<k) initLessThanK=true;
                //if after processing, adjacent vertices' degree is less than k, the cureent vertex will be affected
                if (updateDegree(subQEGNodes, adjId, vDegree, visited, k) && !initLessThanK) {
                    int prevDeg = vDegree.get(nodeId);
                    vDegree.put(nodeId, prevDeg - 1);

                    if ((nodeId == 1414) || ((nodeId >= 1979) && (nodeId <= 1984))) {
                        System.out.println("Child of: " + nodeId + " :: " + adjId);

                        System.out.println("DBG: degrees->");
                        System.out.println("1414: " + vDegree.get(1414));
                        for (int tmp = 1979; tmp <= 1984; tmp++) {
                            System.out.println(tmp + ": " + vDegree.get(tmp));
                        }
                        System.out.println("---");
                    }
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
