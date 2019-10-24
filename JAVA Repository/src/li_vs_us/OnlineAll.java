/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package li_vs_us;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class OnlineAll {

    KICQ kicq;
    QEG qeg;
    int k;

    ArrayList<Community> Q = new ArrayList<>();

    public OnlineAll(KICQ kicq, int k) {
        this.kicq = kicq;
        this.qeg = new QEG(kicq);
        this.k = k; 
        
        if (this.qeg.V.size() != 0) {
            this.solve();
        }
    }

    public void solve() {
        //Compute the maximal k-core C^k(G) of G
        Set<Integer> Vk = this.qeg.findMaxCore(this.qeg.V, k);
        //Array of all communities
        ArrayList<Community> C = new ArrayList<>();
        int i = 0;
        //C^k(G) is not empty
        while (Vk.size() != 0) {
            int minScoreNode = -1;
            //the node with the smallest weight
            double minNodeScore = Double.MAX_VALUE;
            for (int u : Vk) {
                double sc = this.qeg.idNodeMap.get(u).score;

                if (sc < minNodeScore) {
                    minNodeScore = sc;
                    minScoreNode = u;
                }
            }

            //Ideally, the implementation should be like this.
            //H^k(i): maximal connected component of C^k(G) with the smallest influence value
            //Set<Integer> Hk = this.qeg.findConnectedComponent(Vk, minScoreNode);
            Set<Integer> Hk = new LinkedHashSet<>(Vk);
            //saved as C(i)
            Community c = new Community(Hk, minNodeScore, k);
            C.add(c);

            DFS(Vk, minScoreNode);
            i++;
        }

        //System.out.println("Iterations: " + i);
        //Update result. Q contains the top r communities
        for (int j = 1; j <= Constants.TOP_R; j++) {
            Community cm = C.get(C.size() - j);
            Q.add(cm);
            //System.out.println(cm.getvSet());
        }
    }

    public void DFS(Set<Integer> Vk, int u) {
        //neighbors of u in C^k(G)
        Set<Integer> adj = new LinkedHashSet<>(this.qeg.idNodeMap.get(u).adjList);
        adj.retainAll(Vk);
        for (int v : adj) {
            //remove edge (u,v) from C^k(G)
            this.qeg.idNodeMap.get(v).adjList.remove(u);
            this.qeg.idNodeMap.get(u).adjList.remove(v);

            //check degree of v in C^k(G)
            Set<Integer> vAdj = new LinkedHashSet<>(this.qeg.idNodeMap.get(v).adjList);
            vAdj.retainAll(Vk);
            int degree = vAdj.size();
            if (degree < k) {
                DFS(Vk, v);
            }
        }
        //delete node u from the connected component
        Vk.remove(u);
    }

}
