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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
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
        //Auto generated queries saved in file
        QueryGenerator autoQuery = new QueryGenerator();

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

        KICQ.k_min = Constants.K_MIN;
        KICQ.r = Constants.TOP_R;

        authors = new Author[Constants.MAX_AUTH_ID + 1];
        numVertices = Constants.MAX_AUTH_ID - Constants.MIN_AUTH_ID + 1;

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

        long startTime, endTime, totalTime;
        int runs = Constants.RUNS;

        CLTree.buildTree();
        CLTree.loadInvertedList();
        maxK = CLTree.root.getkMax();

        startTime = System.nanoTime();
        double totalDensity = 0.0;
        double totalDiameter = 0.0;
        double CC = 0.0;
        double totalComDegree = 0.0;
<<<<<<< HEAD
        int instances = 0;
        int numQueries = 100;
        
        if (Constants.SHOW_OUTPUT == true) {
            System.err.println("OUTPUT MUST BE DISABLED TO EVALUATE COMMUNITIES");
            System.exit(0);
        }
        
        for (int i = 0; i < numQueries; i++) {
            BasicExplore solve2 = null;

            for (int run = 0; run < runs; run++) {
                solve2 = new BasicExplore(autoQuery.queries[0][i]);
            }

            for (int r = 0; r < KICQ.r; r++) {
                Community c = solve2.Q.remove();
                double t = c.density();
                if (t > 0.0) {
                    double cc = c.clusteringCoeff();
                    double cd = c.degree();
                    double dm = c.diameter();
                    totalDensity += t;
                    totalDiameter+=dm;
                    CC += cc;
                    totalComDegree+=cd;
                    instances++;
                }
            }

        }

        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / (1000000);
        System.out.println("OR Query>>");
        System.out.println("BASIC: " + ((double) totalTime) / (runs * numQueries) + " ms");
        System.out.println("Degree: " + ((double) totalComDegree) / instances);
        System.out.println("Diameter: " + ((double) totalDiameter) / instances);
        System.out.println("Density: " + ((double) totalDensity) / instances);
        System.out.println("Clustering Coefficient: " + ((double) CC) / instances);
        

        startTime = System.nanoTime();
        for (int i = 0; i < numQueries; i++) {
            for (int run = 0; run < runs; run++) {
                PruneAndExplore solve = new PruneAndExplore(autoQuery.queries[0][i]);
            }
=======
        double totalScore = 0.0;
        int instances = 0;
        int numQueries = 25;

        if (Constants.SHOW_OUTPUT == true) {
            System.err.println("OUTPUT MUST BE DISABLED TO EVALUATE COMMUNITIES");
            System.exit(0);
        }

        String OUT_STAT_FILE = "betaVsQuality.txt";
        FileWriter fw = null;
        
        try {
            fw = new FileWriter(new File(OUT_STAT_FILE));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        /*
        Map<Double, Double> betaDensity = new HashMap<>();
        Map<Double, Double> betaDiameter = new HashMap<>();
        Map<Double, Double> betaCC = new HashMap<>();
        Map<Double, Double> betaDegree = new HashMap<>();
        */
        
        double defBeta = Constants.BETA;
        try {
            fw.write("OR Queries: \n");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
>>>>>>> Quality Measures for Varying Beta Values
        }
        for (double beta = 0.00; beta <= 1.001; beta += 0.01) {
            Constants.BETA = beta;
            for (int i = 0; i < numQueries; i++) {
                PruneAndExplore solve = new PruneAndExplore(autoQuery.queries[0][i]);
                for (int r = 0; r < KICQ.r; r++) {
                    Community c = solve.Q.remove();
                    double t = c.density();
                    if (t > 0.0) {
                        //double cc = c.clusteringCoeff();
                        double cd = c.degree();
                        //double dm = c.diameter();
                        totalDensity += t;
                        //totalDiameter += dm;
                        //CC += cc;
                        totalComDegree += cd;
                        totalScore+=c.avgInfScore(solve.qeg);
                        instances++;
                    }

                }
            }
<<<<<<< HEAD
            totalAccess += TreeExplore.nodesAccessed;
        }
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / (1000000);
        System.out.println("TREE: " + ((double) totalTime) / (runs * numQueries) + " ms");
        System.out.println("Nodes Accessed: " + ((double) totalAccess) / (numQueries));

        startTime = System.nanoTime();
        totalComDegree = 0.0;
        totalDiameter = 0.0;
        totalDensity = 0.0;
        CC = 0.0;
        instances = 0;
        for (int i = 0; i < numQueries; i++) {
            BasicExplore solve2 = null;

            for (int run = 0; run < runs; run++) {
                solve2 = new BasicExplore(autoQuery.queries[1][i]);
            }

            for (int r = 0; r < KICQ.r; r++) {
                Community c = solve2.Q.remove();
                double t = c.density();
                if (t > 0.0) {
                    double cc = c.clusteringCoeff();
                    double cd = c.degree();
                    double dm = c.diameter();
                    totalDensity += t;
                    totalDiameter+=dm;
                    CC += cc;
                    totalComDegree+=cd;
                    instances++;
                }
            }
        }

        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / (1000000);
        System.out.println("AND Query >>");
        System.out.println("BASIC: " + ((double) totalTime) / (runs * numQueries) + " ms");
        System.out.println("Degree: " + ((double) totalComDegree) / instances);
        System.out.println("Diameter: " + ((double) totalDiameter) / instances);
        System.out.println("Density: " + ((double) totalDensity) / instances);
        System.out.println("Clustering Coefficient: " + ((double) CC) / instances);

        startTime = System.nanoTime();
        for (int i = 0; i < numQueries; i++) {
            for (int run = 0; run < runs; run++) {
=======
            
            try
            {
                fw.write("Beta: "+beta+"\n");
                fw.write("Degree: " + ((double) totalComDegree) / instances+"\n");
                fw.write("Density: " + ((double) totalDensity) / instances+"\n");
                fw.write("Average Score: " + ((double) totalScore) / instances+"\n");
                fw.flush();
            }
            catch(IOException e)
            {
                System.err.print(e);
            }
            
            totalComDegree = 0;
            totalDensity = 0;
            totalDiameter = 0;
            totalScore = 0;
            CC = 0;
            instances = 0;
        }
        
        try {
            fw.write("AND Queries: \n");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (double beta = 0.0; beta <= 1.001; beta += 0.01) {
            Constants.BETA = beta;
            for (int i = 0; i < numQueries; i++) {
>>>>>>> Quality Measures for Varying Beta Values
                PruneAndExplore solve = new PruneAndExplore(autoQuery.queries[1][i]);
                for (int r = 0; r < KICQ.r; r++) {
                    Community c = solve.Q.remove();
                    double t = c.density();
                    if (t > 0.0) {
                        //double cc = c.clusteringCoeff();
                        double cd = c.degree();
                        //double dm = c.diameter();
                        totalDensity += t;
                        //totalDiameter += dm;
                        //CC += cc;
                        totalComDegree += cd;
                        totalScore+=c.avgInfScore(solve.qeg);
                        instances++;
                    }
                }
            }
            
            try
            {
                fw.write("Beta: "+beta+"\n");
                fw.write("Degree: " + ((double) totalComDegree) / instances+"\n");
                //fw.write("Diameter: " + ((double) totalDiameter) / instances+"\n");
                fw.write("Density: " + ((double) totalDensity) / instances+"\n");
                fw.write("Average Score: " + ((double) totalScore) / instances+"\n");
                fw.flush();
            }
            catch(IOException e)
            {
                System.err.print(e);
            }
            
            totalComDegree = 0;
            totalDensity = 0;
            totalDiameter = 0;
            CC = 0;
            totalScore = 0;
            instances = 0;
        }
<<<<<<< HEAD
        endTime = System.nanoTime();
        totalTime = (endTime - startTime) / (1000000);
        System.out.println("TREE: " + ((double) totalTime) / (runs * numQueries) + " ms");

        System.out.println("Nodes Accessed: " + ((double) totalAccess) / (numQueries));
=======
        
        try {
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        Constants.BETA = defBeta;
>>>>>>> Quality Measures for Varying Beta Values
    }
}
