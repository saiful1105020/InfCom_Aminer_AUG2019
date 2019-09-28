/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * @author MSI
 */
public class TreeExplore {

    KICQ kicq;
    static int nodesAccessed = 0;
    //QEG qeg;

    PriorityQueue<Community> Q;

    public TreeExplore(KICQ kicq) {
        this.kicq = kicq;

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

        this.solve();

        if (Constants.SHOW_OUTPUT) {
            for (int i = 0; i < KICQ.r; i++) {
                Community c = this.Q.remove();
                System.out.println("Top-" + (KICQ.r - i) + ": " + c.getK() + "-core");
                //System.out.println(c.getvSet());
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

    public Set<Integer> decompressVertices(TreeNode u) {
        Set<Integer> testSet = new LinkedHashSet<>(u.vertexSet);
        TreeNode iter = u;
        while (iter.childNodes.size() != 0) {
            for (TreeNode child : iter.childNodes) {
                testSet.addAll(decompressVertices(child));
            }
        }
        return testSet;
    }

    public void visitTree(TreeNode u) {

        double maxDesScore = maxDescendentScore(u);

        double rTopScore = this.Q.peek().getScore();
        if (u == CLTree.root) {
            rTopScore = -1.0;
            //In Root, inverted list is not maintained for memory efficiency
            //Force to visit descendants of root
        }

        int n = kicq.keywords.length;

        for (int i = 0; i < n; i++) {
            Set<Integer> termVertices = new HashSet<Integer>();
            for (int j = 0; j < kicq.keywords[i].size(); j++) {
                int keywordId = kicq.keywords[i].get(j);
                if (u.iList.containsKey(keywordId)) {
                    Set<Integer> keywordVertices = u.iList.get(keywordId).getRelVertices();
                    termVertices.addAll(keywordVertices);
                }
            }

            if (kicq.predicate == Constants.OR_PREDICATE) {
                u.fullVertexSet.addAll(termVertices);
            } else {
                if (i == 0) {
                    u.fullVertexSet = new LinkedHashSet<Integer>(termVertices);
                } else {
                    u.fullVertexSet.retainAll(termVertices);
                }
            }
        }

        if (maxDesScore > rTopScore) {

            for (TreeNode v : u.childNodes) {
                visitTree(v);
                u.fullVertexSet.addAll(v.fullVertexSet);
                v.fullVertexSet.clear();
            }
        }


        /*
        if(u.vertexSet.contains(1603))
        {
            System.out.println("Node ID: "+u.getTreeNodeId());
            System.out.println("K: "+u.getCohesionFactor());
            System.out.println(u.fullVertexSet);
        }
         */
        double maxNodeScore = maxNodeScore(u);

        rTopScore = this.Q.peek().getScore();

        if (u.getCohesionFactor() >= KICQ.k_min && maxNodeScore > rTopScore) {
            nodesAccessed++;

            QEG localQeg = new QEG(kicq, new LinkedHashSet(u.fullVertexSet));

            //DEBUG CODE FOR A PARTICULAR TREE NODE
            /*
            if (u.getTreeNodeId() == 13) {
                System.out.println(u.getCohesionFactor());
                System.out.println(u.fullVertexSet);
                System.out.println(localQeg);
                Constants.SPECIAL_REGION_PRINT = true;
            }
            */
            if (localQeg.V.size() != 0) {
                ModifiedPruneExplore solve = new ModifiedPruneExplore(localQeg, u.getCohesionFactor(), Q);
            }
            /*
            if (u.getTreeNodeId() == 13) {
                Constants.SPECIAL_REGION_PRINT = false;
            }
            */
        }
        return;
    }

    public double maxDescendentScore(TreeNode u) {

        if (u == CLTree.root) {
            return 0.0;
        }
        double score = 0.0;

        double cohesivenessScore = (Constants.BETA * u.getkMax()) / Main.maxDegree;

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

        infScore = ((1.0 - Constants.BETA) * infScore) / Main.numVertices;

        if (infScore != 0.0) {
            score = cohesivenessScore + infScore;
        }

        return score;
    }

    public double maxNodeScore(TreeNode u) {

        if (u == CLTree.root) {
            return 0.0;
        }
        double score = 0.0;

        double cohesivenessScore = (Constants.BETA * u.getCohesionFactor()) / Main.maxDegree;

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

        infScore = ((1.0 - Constants.BETA) * infScore) / Main.numVertices;

        if (infScore != 0.0) {
            score = cohesivenessScore + infScore;
        }

        return score;
    }
}
