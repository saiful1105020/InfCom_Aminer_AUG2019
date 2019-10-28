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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
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
    public static Author authors[];
    public static int numArticles = 0;
    public static int maxDegree = 0;
    public static int maxK = 0;
    public static int numVertices;

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
                    int kid = Integer.parseInt(keyId.trim());
                    if (kid > Constants.NUM_KEYWORDS) {
                        continue;
                    }
                    article.addKeyword(kid);
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

    public static void readGraph(String dir) {
        long totalDegree = 0;
        long totalNodes = 0;

        try {
            //read from Graph.txt
            Scanner input = new Scanner(new File(dir + "Graph.txt"));
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

    public static void storeGraphToFile(String filename) {
        FileWriter fw = null;

        try {
            fw = new FileWriter(new File(filename));
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
    }

    public static void main(String[] args) {
        //Load Config
        try {
            Scanner config = new Scanner(new File("CONFIG.txt"));
            while (config.hasNext()) {
                String key = config.next();
                String value = config.next();

                switch (key) {
                    case "INPUT_DIR":
                        Constants.INPUT_DIR = value;
                        break;
                    case "MIN_AUTH_ID":
                        Constants.MIN_AUTH_ID = Integer.parseInt(value);
                        break;
                    case "MAX_AUTH_ID":
                        Constants.MAX_AUTH_ID = Integer.parseInt(value);
                        break;
                    case "NUM_KEYWORDS":
                        Constants.NUM_KEYWORDS = Integer.parseInt(value);
                        break;
                    case "MATCHING_THRESHOLD":
                        Constants.MATCHING_THRESHOLD = Double.parseDouble(value);
                        break;
                    case "K_MIN":
                        Constants.K_MIN = Integer.parseInt(value);
                        break;
                    case "TOP_R":
                        Constants.TOP_R = Integer.parseInt(value);
                        break;
                    case "BETA":
                        Constants.BETA = Double.parseDouble(value);
                        break;
                    case "LOAD_GRAPH":
                        if (value.equals("false")) {
                            Constants.LOAD_GRAPH = false;
                        } else {
                            Constants.LOAD_GRAPH = true;
                        }
                        break;
                    case "COMPUTE_CL_TREE":
                        if (value.equals("false")) {
                            Constants.COMPUTE_CL_TREE = false;
                        } else {
                            Constants.COMPUTE_CL_TREE = true;
                        }
                        break;
                    case "SHOW_OUTPUT":
                        if (value.equals("false")) {
                            Constants.SHOW_OUTPUT = false;
                        } else {
                            Constants.SHOW_OUTPUT = true;
                        }
                        break;
                    case "DEBUG_MODE":
                        if (value.equals("false")) {
                            Constants.DEBUG_MODE = false;
                        } else {
                            Constants.DEBUG_MODE = true;
                        }
                        break;
                    case "RUNS":
                        Constants.RUNS = Integer.parseInt(value);
                        break;
                    default:
                        System.err.println("Error loading config");
                        System.exit(0);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        int numKeywords[] = {100, 250, 500, 750, 1000};
        for (int x = 0; x < numKeywords.length; x++) {
            System.out.println("Total keywords: " + numKeywords[x]);
            authors = new Author[Constants.MAX_AUTH_ID + 1];
            numVertices = Constants.MAX_AUTH_ID - Constants.MIN_AUTH_ID + 1;
            Constants.NUM_KEYWORDS = numKeywords[x];

            File directory = new File(numKeywords[x] + "Keywords");
            if (!directory.exists()) {
                directory.mkdir();
            }

            String dir = numKeywords[x] + "Keywords/";
            String fileName;
            loadGraph();
            fileName = dir + "invertedList.txt";
            GlobalInvertedList.compute(fileName);
            fileName = dir + "Graph.txt";
            storeGraphToFile(fileName);

            CLTree.buildTree(dir);
            CLTree.loadInvertedList(dir);
            maxK = CLTree.root.getkMax();
            System.out.println("-----------");
        }
    }
}
