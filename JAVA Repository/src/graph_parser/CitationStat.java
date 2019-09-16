/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author MSI
 */
public class CitationStat {

    int keywordId;
    //int totalCitations = 0;
    double avgCitations = 0.0;
    int minCitations = 0;
    int maxCitations = 0;

    ArrayList<Integer> allCitations = new ArrayList<Integer>();
    HashMap<Integer, Double> citationPercentile = new HashMap<Integer, Double>();

    public CitationStat(int keywordId) {
        this.keywordId = keywordId;
        this.calculateStats();
    }

    public double getAvgCitations() {
        return avgCitations;
    }

    public void setAvgCitations(double avgCitations) {
        this.avgCitations = avgCitations;
    }

    public int getMinCitations() {
        return minCitations;
    }

    public void setMinCitations(int minCitations) {
        this.minCitations = minCitations;
    }

    public int getMaxCitations() {
        return maxCitations;
    }

    public void setMaxCitations(int maxCitations) {
        this.maxCitations = maxCitations;
    }

    
    public void calculateStats() {
        for (Integer authorId : GlobalInvertedList.IL[keywordId]) {
            allCitations.add((int) Main.authors[authorId].getKeywordCitationCount(keywordId));
        }

        int numAuthors = allCitations.size();
        
        Collections.sort(allCitations);
        for(int i=0;i<numAuthors;i++)
        {
            int citationCout = allCitations.get(i);
            double percentile = ((double)(i+1))/numAuthors;
            citationPercentile.put(citationCout, percentile);
        }
        
        if (numAuthors > 0) {
            minCitations = Collections.min(allCitations);
            maxCitations = Collections.max(allCitations);
            int totalCitations = 0;
            for (int i = 0; i < numAuthors; i++) {
                totalCitations += allCitations.get(i);
            }
            avgCitations = totalCitations / numAuthors;
        }
    }

    @Override
    public String toString() {
        return "citationStat{" + "keywordId=" + keywordId + ", avgCitations=" + avgCitations + ", minCitations=" + minCitations + ", maxCitations=" + maxCitations + '}';
    }

}
