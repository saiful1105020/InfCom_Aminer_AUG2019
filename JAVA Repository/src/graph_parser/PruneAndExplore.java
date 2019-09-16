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
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class PruneAndExplore {

    KICQ kicq;
    QEG qeg;

    PriorityQueue<Community> Q;
    double rTopScore;

    public PruneAndExplore(KICQ kicq) {
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

        for (int i = 0; i < KICQ.r; i++) {
            Community c = new Community();
            this.Q.add(c);
        }

        this.rTopScore = this.Q.peek().getScore();

        if (this.qeg.V.size() != 0) {
            this.solve(this.qeg.V, KICQ.k_min);
        }

        for (int i = 0; i < KICQ.r; i++) {
            Community c = this.Q.remove();
            System.out.println("Top-" + (KICQ.r - i) + ": " + c.getK() + "-core");
            this.qeg.printSubgraph(c.getvSet());
            System.out.println("Score: " + c.getScore());
        }

        /*
        int nodeId=1414;
        System.out.println("DBG: Degree of " + nodeId + ": " + qeg.idNodeMap.get(nodeId).adjList.size());
        for(nodeId=1979;nodeId<=1984;nodeId++) {
            System.out.println("DBG: Degree of " + nodeId + ": " + qeg.idNodeMap.get(nodeId).adjList.size());
        }
         */
    }

    public void solve(Set<Integer> H, int k) {
        //System.out.println("Starting k: "+k);

        int minDegree = Integer.MAX_VALUE;
        int maxDegree = Integer.MIN_VALUE;
        for (int vId : H) {
            Node node = qeg.idNodeMap.get(vId);

            Set<Integer> subgraphAdjacent = new LinkedHashSet<Integer>(node.adjList);
            subgraphAdjacent.retainAll(H);

            int deg = subgraphAdjacent.size();

            if (deg < minDegree) {
                minDegree = deg;
            }

            if (deg > maxDegree) {
                maxDegree = deg;
            }

        }

        if (minDegree > k) {
            k = minDegree;
        }

        //System.out.println("Updated k: " + k);
        Set<Integer> Vk = this.qeg.findMaxCore(H, k);

        if (k == 6 && H.contains(1414)) {
            System.out.println("DBG: 6-core output->");
            if (!Vk.contains(1414)) {
                System.err.println("Error! 1414 not found");
            }
            for (int tmp = 1979; tmp <= 1984; tmp++) {
                if (!Vk.contains(tmp)) {
                    System.err.println("Error! " + tmp + " not found");
                }
            }

        }

        //if Vk empty, return
        ArrayList<Set> components = this.qeg.findConnectedComponents(Vk);

        for (int i = 0; i < components.size(); i++) {
            Set<Integer> componentNodes = components.get(i);
            //this.qeg.printSubgraph(componentNodes);
            //System.out.println("-------\n");

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

            for (int newK = k + 1; newK <= maxDegree; newK++) {
                double upperBoundScore = qeg.score(componentNodes, newK);
                if (upperBoundScore > rTopScore) {
                    //System.out.println("Expanding for new k: "+newK);
                    this.solve(componentNodes, newK);
                    //System.out.println("Returning...\n");
                    break;
                }
            }
        }

    }

}
