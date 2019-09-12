/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class Community {
    private Set<Integer> vSet;
    private double score;

    public Community() {
        vSet = new LinkedHashSet<Integer>();
        score=0.0;
    }

    
    public Community(Set<Integer> vSet, double score) {
        this.vSet = vSet;
        this.score = score;
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

    @Override
    public boolean equals(Object obj) {
        Community c = (Community) obj;
        return this.vSet.equals(c.vSet);
    }
    
    
}
