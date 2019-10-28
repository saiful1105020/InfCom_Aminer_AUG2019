/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package li_vs_us;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author MSI
 */
public class Node {

    int id;
    Set<Integer> adjList = new LinkedHashSet<Integer>();
    double score;

    public Node (Node node)
    {
        this.id = node.id;
        this.adjList = new LinkedHashSet<>(node.adjList);
        this.score = node.score;
    }
    public Node(int id, KICQ kicq) {
        this.id = id;
        this.adjList = new LinkedHashSet<Integer>(Main.authors[id].getCoAuthorPaperCounts().keySet());
        this.score = 0.0;

        int n = kicq.keywords.length;
        for (int i = 0; i < n; i++) {
            double termScore = 0.0;
            for (int j = 0; j < kicq.keywords[i].size(); j++) {
                int keywordId = kicq.keywords[i].get(j);
                termScore = Math.max(termScore, Main.authors[id].getKeywordScore(keywordId));
            }

            if (i == 0) {
                this.score = termScore;
            } else {
                if (kicq.predicate == Constants.AND_PREDICATE) {
                    this.score = Math.min(this.score, termScore);
                }
                else{
                    this.score = Math.max(this.score, termScore);
                }
            }

        }
    }

    public int getDegree()
    {
        return this.adjList.size();
    }
    
    @Override
    public String toString() {
        return "Node{" + "id=" + id + ", adjList=" + adjList + ", score=" + score + '}';
    }

    
}
