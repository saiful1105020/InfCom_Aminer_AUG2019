/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MSI
 */
public class QueryGenerator {
    public KICQ queries[][] = new KICQ[2][100];
    
    public QueryGenerator() {
        try {
            Scanner input = new Scanner(new File("Queries_100_random_co.txt"));
            int count=0;
            while(input.hasNext())
            {
                int n = Integer.parseInt(input.nextLine());
                String q = input.nextLine();
                ArrayList<Integer> keywords[] = new ArrayList[n];
                
                String tmp[] = q.split(";");
                
                if(n!=tmp.length)
                {
                    System.out.println("Error");
                }
                
                for(int i=0;i<tmp.length;i++)
                {
                    keywords[i] = new ArrayList<>();
                    
                    tmp[i] = tmp[i].replace("[", "").replace("]", "");
                    String ids[] = tmp[i].split(",");
                    for(int j=0;j<ids.length;j++)
                    {
                        keywords[i].add(Integer.parseInt(ids[j].trim()));
                    }
                }
                
                KICQ kicq = new KICQ(keywords, Constants.OR_PREDICATE);
                queries[Constants.OR_PREDICATE][count] = kicq;
                kicq = new KICQ(keywords, Constants.AND_PREDICATE);
                queries[Constants.AND_PREDICATE][count] = kicq;
                
                count++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QueryGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public QueryGenerator(String filename) {
        try {
            Scanner input = new Scanner(new File(filename));
            int count=0;
            while(input.hasNext())
            {
                int n = Integer.parseInt(input.nextLine());
                String q = input.nextLine();
                ArrayList<Integer> keywords[] = new ArrayList[n];
                
                String tmp[] = q.split(";");
                
                if(n!=tmp.length)
                {
                    System.out.println("Error");
                }
                
                for(int i=0;i<tmp.length;i++)
                {
                    keywords[i] = new ArrayList<>();
                    
                    tmp[i] = tmp[i].replace("[", "").replace("]", "");
                    String ids[] = tmp[i].split(",");
                    for(int j=0;j<ids.length;j++)
                    {
                        keywords[i].add(Integer.parseInt(ids[j].trim()));
                    }
                }
                
                KICQ kicq = new KICQ(keywords, Constants.OR_PREDICATE);
                queries[Constants.OR_PREDICATE][count] = kicq;
                kicq = new KICQ(keywords, Constants.AND_PREDICATE);
                queries[Constants.AND_PREDICATE][count] = kicq;
                
                count++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QueryGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
