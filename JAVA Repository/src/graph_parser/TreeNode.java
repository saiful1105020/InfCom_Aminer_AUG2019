/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class TreeNode {

    private int cohesionFactor;
    private TreeNode parent;

    //computed first, updated later from child
    Set<Integer> vertexSet = new LinkedHashSet<>();

    ArrayList<TreeNode> childNodes = new ArrayList<>();

    Map<Integer, iListElement> iList = new HashMap<>();

    public TreeNode() {
        //constructor for root node
        this.cohesionFactor = 0;
        this.parent = null;
        
        for (int i = Constants.MIN_AUTH_ID; i <= Constants.MAX_AUTH_ID; i++) {
            this.vertexSet.add(i);
        }

        this.attachChildNodes();
        this.compressVertices(this);

    }

    public TreeNode(TreeNode parent) {
        //constructor for other nodes
        this.parent = parent;
    }

    public void compressVertices(TreeNode tnode)
    {
        for(TreeNode child:tnode.childNodes)
        {
            tnode.vertexSet.removeAll(child.vertexSet);
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
                
                tnode.attachChildNodes();
                
                this.childNodes.add(tnode);
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
        String str="";
        str+="k: "+this.cohesionFactor+", vertices: "+this.vertexSet+"\n";
        return str; //To change body of generated methods, choose Tools | Templates.
    }
    
    

}
