/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class ModifiedPruneExplore {

    QEG qeg;

    double rTopScore;
    PriorityQueue<Community> Q;

    public ModifiedPruneExplore(QEG qeg, int k_max, PriorityQueue<Community> Q) {
        this.qeg = qeg;
        this.Q = Q;
        rTopScore = Q.peek().getScore();

        if (this.qeg.V.size() != 0) {
            this.solve(this.qeg.V, KICQ.k_min, k_max);
        }
    }

    public void solve(Set<Integer> H, int k, int k_max) {
        if (Constants.SPECIAL_REGION_PRINT) {
            System.out.println("Starting k: " + k);
        }

        int minDegree = Integer.MAX_VALUE;
        int maxDegree = Integer.MIN_VALUE;
        for (int vId : H) {
            int deg = qeg.vertexDegree.get(vId);
            if (deg > maxDegree) {
                maxDegree = deg;
            }
            if (deg < minDegree) {
                minDegree = deg;
            }
        }

        if (minDegree > k) {
            k = minDegree;
        }

        if (Constants.SPECIAL_REGION_PRINT) {
            System.out.println("Updated k: " + k);
        }

        Set<Integer> Vk = this.qeg.findMaxCore(H, k);

        if (Constants.SPECIAL_REGION_PRINT) {
            System.out.println("k-core vertices: " + H);
        }

        ArrayList<Set> components = this.qeg.findConnectedComponents(Vk);

        for (int i = 0; i < components.size(); i++) {
            Set<Integer> componentNodes = components.get(i);

            if (Constants.SPECIAL_REGION_PRINT) {
                this.qeg.printSubgraph(componentNodes);
                System.out.println("-------\n");
            }

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

            for (int newK = k + 1; newK <= k_max; newK++) {
                double upperBoundScore = qeg.score(componentNodes, newK);
                if (upperBoundScore > rTopScore) {
                    if(Constants.SPECIAL_REGION_PRINT)
                    {
                        System.out.println("Expanding for new k: " + newK);
                    }
                    this.solve(componentNodes, newK, k_max);
                    if(Constants.SPECIAL_REGION_PRINT)
                    {
                        System.out.println("Returning ...");
                    }
                    break;
                }
            }
        }
    }
}
