/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class Community {

    private Set<Integer> vSet;
    private double score;
    private int k;
    private QEG qeg;

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

    public boolean isContainedBy(Object obj) {
        Community c = (Community) obj;

        Set<Integer> temp = new LinkedHashSet<>(this.vSet);
        temp.removeAll(c.vSet);
        return (temp.size() == 0) || (this.equals(c));
    }

    @Override
    public boolean equals(Object obj) {
        Community c = (Community) obj;
        return this.vSet.equals(c.vSet);
    }

    
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

    int degree(int vertexId) {
        Set<Integer> adjList = new LinkedHashSet<Integer>(Main.authors[vertexId].getCoAuthorPaperCounts().keySet());
        adjList.retainAll(vSet);
        return adjList.size();
    }
    
    public boolean isEdge(int u, int v)
    {
        if(Main.authors[u].getCoAuthorPaperCounts().containsKey(v))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Need to replace by a more efficient algorithm
     * @return 
     */
    public long getNumberOfTriangles()
    {
        long numberTriangles = 0;
        
        for(int i:vSet)
        {
            for(int j:vSet)
            {
                if(j==i) continue;
                for(int k:vSet)
                {
                    if(k==j) continue;
                    
                    if(isEdge(i, j) && isEdge(j, k) && isEdge(i, k))
                    {
                        numberTriangles++;
                    }
                }
            }
        }
        
        numberTriangles/=6;
        
        return numberTriangles;
    }

    /**
     * Naive implementation
     * Later: https://github.com/jgrapht/jgrapht/blob/master/jgrapht-core/src/main/java/org/jgrapht/alg/scoring/ClusteringCoefficient.java
     * @return
     */
    public double clusteringCoeff() {
        long numTriplets = 0;
        long numTriangles = 0;

        for (int u : this.vSet) {
            int deg = degree(u);
            numTriplets +=((deg*(deg-1))/2);
        }

        numTriangles = getNumberOfTriangles();
        
        return (3.0 * numTriangles) / numTriplets;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

}
