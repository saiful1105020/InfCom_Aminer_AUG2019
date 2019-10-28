/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package li_vs_us;

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

    public static void retrieveOriginalGraph() {
        for (int i = 1; i <= Constants.MAX_AUTH_ID; i++) {
            Author author = Main.authors[i];
            //keep a copy
            author.retrieveKeywordMaps();
        }
    }

    public static void removeKeywords(int targetKeyword) {
        //keyword can be chosen random, 100 runs for reporting the average.
        //update graph
        for (int i = 1; i <= Constants.MAX_AUTH_ID; i++) {
            Author author = Main.authors[i];
            //keep a copy
            author.copyKeywordMaps();
            //remove keywords
            author.convertToSingleKeyword(targetKeyword);
        }
        //update inverted list?
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

        //Change Dataset
        KICQ.k_min = Constants.K_MIN;
        KICQ.r = Constants.TOP_R;
        int numQueries = 25;

        String dirArray[] = {"Graphs/10K/", "Graphs/50K/", "Graphs/100K/", "Graphs/500K/", "Graphs/1M/"};
        int authorCount[] = {10000, 50000, 100000, 500000, 1000000};
        //String dirArray[] = {"Graphs/50K/"};
        //int authorCount[] = {50000};
        int queryKeywords[] = new int[numQueries];
        Random rand = new Random(System.currentTimeMillis());
        for (int i = 0; i < numQueries; i++) {
            int randomKeyId = 1 + rand.nextInt(Constants.NUM_KEYWORDS);
            queryKeywords[i] = randomKeyId;
            //System.out.println(queryKeywords[i]);
        }

        for (int i = 0; i < dirArray.length; i++) {
            System.out.println("Dataset: " + dirArray[i]);
            authors = new Author[authorCount[i] + 1];
            numVertices = authorCount[i] - Constants.MIN_AUTH_ID + 1;
            Constants.MAX_AUTH_ID = authorCount[i];

            //Read already computed graph
            readGraph(dirArray[i]);
            GlobalInvertedList.loadFromFile(dirArray[i]);

            long startTime, endTime, totalTime;
            int actualInstances = 0;

            double timeBasic = 0;
            double timePrune = 0;
            double timeOnline = 0;

            double ccBasic = 0;
            double ccLi = 0;
            double degreeBasic = 0;
            double degreeLi = 0;
            double densityBasic = 0;
            double densityLi = 0;
            double diameterBasic = 0;
            double diameterLi = 0;

            double cmfBasic = 0;
            double cmfLi = 0;
            double cpjBasic = 0;
            double cpjLi = 0;

            for (int q = 0; q < numQueries; q++) {
                System.out.println("Query: " + (q + 1));
                //System.out.println(queryKeywords[q]);
                //Generate single keyword query
                Constants.TARGET_KEYWORD_ID = queryKeywords[q];
                //Reform graph for a single keyword
                removeKeywords(Constants.TARGET_KEYWORD_ID);
                //Formulate query as per our definition
                KICQ kicq = new KICQ(true);

                startTime = System.nanoTime();
                BasicExplore solve1 = new BasicExplore(kicq);
                endTime = System.nanoTime();

                //test if valid keyword
                if (solve1.qeg.V.size() == 0) {
                    System.out.println("Invalid Keyword");
                    continue;
                }

                Community topBasic = solve1.Q.peek();
                if (topBasic.getvSet().size() == 0) {
                    System.out.println("No result found");
                    continue;
                }

                /*
                for(int u:topBasic.getvSet())
                {
                    System.out.println(Main.authors[u].getKeywordScore().keySet().size());
                }
                */
                
                //Otherwise record runtimes
                actualInstances++;
                totalTime = (endTime - startTime) / (1000000);
                timeBasic += totalTime;

                double cc = topBasic.clusteringCoeff();
                double degree = topBasic.degree();
                double diameter = topBasic.diameter();
                double density = topBasic.density();

//                System.out.println(cc);
//                System.out.println(degree);
//                System.out.println(diameter);
//                System.out.println(density);
                ccBasic += cc;
                degreeBasic += degree;
                diameterBasic += diameter;
                densityBasic += density;

                startTime = System.nanoTime();
                PruneAndExplore solve2 = new PruneAndExplore(kicq);
                endTime = System.nanoTime();
                totalTime = (endTime - startTime) / (1000000);
                timePrune += totalTime;

                //get k value that will be input to Li's algorithm (OnlineAll)
                int k = solve2.Q.peek().getK();

                startTime = System.nanoTime();
                OnlineAll solve3 = new OnlineAll(kicq, k);
                endTime = System.nanoTime();
                totalTime = (endTime - startTime) / (1000000);
                timeOnline += totalTime;

                Community topLi = solve3.Q.get(0);
                cc = topLi.clusteringCoeff();
                degree = topLi.degree();
                diameter = topLi.diameter();
                density = topLi.density();

//                System.out.println(cc);
//                System.out.println(degree);
//                System.out.println(diameter);
//                System.out.println(density);
                ccLi += cc;
                degreeLi += degree;
                diameterLi += diameter;
                densityLi += density;

                //back to original graph
                retrieveOriginalGraph();
                
                double cmf = topBasic.CMF(kicq, solve1.qeg);
                double cpj = topBasic.CPJ(kicq);
                cmfBasic += cmf;
                cpjBasic += cpj;
                cmf = topLi.CMF(kicq, solve3.qeg);
                cpj = topLi.CPJ(kicq);
                cmfLi += cmf;
                cpjLi += cpj;
            }

            System.out.println("BASIC Runtime: " + ((double) timeBasic) / (actualInstances) + " ms");
            System.out.println("PRUNE Runtime: " + ((double) timePrune) / (actualInstances) + " ms");
            System.out.println("OnlineAll Runtime: " + ((double) timeOnline) / (actualInstances) + " ms");

            System.out.println("BASIC CC: " + ((double) ccBasic) / (actualInstances));
            System.out.println("OnlineAll CC: " + ((double) ccLi) / (actualInstances));
            System.out.println("BASIC DEGREE: " + ((double) degreeBasic) / (actualInstances));
            System.out.println("OnlineAll DEGREE: " + ((double) degreeLi) / (actualInstances));
            System.out.println("BASIC Diameter: " + ((double) diameterBasic) / (actualInstances));
            System.out.println("OnlineAll Diameter: " + ((double) diameterLi) / (actualInstances));
            System.out.println("BASIC Density: " + ((double) densityBasic) / (actualInstances));
            System.out.println("OnlineAll Density: " + ((double) densityLi) / (actualInstances));

            System.out.println("BASIC CMF: " + ((double) cmfBasic) / (actualInstances));
            System.out.println("OnlineAll CMF: " + ((double) cmfLi) / (actualInstances));
            System.out.println("BASIC CPJ: " + ((double) cpjBasic) / (actualInstances));
            System.out.println("OnlineAll CPJ: " + ((double) cpjLi) / (actualInstances));
        }
    }
}
