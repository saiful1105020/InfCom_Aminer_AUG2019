/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author MSI
 */
public class GlobalInvertedList {

    public static Set<Integer>[] IL = new LinkedHashSet[Constants.NUM_KEYWORDS+1];

    public static CitationStat[] citationStats = new CitationStat[Constants.NUM_KEYWORDS+1];
    public static String fileName = "invertedList.txt";

    public static void compute() {
        for (int i = 0; i < Constants.NUM_KEYWORDS; i++) {
            ArrayList<Integer> keywordAuthors = new ArrayList<Integer>();
            for (int j = Constants.MIN_AUTH_ID; j <= Constants.MAX_AUTH_ID; j++) {
                if (Main.authors[j].getKeywordCitationCount(i) > 0) {
                    keywordAuthors.add(j);
                }
            }
            //invertedList[i] = keywordAuthors;
            IL[i] = new LinkedHashSet<Integer>(keywordAuthors);
            citationStats[i] = new CitationStat(i);
            //System.out.println(i+": "+invertedList[i].toString());
        }

        storeIntoFile();

    }

    public static void loadFromFile() {
        try {
            Scanner input = new Scanner(new File(fileName));
            while (input.hasNext()) {
                Object obj = new JSONParser().parse(input.nextLine());
                JSONObject jo = (JSONObject) obj;

                int keywordId = (int) (long) jo.get("keyword");
                int totalAuthors = (int) (long) jo.get("total-authors");
                JSONArray jAuthors = (JSONArray) jo.get("authors");

                ArrayList<Integer> keywordAuthors = new ArrayList<Integer>();
                for (Object item : jAuthors) {
                    keywordAuthors.add((int) (long) item);
                }
                //invertedList[i] = keywordAuthors;
                IL[keywordId] = new LinkedHashSet<Integer>(keywordAuthors);
                citationStats[keywordId] = new CitationStat(keywordId);
                /*
                if (keywordId == 2) {
                    System.out.println(keywordId);
                    System.out.println(totalAuthors);
                    System.out.println(invertedList[keywordId]);
                    System.exit(0);
                }
                 */
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GlobalInvertedList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GlobalInvertedList.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void storeIntoFile() {
        try {
            FileWriter fw = new FileWriter(new File(fileName));
            for (int i = 0; i < Constants.NUM_KEYWORDS; i++) {
                JSONObject jo = new JSONObject();
                jo.put("keyword", i);
                jo.put("total-authors", IL[i].size());
                jo.put("authors", IL[i]);
                fw.write(jo.toJSONString());
                fw.write("\n");
                fw.flush();
            }
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(GlobalInvertedList.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
