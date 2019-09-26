/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author MSI
 */
public class TreeExplore {

    KICQ kicq;
    QEG qeg;

    PriorityQueue<Community> Q;

    public TreeExplore(KICQ kicq) {
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

        if (this.qeg.V.size() != 0) {
            this.solve();
        }

        if (Constants.SHOW_OUTPUT) {
            for (int i = 0; i < KICQ.r; i++) {
                Community c = this.Q.remove();
                System.out.println("Top-" + (KICQ.r - i) + ": " + c.getK() + "-core");
                this.qeg.printSubgraph(c.getvSet());
                System.out.println("Score: " + c.getScore());
            }
        }
    }

    public void solve() {
        for (int i = 0; i < KICQ.r; i++) {
            Community c = new Community();
            this.Q.add(c);
        }
        visitTree(CLTree.root);
    }

    public void visitTree(TreeNode u) {
        if (u.childNodes.size() != 0) {
            double maxDesScore = maxDescendentScore(u);

            double rTopScore = this.Q.peek().getScore();
            if (u == CLTree.root) {
                rTopScore = -1.0;
                //In Root, inverted list is not maintained for memory efficiency
                //Force to visit descendants of root
            }

            if (maxDesScore > rTopScore) {
                for (TreeNode v : u.childNodes) {
                    visitTree(v);
                }
            }
        }

        double maxNodeScore = maxNodeScore(u);
        /*
        if (maxNodeScore > 0.0) {
            System.out.println("Node Max Score: " + maxNodeScore);
            System.out.println(u);
            System.out.println("-------");
        }
        */
        double rTopScore = this.Q.peek().getScore();
        if (u.getCohesionFactor() >= KICQ.k_min && maxNodeScore > rTopScore) {
            //BASIC-ALGO
        }
        return;
    }

    public double maxDescendentScore(TreeNode u) {
        double score = 0.0;

        double cohesivenessScore = (Constants.BETA * u.getkMax()) / qeg.maxDegree;

        double infScore = 0.0;

        for (int i = 0; i < kicq.keywords.length; i++) {
            double termScore = 0.0;
            for (int keywordId : kicq.keywords[i]) {
                if (u.iList.containsKey(keywordId)) {
                    termScore += u.iList.get(keywordId).getMaxOutScore();
                }
            }

            if (i == 0) {
                infScore = termScore;
            } else {
                if (kicq.predicate == Constants.AND_PREDICATE) {
                    infScore = Math.min(infScore, termScore);
                } else {
                    infScore += termScore;
                }
            }
        }

        infScore = ((1.0 - Constants.BETA) * infScore) / qeg.V.size();

        if (infScore != 0.0) {
            score = cohesivenessScore + infScore;
        }

        return score;
    }

    public double maxNodeScore(TreeNode u) {
        double score = 0.0;

        double cohesivenessScore = (Constants.BETA * u.getCohesionFactor()) / qeg.maxDegree;

        double infScore = 0.0;

        for (int i = 0; i < kicq.keywords.length; i++) {
            double termScore = 0.0;
            for (int keywordId : kicq.keywords[i]) {
                if (u.iList.containsKey(keywordId)) {
                    termScore += u.iList.get(keywordId).getMaxInScore();
                }
            }

            if (i == 0) {
                infScore = termScore;
            } else {
                if (kicq.predicate == Constants.AND_PREDICATE) {
                    infScore = Math.min(infScore, termScore);
                } else {
                    infScore += termScore;
                }
            }
        }

        infScore = ((1.0 - Constants.BETA) * infScore) / qeg.V.size();

        if (infScore != 0.0) {
            score = cohesivenessScore + infScore;
        }

        return score;
    }
}
