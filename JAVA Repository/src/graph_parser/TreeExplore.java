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
    Set<Integer> relevantTreeNodes = new LinkedHashSet<Integer>();
    //QEG qeg;

    PriorityQueue<Community> Q;

    public TreeExplore(KICQ kicq) {
        this.kicq = kicq;

        for (int i = 0; i < kicq.keywords.length; i++) {
            Set<Integer> termNodes = new LinkedHashSet<>();
            for (int j : kicq.keywords[i]) {
                termNodes.addAll(CLTree.invertedList[j]);
            }

            if (kicq.predicate == Constants.AND_PREDICATE) {
                if (i == 0) {
                    relevantTreeNodes = termNodes;
                } else {
                    relevantTreeNodes.retainAll(termNodes);
                }
            } else {
                relevantTreeNodes.addAll(termNodes);
            }
        }
        
        //System.out.println(relevantTreeNodes);

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
        Set<Integer> testSet = new LinkedHashSet<>();
        
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
                testSet.addAll(termVertices);
            } else {
                if (i == 0) {
                    testSet = new LinkedHashSet<Integer>(termVertices);
                } else {
                    testSet.retainAll(termVertices);
                }
            }
        }
        
        for (TreeNode child : u.childNodes) {
            testSet.addAll(decompressVertices(child));
        }
        
        return testSet;
    }

    public void visitTree(TreeNode u) {

        if (u.getCohesionFactor() < (Constants.K_MIN-1)) {
            for (TreeNode v : u.childNodes) {
                if (relevantTreeNodes.contains(v.getTreeNodeId())) {
                    visitTree(v);
                }
            }
            return;
        }
        
        //Ensures k is at least (k_min - 1). So children are k_min cores
        
        double maxDesScore = maxDescendentScore(u);

        double rTopScore = this.Q.peek().getScore();

        
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
                u.exclusiveVertexSet.addAll(termVertices);
            } else {
                if (i == 0) {
                    u.exclusiveVertexSet = new LinkedHashSet<Integer>(termVertices);
                } else {
                    u.exclusiveVertexSet.retainAll(termVertices);
                }
            }
        }

        
        if (maxDesScore > rTopScore) {
            for (TreeNode v : u.childNodes) {
                if (relevantTreeNodes.contains(v.getTreeNodeId())) {
                    visitTree(v);
                    //u.fullVertexSet.addAll(v.fullVertexSet);
                    v.exclusiveVertexSet.clear();
                }
            }
        }
        
        if(u.exclusiveVertexSet.size()==0)
        {
            return;
        }
        
        //update max node score

        double maxNodeScore = maxNodeScore(u);

        rTopScore = this.Q.peek().getScore();

        if (u.getCohesionFactor() >= KICQ.k_min && maxNodeScore > rTopScore) {
            nodesAccessed++;

            QEG localQeg = new QEG(kicq, new LinkedHashSet(decompressVertices(u)));

            //DEBUG CODE FOR A PARTICULAR TREE NODE
            /*
            if (u.getTreeNodeId() == 13) {
                System.out.println(u.getCohesionFactor());
                System.out.println(u.fullVertexSet);
                System.out.println(localQeg);
                Constants.SPECIAL_REGION_PRINT = true;
            }
             */
            
            int k_max = u.getCohesionFactor();
            
            int localMaxDegree = 0;
            for(int v:u.exclusiveVertexSet)
            {
                if(localQeg.vertexDegree.containsKey(v))
                {
                    int deg = localQeg.vertexDegree.get(v);
                    if(localMaxDegree<deg)
                    {
                        localMaxDegree = deg;
                    }
                }
            }
            if(k_max>localMaxDegree)
            {
                k_max = localMaxDegree;
            }
            
            if (localQeg.V.size() != 0) {
                ModifiedPruneExplore solve = new ModifiedPruneExplore(localQeg, k_max, Q);
            }
        }
        return;
    }

    public double maxDescendentScore(TreeNode u) {
        if (u == CLTree.root) {
            return 1.0;
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
