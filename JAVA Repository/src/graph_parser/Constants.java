/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

/**
 *
 * @author MSI
 */
public class Constants {
    public static final String INPUT_DIR = "../Dataset_Preprocessed/Augmented_1K";
    public static final int MIN_AUTH_ID = 1;
    public static final int MAX_AUTH_ID = 10000;
    public static final int NUM_KEYWORDS = 100;
    public static final double MATCHING_THRESHOLD = 0.20;
    
    public static boolean LOAD_GRAPH = false;
    public static boolean SHOW_OUTPUT = false;
    public static boolean DEBUG_MODE = false;
    public static int RUNS = 100;
    
    public static final int AND_PREDICATE = 1;
    public static final int OR_PREDICATE = 0;
    
    public static final int INVALID_INT = Integer.MIN_VALUE;
    
    public static double BETA = 0.90;
}
