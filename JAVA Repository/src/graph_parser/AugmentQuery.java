/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
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
public class AugmentQuery {

    public static JSONObject augment(ArrayList<String> terms) {
        JSONObject augmentedKeywords = null;

        ArrayList<String> pyArgs = new ArrayList<String>();
        pyArgs.add("python");
        pyArgs.add("queryEmbedding.py");
        pyArgs.add("\"" + Constants.NUM_KEYWORDS + "\"");
        pyArgs.add("\"" + Constants.MATCHING_THRESHOLD + "\"");
        pyArgs.add("\"" + terms.size() + "\"");

        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i);
            pyArgs.add("\"" + term + "\"");
        }

        ProcessBuilder pb = new ProcessBuilder(pyArgs);
        try {
            Process p = pb.start();
            System.out.println("Augmenting Query...");
            p.waitFor();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Scanner input = new Scanner(new File("query_response.txt"));
            Object obj = new JSONParser().parse(input.nextLine());
            JSONObject jo = (JSONObject) obj;
            augmentedKeywords = jo;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AugmentQuery.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(AugmentQuery.class.getName()).log(Level.SEVERE, null, ex);
        }

        return augmentedKeywords;
    }
}
