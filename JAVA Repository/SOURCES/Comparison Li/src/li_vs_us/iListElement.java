/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package li_vs_us;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class iListElement {
    private Set<Integer> relVertices = new LinkedHashSet<Integer>();
    private double maxInScore = 0.0;
    private double maxOutScore = 0.0;

    public iListElement() {
    }

    public Set<Integer> getRelVertices() {
        return relVertices;
    }

    public void setRelVertices(Set<Integer> relVertices) {
        this.relVertices = relVertices;
    }

    public double getMaxInScore() {
        return maxInScore;
    }

    public void setMaxInScore(double maxInScore) {
        this.maxInScore = maxInScore;
    }

    public double getMaxOutScore() {
        return maxOutScore;
    }

    public void setMaxOutScore(double maxOutScore) {
        this.maxOutScore = maxOutScore;
    }
    
    public void addRelVertex(int vId)
    {
        this.relVertices.add(vId);
    }
}
