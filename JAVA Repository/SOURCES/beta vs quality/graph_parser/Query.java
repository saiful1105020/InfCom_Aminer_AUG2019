/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.ArrayList;

/**
 *
 * @author MSI
 */
public class Query {
    ArrayList<String> terms = new ArrayList<String>();
    int predicate;

    public Query(ArrayList<String> terms, int predicate) {
        
        for(int i=0;i<terms.size();i++)
        {
            this.terms = (ArrayList<String>)terms.clone();
        }
        this.predicate = predicate;
    }
    
    
}
