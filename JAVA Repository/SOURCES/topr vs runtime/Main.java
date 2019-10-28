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

        int numQueries = 100;

        double runtimes[][][] = new double[2][11][3];
        //first -> AND, OR; second -> r value; third -> algorithm: basic, prune, tree
        
        for (int k = 1; k <= 10; k++) {
            Constants.TOP_R = k;
            KICQ.r = Constants.TOP_R;
            System.out.println("r: " + k);
            startTime = System.nanoTime();
            for (int i = 0; i < numQueries; i++) {
                BasicExplore solve = new BasicExplore(autoQuery.queries[0][i]);
                //Community c = solve.Q.peek();
                //System.out.println(c.keywordCohesiveness(solve.kicq, solve.qeg));
                //System.exit(0);
            }
            endTime = System.nanoTime();
            totalTime = (endTime - startTime) / (1000000);
            runtimes[0][k][0] = ((double) totalTime) / (numQueries);

            //System.out.println("k_min: "+k);
            startTime = System.nanoTime();
            for (int i = 0; i < numQueries; i++) {
                PruneAndExplore solve = new PruneAndExplore(autoQuery.queries[0][i]);
            }
            endTime = System.nanoTime();
            totalTime = (endTime - startTime) / (1000000);
            runtimes[0][k][1] = ((double) totalTime) / (numQueries);

            startTime = System.nanoTime();
            for (int i = 0; i < numQueries; i++) {
                TreeExplore solve = new TreeExplore(autoQuery.queries[0][i]);
            }
            endTime = System.nanoTime();
            totalTime = (endTime - startTime) / (1000000);
            runtimes[0][k][2] = ((double) totalTime) / (numQueries);

            startTime = System.nanoTime();
            for (int i = 0; i < numQueries; i++) {
                BasicExplore solve = new BasicExplore(autoQuery.queries[1][i]);
            }
            endTime = System.nanoTime();
            totalTime = (endTime - startTime) / (1000000);
            runtimes[1][k][0] = ((double) totalTime) / (numQueries);

            //System.out.println("k_min: "+k);
            startTime = System.nanoTime();
            for (int i = 0; i < numQueries; i++) {
                PruneAndExplore solve = new PruneAndExplore(autoQuery.queries[1][i]);
            }
            endTime = System.nanoTime();
            totalTime = (endTime - startTime) / (1000000);
            runtimes[1][k][1] = ((double) totalTime) / (numQueries);

            startTime = System.nanoTime();
            for (int i = 0; i < numQueries; i++) {
                TreeExplore solve = new TreeExplore(autoQuery.queries[1][i]);
            }
            endTime = System.nanoTime();
            totalTime = (endTime - startTime) / (1000000);
            runtimes[1][k][2] = ((double) totalTime) / (numQueries);
        }
        
        try {
            FileWriter fw = new FileWriter(new File("runtimeVsTopr.txt"));
            for(int l1=0;l1<runtimes.length;l1++)
            {
                for(int l2=0; l2<runtimes[l1].length;l2++)
                {
                    for(int l3=0; l3<runtimes[l1][l2].length;l3++)
                    {
                        fw.write("Type: "+l1+"\n");
                        fw.write("r: "+l2+"\n");
                        fw.write("Algorithm: "+l3+"\n");
                        fw.write("Runtime: "+runtimes[l1][l2][l3]+"\n");
                        fw.flush();
                    }
                }
            }
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
