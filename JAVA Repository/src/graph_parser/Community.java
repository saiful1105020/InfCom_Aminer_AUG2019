/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class Community {

    private Set<Integer> vSet;
    private double score;
    private int k;
    
    public Community() {
        vSet = new LinkedHashSet<Integer>();
        score = 0.0;
        k = 0;
    }

    public Community(Set<Integer> vSet, double score, int k) {
        this.vSet = vSet;
        this.score = score;
        this.k = k;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Set<Integer> getvSet() {
        return vSet;
    }

    public void setvSet(Set<Integer> vSet) {
        this.vSet = vSet;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
    
    //all vertices in this community are contained by the vertices of community "container" 
    public boolean isContainedBy(Object container) {
        Community c = (Community) container;

        Set<Integer> temp = new LinkedHashSet<>(this.vSet);
        temp.removeAll(c.vSet);
        return (temp.size() == 0) || (this.equals(c));
    }

    @Override
    public boolean equals(Object obj) {
        Community c = (Community) obj;
        return this.vSet.equals(c.vSet);
    }

    int degree(int vertexId) {
        Set<Integer> adjList = new LinkedHashSet<Integer>(Main.authors[vertexId].getCoAuthorPaperCounts().keySet());
        adjList.retainAll(vSet);
        return adjList.size();
    }

    public boolean isEdge(int u, int v) {
        if (Main.authors[u].getCoAuthorPaperCounts().containsKey(v)) {
            return true;
        }
        return false;
    }

    /**
     * An $O(|E|^{3/2})$ algorithm for counting the number of non-trivial
     * triangles in an undirected graph. A non-trivial triangle is formed by
     * three distinct vertices all connected to each other.
     */
    public long getNumberOfTriangles() {
        long numberTriangles = 0;
        int sqrtV = (int) Math.sqrt(this.vSet.size());

        Map<Integer, Integer> vertexDegree = new HashMap<>();
        Map< Integer, Set<Integer> > adjList = new HashMap<>();
        
        for (int v : this.vSet) {
            Set<Integer> adj = new HashSet<>(Main.authors[v].getCoAuthorPaperCounts().keySet());
            adj.retainAll(this.vSet);
            int deg = adj.size();
            vertexDegree.put(v, deg);
            adjList.put(v,adj);
        }

        ArrayList<Integer> heavyHeaterVertices = new ArrayList<>();
        // vertex v is a heavy-hitter iff degree(v) >= sqrtV
        for (int v : this.vSet) {
            int d = vertexDegree.get(v);
            if (d >= sqrtV) {
                heavyHeaterVertices.add(v);
            }
        }

        // count the number of triangles formed from only heavy-hitter vertices
        numberTriangles = getNumberOfTrianglesNaive(heavyHeaterVertices);

        for (int v1 : this.vSet) {
            for (int v2 : adjList.get(v1)) {
                if (v1 == v2) {
                    continue;
                }

                if (vertexDegree.get(v1) < sqrtV || vertexDegree.get(v2) < sqrtV) {
                    // ensure that v1 <= v2 (swap them otherwise)
                    if ((vertexDegree.get(v1) > vertexDegree.get(v2)) || ((vertexDegree.get(v1) == vertexDegree.get(v2)) && (v1 > v2))) {
                        int temp = v1;
                        v1 = v2;
                        v2 = temp;
                    }

                    for (int u : adjList.get(v1)) {
                        // check if the triangle is non-trivial: u, v1, v2 are distinct vertices
                        if (u == v1 || u == v2) {
                            continue;
                        }

                        /*
                        * Check if u > v2 and if (u, v2) is a valid edge. If both of them are true,
                        * then we have a new triangle (v1, v2, u) and all three vertices in the
                        * triangle are ordered (v1 <= v2 <= u) so we count it only once.
                        */
                        if ((vertexDegree.get(u) > vertexDegree.get(v2)) || ((vertexDegree.get(u) == vertexDegree.get(v2)) && (u > v2)) && isEdge(u, v2)) {
                            numberTriangles++;
                        }
                    }
                }
            }
        }
        return numberTriangles;
    }

    public long getNumberOfTrianglesNaive(ArrayList<Integer> heavyHeaters) {
        long numberTriangles = 0;

        for (int i : heavyHeaters) {
            for (int j : heavyHeaters) {
                if (j == i) {
                    continue;
                }
                for (int k : heavyHeaters) {
                    if (k == j) {
                        continue;
                    }

                    if (isEdge(i, j) && isEdge(j, k) && isEdge(i, k)) {
                        numberTriangles++;
                    }
                }
            }
        }

        numberTriangles /= 6;

        return numberTriangles;
    }
    
    //Cohesiveness Metric
    public double density() {
        int totalOutDegree = 0; //totalOutDegree = 2*edges
        int vertices = this.vSet.size();
        if (vertices == 0) {
            return 0.0;
        }

        for (int u : vSet) {
            totalOutDegree += degree(u);
        }

        return ((double) (totalOutDegree)) / (vertices * (vertices - 1));
    }

    /**
     * Copied from jgrapht implementation
     * https://github.com/jgrapht/jgrapht/blob/master/jgrapht-core/src/main/java/org/jgrapht/alg/scoring/ClusteringCoefficient.java
     * @return clustering co-efficent (cohesiveness measure)
     */
    public double clusteringCoeff() {
        long numTriplets = 0;
        long numTriangles = 0;

        for (int u : this.vSet) {
            int deg = degree(u);
            numTriplets += ((deg * (deg - 1)) / 2);
        }

        numTriangles = getNumberOfTriangles();

        return (3.0 * numTriangles) / numTriplets;
    }

    //cohesiveness measure: average degree of community members
    public double degree()
    {
        double d = 0;
        for (int u : this.vSet) {
            d+= degree(u);
        }
        return (d/this.vSet.size());
    }
    
    //cohesiveness measure
    public double diameter()
    {
        double dm = 0;
        ArrayList<Integer> vertices = new ArrayList<>(this.vSet);
        for (int i = 0; i < vertices.size() - 1; i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                double dist =shortestPath(vertices.get(i), vertices.get(j));
                dm = Math.max(dm, dist);
            }
        }
        
        return dm;
    }
    
    public double shortestPath(int source, int dest)
    {
        if(source==dest) return 0;
        
        Map<Integer, Boolean> visited = new LinkedHashMap<>();
        Map<Integer, Integer> vertexLevel = new LinkedHashMap<>();
        LinkedList<Integer> queue = new LinkedList<Integer>();
        visited.put(source, true);
        vertexLevel.put(source, 0);
        queue.add(source);

        while (queue.size() != 0) {
            int u = queue.poll();
            int lvl = vertexLevel.get(u);
            
            Set<Integer> adj = new LinkedHashSet(Main.authors[u].getCoAuthorPaperCounts().keySet());
            adj.retainAll(this.vSet);
            
            for (int v : adj) {
                if(visited.containsKey(v)==false)
                {
                    if(v==dest) return lvl+1;
                    
                    visited.put(v, true);
                    vertexLevel.put(v, lvl+1);
                    queue.add(v);
                }
            }
        }
        return 0;
    }
}
