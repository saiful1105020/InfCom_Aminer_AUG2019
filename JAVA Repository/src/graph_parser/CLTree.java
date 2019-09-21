/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class CLTree {

    public static TreeNode root;
    public static int vertexDegree[] = new int[Constants.MAX_AUTH_ID + 1];
    public static int maxCohesionFactor = 0;

    public static void buildTree() {
        coreDecomposition();
        root = new TreeNode();
    }

    public static void traverseTree(TreeNode tnode)
    {
        System.out.println("Visiting children. Parent node k value = "+tnode.getCohesionFactor());
        for(TreeNode child:tnode.childNodes)
        {
            traverseTree(child);
            System.out.println(child);
        }
    }
    
    public static void printSubgraph(Set<Integer> vertices) {
        for (int nodeId : vertices) {
            String nodeStr = "[" + nodeId + "]->";

            Set<Integer> temp = new HashSet<Integer>(Main.authors[nodeId].getCoAuthorPaperCounts().keySet());
            temp.retainAll(vertices);
            nodeStr += temp;
            System.out.println(nodeStr);
        }
    }
    
    public static ArrayList<Set> findConnectedComponents(Set<Integer> vSet) {
        ArrayList<Set> components = new ArrayList<>();
        HashMap<Integer, Boolean> visited = new HashMap<>();

        for (int nodeId : vSet) {
            visited.put(nodeId, false);
        }

        for (int nodeId : vSet) {
            if (!visited.get(nodeId)) {
                Set<Integer> C = new LinkedHashSet<>();
                dfsVisit(C, nodeId, vSet, visited);
                components.add(C);
            }
        }

        return components;
    }

    public static void dfsVisit(Set<Integer> connectedComponent, int nodeId, Set<Integer> vSet, HashMap<Integer, Boolean> visited) {
        visited.put(nodeId, true);
        connectedComponent.add(nodeId);

        Set<Integer> adj = new LinkedHashSet(Main.authors[nodeId].getCoAuthorPaperCounts().keySet());
        adj.retainAll(vSet);

        for (int v : adj) {
            if (!visited.get(v)) {
                dfsVisit(connectedComponent, v, vSet, visited);
            }
        }

    }

    public static void coreDecomposition(){
        /*
        Also remove zero-degree nodes
        */
        int dMax = Main.maxDegree;
        
        //count[i]: how many vertices of degree i
        int count[] = new int[dMax+1];
        
        //temp variable for assigning index of D table
        int currPos[] = new int[dMax+1];
        
        //b[i]: where is (in D) the first vertex with degree i
        int b[] = new int[dMax+1];
        
        //System.out.println("Size of V: "+V.size());
        
        //init
        for(int i=0;i<=dMax;i++)
        {
            count[i]=0;
            currPos[i]=0;
        }
        
        for(int nodeId=Constants.MIN_AUTH_ID; nodeId<=Constants.MAX_AUTH_ID; nodeId++)
        {
            int deg = Main.authors[nodeId].getCoAuthorPaperCounts().keySet().size();
            count[deg]++;
            vertexDegree[nodeId] = deg;
        }
        
        
        //System.out.println("Size of V after removal: "+V.size());
        
        b[0]=0;
        currPos[0]=0;
        b[1]=0;
        currPos[1]=0;
        for(int i=2;i<=dMax;i++)
        {
            b[i]=b[i-1]+count[i-1];
            currPos[i]=b[i];
            //System.out.println("Deg "+i+" starts from "+b[i]);
        }
        
        //List of nodes sorted by degree
        int D[] = new int[Constants.MAX_AUTH_ID+1];
        //p[i]: where is node i in D? 
        int p[] = new int[Constants.MAX_AUTH_ID+1];
        
        for(int nodeId=Constants.MIN_AUTH_ID; nodeId<=Constants.MAX_AUTH_ID; nodeId++)
        {
            int deg = vertexDegree[nodeId];
            if(deg==0) continue;
            D[currPos[deg]]=nodeId;
            p[nodeId] = currPos[deg];
            currPos[deg]++;
        }
        
        for(int i=0;i<currPos[dMax];i++)
        {
            int vId = D[i];
            
            for(int uId:Main.authors[vId].getCoAuthorPaperCounts().keySet())
            {
                int du = vertexDegree[uId];
                int dv = vertexDegree[vId];
                if(du>dv)
                {
                    int pu = p[uId];
                    int pw = b[du];
                    int w = D[pw];
                    
                    if(uId!=w)
                    {
                        D[pu] = w;
                        D[pw] = uId;
                        p[uId] = pw;
                        p[w] = pu;
                    }
                    b[du]++;
                    du--;
                    vertexDegree[uId] = du;
                }
                
            }
        }
        
        /*
        for(int i=Constants.MIN_AUTH_ID;i<=Constants.MAX_AUTH_ID;i++)
        {
            if(vertexDegree[i]>maxCohesionFactor)
            {
                maxCohesionFactor = vertexDegree[i];
            }
        }
        */
    }
    
    public static Set<Integer> findMaxCore(int k)
    {
        Set<Integer> Vk = new LinkedHashSet<Integer>();
        
        for(int nodeId=Constants.MIN_AUTH_ID; nodeId<=Constants.MAX_AUTH_ID;nodeId++)
        {
            if(vertexDegree[nodeId]>=k)
            {
                Vk.add(nodeId);
            }
        }
        
        return Vk;
    }
    
    public static Set<Integer> findMaxCore(Set<Integer>vSet, int k)
    {
        Set<Integer> Vk = new LinkedHashSet<Integer>();
        
        for(int nodeId:vSet)
        {
            if(vertexDegree[nodeId]>=k)
            {
                Vk.add(nodeId);
            }
        }
        
        return Vk;
    }
}
