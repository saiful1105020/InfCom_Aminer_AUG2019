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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class QEG {
    Set<Integer> V = new HashSet<Integer>();
    HashMap<Integer, Node> idNodeMap = new HashMap<Integer,Node>();
    int maxDegree = 0;

    public QEG(KICQ kicq) {
        int n;
        n = kicq.keywords.length;
        
        for(int i=0;i<n;i++)
        {
            Set<Integer> termVertices = new HashSet<Integer>();
            for(int j=0;j<kicq.keywords[i].size();j++)
            {
                int keywordId = kicq.keywords[i].get(j);
                Set<Integer> keywordVertices = GlobalInvertedList.IL[keywordId];
                termVertices.addAll(keywordVertices);
            }
            
            if(kicq.predicate==Constants.OR_PREDICATE)
            {
                this.V.addAll(termVertices);
            }
            else
            {
                this.V.retainAll(termVertices);
            }
        }
        
        //System.out.println(this.V);
        
        for(int nodeId:this.V)
        {
            Node node = new Node(nodeId, kicq);
            node.adjList.retainAll(this.V);
            int deg = node.adjList.size();
            if(deg>maxDegree)
            {
                maxDegree = deg;
            }
            idNodeMap.put(nodeId, node);
        }    
        
        System.out.println("Max Degree: "+maxDegree);
    }
    
    public Set<Integer> findMaxCore(int k)
    {
        Set<Integer> Vk = new LinkedHashSet<Integer>();
        return Vk;
    }
    
}
