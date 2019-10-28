/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package li_vs_us;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author MSI
 */
public class KICQ {

    public static int k_min = Constants.K_MIN;
    public static int r = Constants.TOP_R;

    Query query;
    
    int predicate;
    int n;
    ArrayList<Integer>[] keywords = null;

    //single keyword, predicate doesn't matter
    public KICQ(boolean removeKeyword)
    {
        this.keywords = new ArrayList[1];
        ArrayList<Integer> singleKeyword = new ArrayList<>();
        singleKeyword.add(Constants.TARGET_KEYWORD_ID);
        this.keywords[0] = singleKeyword;
        this.predicate = Constants.OR_PREDICATE;
        this.n = 1;
    }
    
    public KICQ(ArrayList<Integer>[] keywords, int predicate)
    {
        this.keywords = keywords;
        this.n = keywords.length;
        this.predicate = predicate;
    }
    
    public KICQ(Query query) {
        this.query = query;
        this.predicate = query.predicate;
        
        JSONObject jo;
        jo = AugmentQuery.augment(query.terms);
        
        this.n = jo.keySet().size();
        
        keywords = new ArrayList[n];
        
        int index=0;
        for (Object object : jo.keySet()) {
            keywords[index] = new ArrayList<Integer>();
            String keywordStr = (String) object;
            JSONArray jKeywordIds = (JSONArray) jo.get(keywordStr);
            for (int i = 0; i < jKeywordIds.size(); i++) {
                int keywordId = (int) (long) jKeywordIds.get(i);
                keywords[index].add(keywordId);
            }
            index++;
        }
        
    }

    @Override
    public String toString() {
        String keywordStr = "";
        
        for(int i=0;i<n;i++)
        {
            keywordStr+=keywords[i]+";";
        }
        
        return n + "\n" + keywordStr;
    }
    
    

}
