/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

/**
 *
 * @author MSI
 */
public class Main {

    //public static ArrayList<Article> articles = new ArrayList<Article>();
    public static Author authors[] = new Author[Constants.MAX_AUTH_ID + 1];
    public static int numArticles = 0;
    public static int maxDegree = 0;

    /**
     * @param args the command line arguments
     */
    public static void readAllFiles(File folder) {
        File[] fileNames = folder.listFiles();

        for (File file : fileNames) {
            // if directory call the same method again
            if (file.isDirectory()) {
                readAllFiles(file);
            } else {
                try {
                    readArticle(file);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    public static void readArticle(File file) throws FileNotFoundException, ParseException {
        System.out.println("Reading File: " + file.getName());
        Scanner input = new Scanner(file);
        while (input.hasNext()) {
            Object obj = new JSONParser().parse(input.nextLine());
            JSONObject jo = (JSONObject) obj;
            String articleId = (String) jo.get("id");
            long citations = (Long) jo.get("citations");

            Article article = new Article(articleId);
            article.setCitations(citations);

            String authList[] = ((String) jo.get("authors")).replaceAll("\"", "").replace("[", "").replace("]", "").split(",");
            for (String authId : authList) {
                article.addAuthor(Integer.parseInt(authId.trim()));
            }

            String parsedKeyList = ((String) jo.get("keywords")).replaceAll("\"", "").replace("[", "").replace("]", "");
            if (parsedKeyList.length() != 0) {
                String keywordList[] = parsedKeyList.split(",");
                for (String keyId : keywordList) {
                    article.addKeyword(Integer.parseInt(keyId.trim()));
                }
            }

            ArrayList<Integer> articleAuthors = article.getAuthors();
            ArrayList<Integer> articleKeywords = article.getKeywords();

            for (int j = 0; j < articleAuthors.size(); j++) {
                Author articleAuthor = authors[articleAuthors.get(j)];
                for (int k = 0; k < articleAuthors.size(); k++) {
                    if (j == k) {
                        continue;
                    }
                    articleAuthor.addCoAuthor(articleAuthors.get(k));
                }

                for (int k = 0; k < articleKeywords.size(); k++) {
                    int keywordId = articleKeywords.get(k);
                    articleAuthor.addKeywordCount(keywordId, article.getCitations());
                }
            }
            numArticles++;
        }
    }

    public static void loadGraph() {
        File folder = new File(Constants.INPUT_DIR);

        for (int i = 0; i <= Constants.MAX_AUTH_ID; i++) {
            authors[i] = new Author(i);
        }

        readAllFiles(folder);

        System.out.println("Number of articles read: " + numArticles);

        //System.out.println(authors[1].toJSON().toString());
        //System.out.println(authors[1]);
    }

    public static void readGraph() {
        long totalDegree = 0;
        long totalNodes = 0;

        try {
            //read from Graph.txt
            Scanner input = new Scanner(new File("Graph.txt"));
            while (input.hasNext()) {
                Object obj = new JSONParser().parse(input.nextLine());
                JSONObject jo = (JSONObject) obj;
                Integer authorId = (int) ((long) jo.get("author-id"));

                Author author = new Author(authorId);
                JSONArray jCoAuths = (JSONArray) jo.get("co-authors");

                int degree = jCoAuths.size();
                if (degree > Main.maxDegree) {
                    Main.maxDegree = degree;
                }
                totalDegree += degree;
                totalNodes++;

                for (int i = 0; i < jCoAuths.size(); i++) {
                    JSONObject jCoAuth = (JSONObject) jCoAuths.get(i);
                    int coAuthorId = (int) ((long) jCoAuth.get("coauth-id"));
                    int paperCount = (int) ((long) jCoAuth.get("paper-count"));
                    author.addCoAuthorPaperCount(coAuthorId, paperCount);
                }

                JSONArray jKeywords = (JSONArray) jo.get("keywords");
                for (int i = 0; i < jKeywords.size(); i++) {
                    JSONObject jKeyword = (JSONObject) jKeywords.get(i);
                    int keywordId = (int) ((long) jKeyword.get("keyword-id"));
                    int citations = (int) ((long) jKeyword.get("citations"));
                    int paperCount = (int) ((long) jKeyword.get("paper-count"));
                    double score = (double) jKeyword.get("score");
                    author.setKeywordPaperCitationCount(keywordId, paperCount, citations);
                    author.setKeywordScore(keywordId, score);
                }

                authors[authorId] = author;
                //System.out.println(authors[authorId].toJSON());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }

        //System.out.println(authors[1].toJSON().toString());
        System.out.println("Number of nodes: " + totalNodes);
        System.out.println("Number of edges: " + totalDegree / 2);

    }

    public static void main(String[] args) {
        // TODO code application logic here

        if (Constants.LOAD_GRAPH) {
            //Compute graph from raw files
            loadGraph();
            GlobalInvertedList.compute();

            FileWriter fw = null;

            try {
                fw = new FileWriter(new File("Graph.txt"));
                for (int i = Constants.MIN_AUTH_ID; i <= Constants.MAX_AUTH_ID; i++) {

                    int degree = authors[i].getCoAuthorPaperCounts().keySet().size();
                    if (degree > Main.maxDegree) {
                        Main.maxDegree = degree;
                    }

                    fw.write(authors[i].toJSON().toJSONString());
                    fw.write("\n");
                    fw.flush();
                }
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else {
            //Read already computed graph
            readGraph();
            GlobalInvertedList.loadFromFile();
        }

        //Scanner input = new Scanner(System.in);
        int n = 0;
        ArrayList<String> queryTerms = new ArrayList<String>();
        /*
        System.out.println("Number of terms: ");

        try {
            n = Integer.parseInt(input.nextLine());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        for (int i = 0; i < n; i++) {
            System.out.println("Term " + i + " : ");
            queryTerms.add(input.nextLine());
            System.out.println(queryTerms.get(i));
        }
        */

        long startTime, endTime, totalTime;
        int runs = Constants.RUNS;

        
        queryTerms.add("database");
        queryTerms.add("data mining");
        int queryType = Constants.OR_PREDICATE;
        
        Query query = new Query(queryTerms, queryType);
        KICQ augmentedQuery = new KICQ(query);
         
        
        
        /*
        startTime = System.nanoTime();
        for(int run=0;run<runs;run++)
        {
            BasicExplore solve2 = new BasicExplore(augmentedQuery);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime)/(1000000);
        System.out.println("BASIC: "+((double)totalTime)/runs+" ms");
        
        startTime = System.nanoTime();
        for(int run=0;run<runs;run++)
        {
            PruneAndExplore solve = new PruneAndExplore(augmentedQuery);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime)/(1000000);
        System.out.println("PRUNE: "+((double)totalTime)/runs+" ms");
        */
        
        startTime = System.nanoTime();
        CLTree.buildTree();
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / (1000000);
        System.out.println("CL-tree with iList: " + totalTime + " ms");
        
        startTime = System.nanoTime();
        for(int run=0;run<runs;run++)
        {
            TreeExplore solve = new TreeExplore(augmentedQuery);
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime)/(1000000);
        System.out.println("TREE: "+((double)totalTime)/runs+" ms");
    }
}
