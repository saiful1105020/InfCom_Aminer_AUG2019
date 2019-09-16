/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class BasicExplore {

    KICQ kicq;
    QEG qeg;

    PriorityQueue<Community> Q;

    public BasicExplore(KICQ kicq) {
        this.kicq = kicq;
        this.qeg = new QEG(kicq);

        Comparator<Community> CommunityComparator = new Comparator<Community>() {
            @Override
            public int compare(Community c1, Community c2) {
                if (c1.getScore() < c2.getScore()) {
                    return -1;
                }
                if (c1.getScore() > c2.getScore()) {
                    return 1;
                }
                return 0;
            }
        };
        this.Q = new PriorityQueue<Community>(KICQ.r, CommunityComparator);

        if(this.qeg.V.size()!=0)
        {
            this.solve();
        }
        
        //Result
        
        for(int i=0;i<KICQ.r;i++)
        {
            Community c = this.Q.remove();
            System.out.println("Top-"+(KICQ.r-i)+": "+c.getK()+"-core");
            this.qeg.printSubgraph(c.getvSet());
            System.out.println("Score: "+c.getScore());
        }
        
        Integer testIds[] = {8556, 8560, 8561, 8557, 8562, 3699, 8558, 8563, 8564, 8559};
        Set<Integer> testSet = new HashSet<>(Arrays.asList(testIds));
        this.qeg.printSubgraph(testSet);
        System.exit(0);
        
    }

    public void solve() {
        for(int i=0;i<KICQ.r;i++)
        {
            Community c = new Community();
            this.Q.add(c);
        }
        double rTopScore = this.Q.peek().getScore();

        for (int k = KICQ.k_min; k <= qeg.maxDegree; k++) {
            System.out.println("k-core: k="+k+"\n----------\n");
            //Find maximal k-core
            Set<Integer> Vk = this.qeg.findMaxCore(this.qeg.V, k);
            //System.out.println(Vk);
            
            //this.qeg.printSubgraph(Vk);
            //Find set of connected components
            ArrayList<Set> components = this.qeg.findConnectedComponents(Vk);
            //System.out.println("Number of components: "+components.size());
            for (int i = 0; i < components.size(); i++) {
                //System.out.println("Component " + i + ": ");
                Set<Integer> componentNodes = components.get(i);
                System.out.println("k:"+k+"\t num nodes: "+componentNodes.size());
                //this.qeg.printSubgraph(componentNodes);
                //assign score to each connected component
                double score = this.qeg.score(componentNodes, k);
                if (score > rTopScore) {
                    Community candidate = new Community(componentNodes, score, k);
                    
                    if (this.Q.contains(candidate)) {
                        this.Q.remove(candidate);
                        this.Q.add(candidate);
                    } else {
                        this.Q.remove();
                        this.Q.add(candidate);
                    }
                    
                    rTopScore = this.Q.peek().getScore();
                }

                //System.out.println("Score: " + score);
                //System.out.println("--------\n");
            }

        }
        
    }

}
