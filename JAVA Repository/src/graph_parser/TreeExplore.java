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
    QEG qeg;

    PriorityQueue<Community> Q;

    public TreeExplore(KICQ kicq) {
        this.kicq = kicq;
        //find relevant CL-tree nodes
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
        
        //query essential graph with core decomposition
        this.qeg = new QEG(kicq);
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
        //algorithm
        visitTree(CLTree.root);
    }

    public Set<Integer> decompressVertices(TreeNode u) {
        Set<Integer> testSet = new LinkedHashSet<>(u.vertexSet);
        testSet.retainAll(qeg.V);
        
        for (TreeNode child : u.childNodes) {
            if(relevantTreeNodes.contains(child))
            {
                testSet.addAll(decompressVertices(child));
            }
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
<<<<<<< HEAD
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

=======
        double rTopScore = this.Q.peek().getScore();        
        
        //Get query relevant vertices in this tree-node
        u.exclusiveVertexSet = new LinkedHashSet<>(u.vertexSet);
        u.exclusiveVertexSet.retainAll(qeg.V);
>>>>>>> a76483a6ad32e53b151b699fe7defbb40cc6a79d
        
        if (maxDesScore > rTopScore) {
            for (TreeNode v : u.childNodes) {
                if (relevantTreeNodes.contains(v.getTreeNodeId())) {
                    visitTree(v);
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
<<<<<<< HEAD
            QEG localQeg = new QEG(kicq, new LinkedHashSet(decompressVertices(u)));

=======
>>>>>>> a76483a6ad32e53b151b699fe7defbb40cc6a79d
            int k_max = u.getCohesionFactor();
            
            int localMaxDegree = 0;
            int localMinDegree = Integer.MAX_VALUE;
            for(int v:u.exclusiveVertexSet)
            {
                if(qeg.vertexDegree.containsKey(v))
                {
                    int deg = qeg.vertexDegree.get(v);
                    if(localMaxDegree<deg)
                    {
                        localMaxDegree = deg;
                    }
                    if(localMinDegree>deg)
                    {
                        localMinDegree = deg;
                    }
                }
            }
            
            if(k_max>localMaxDegree)
            {
                k_max = localMaxDegree;
            }
            int k_min = Math.max(localMinDegree, KICQ.k_min);
            
<<<<<<< HEAD
            if (localQeg.V.size() != 0) {
                ModifiedPruneExplore solve = new ModifiedPruneExplore(localQeg, k_min, k_max, Q);
=======
            int k_min = Math.max(localMinDegree, KICQ.k_min);
            
            if (qeg.V.size() != 0) {
                ModifiedPruneExplore solve = new ModifiedPruneExplore(qeg, decompressVertices(u), k_min, k_max, Q);
>>>>>>> a76483a6ad32e53b151b699fe7defbb40cc6a79d
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
