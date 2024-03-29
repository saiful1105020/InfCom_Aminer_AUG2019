/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package li_vs_us;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author MSI
 */
public class Author {
    private int id;
    
    private HashMap<Integer, Integer> coAuthorPaperCounts = new HashMap<Integer, Integer>();
    private HashMap<Integer, CountPair> keywordCounts = new HashMap<Integer, CountPair>();
    private HashMap<Integer, Double> keywordScore = new HashMap<Integer, Double>();
    
    private HashMap<Integer, CountPair> keywordCountsCopy = new HashMap<Integer, CountPair>();
    private HashMap<Integer, Double> keywordScoreCopy = new HashMap<Integer, Double>();
    
    public Author(int id) {
        this.id = id;
    }

    public HashMap<Integer, CountPair> getKeywordCounts() {
        return keywordCounts;
    }

    public void setKeywordCounts(HashMap<Integer, CountPair> keywordCounts) {
        this.keywordCounts = keywordCounts;
    }

    public HashMap<Integer, Double> getKeywordScore() {
        return keywordScore;
    }

    public void setKeywordScore(HashMap<Integer, Double> keywordScore) {
        this.keywordScore = keywordScore;
    }

    public void copyKeywordMaps()
    {
        //shallow copy is fine
        for(int keyId:keywordCounts.keySet())
        {
            CountPair cp = keywordCounts.get(keyId);
            double score = keywordScore.get(keyId);
            keywordCountsCopy.put(keyId, cp);
            keywordScoreCopy.put(keyId, score);
        }
    }
    
    public void retrieveKeywordMaps()
    {
        keywordCounts.clear();
        keywordScore.clear();
        keywordCounts = keywordCountsCopy;
        keywordScore = keywordScoreCopy;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<Integer, Integer> getCoAuthorPaperCounts() {
        return coAuthorPaperCounts;
    }

    public long getKeywordCitationCount(int keyword)
    {
        if(this.keywordCounts.containsKey(keyword))
        {
            return this.keywordCounts.get(keyword).getCitationCount();
        }
        return 0;
    }
    
    public double getKeywordScore(int keyword)
    {
        if(this.keywordScore.containsKey(keyword))
        {
            return this.keywordScore.get(keyword);
        }
        return 0;
    }
    
    public void setCoAuthorPaperCounts(HashMap<Integer, Integer> coAuthorPaperCounts) {
        this.coAuthorPaperCounts = coAuthorPaperCounts;
    }
    
    public void addCoAuthor(int coAuthorId)
    {
        if(this.coAuthorPaperCounts.containsKey(coAuthorId))
        {
            int prevCount = coAuthorPaperCounts.get(coAuthorId);
            coAuthorPaperCounts.put(coAuthorId, prevCount+1);
        }
        else
        {
            coAuthorPaperCounts.put(coAuthorId, 1);
        }
    }
    
    public void addCoAuthorPaperCount(int coAuthorId, int paperCount)
    {
        coAuthorPaperCounts.put(coAuthorId, paperCount);
    }

    
    public void addKeywordCount(int keyword, Long citations)
    {
        if(this.keywordCounts.containsKey(keyword))
        {
            CountPair cp = this.keywordCounts.get(keyword);
            cp.incPaperCount();
            cp.addCitationCount(citations);
            this.keywordCounts.put(keyword, cp);
        }
        else
        {
            CountPair cp = new CountPair(1, citations);
            this.keywordCounts.put(keyword, cp);
        }
    }
    
    public void setKeywordPaperCitationCount(int keyword, int paperCount, long citations)
    {
        CountPair cp = new CountPair(paperCount,citations);
        this.keywordCounts.put(keyword, cp);
    }
    
    public void convertToSingleKeyword(int keywordId)
    {
        boolean hasKeyword = this.keywordCounts.containsKey(keywordId);
        double score=0;
        CountPair count=null;
        if(hasKeyword)
        {
            score = this.keywordScore.get(keywordId);
            count = this.keywordCounts.get(keywordId);
        }
        
        this.keywordCounts.clear();
        this.keywordScore.clear();
        
        if(hasKeyword)
        {
            this.keywordCounts.put(keywordId, count);
            this.keywordScore.put(keywordId, score);
        }
        
    }

    @Override
    public String toString() {
        return "Author{" + "id=" + id + ", coAuthorPaperCounts=" + coAuthorPaperCounts + paperAndCitationCounts() + '}';
    }
    
    public String paperAndCitationCounts(){
        String pstr=", keywordPaperCount=";
        String cstr = ", keywordCitationCount=";
        for(int i=0;i<Constants.NUM_KEYWORDS;i++)
        {
            if(this.keywordCounts.containsKey(i))
            {
                pstr+=i+": "+this.keywordCounts.get(i).paperCount+"\t";
                cstr+=i+": "+this.keywordCounts.get(i).citationCount+"\t";
            }
        }
        return pstr+cstr;
    }
    
    public JSONObject toJSON(){
        JSONObject jo = new JSONObject();
        jo.put("author-id", this.id);
        
        JSONArray ja = new JSONArray();
        
        ArrayList<Integer> coAuthors = new ArrayList(this.coAuthorPaperCounts.keySet());
        for(int i=0;i<this.coAuthorPaperCounts.size();i++)
        {
            int coAuthorId = coAuthors.get(i);
            Map m = new LinkedHashMap(2);
            m.put("coauth-id", coAuthorId);
            m.put("paper-count", this.coAuthorPaperCounts.get(coAuthorId));
            ja.add(m);
        }
        jo.put("co-authors", ja);
        
        ja = new JSONArray();
        
        
        for(int keywordId:keywordCounts.keySet())
        {
            CountPair cp = this.keywordCounts.get(keywordId);
            Map m = new LinkedHashMap(3);
            m.put("keyword-id",keywordId);
            m.put("paper-count", cp.getPaperCount());
            m.put("citations", cp.getCitationCount());
            m.put("score", this.keywordScore.get(keywordId));
            ja.add(m);
        }
        jo.put("keywords", ja);
        
        return jo;
    }
    
    
    public void setKeywordScore(int keyword, double score)
    {
        this.keywordScore.put(keyword, score);
    }

}
