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
    /**
     * RUN PARAMETERS
     */
    public static String INPUT_DIR = "../Dataset_Preprocessed/Augmented_1K";
    
    public static int MIN_AUTH_ID = 1;
    public static int MAX_AUTH_ID = 20000;
    public static int NUM_KEYWORDS = 1000;
    public static double MATCHING_THRESHOLD = 0.30;
    public static int K_MIN = 2;
    public static int TOP_R = 5;
    
    public static double BETA = 0.90;
    
    public static boolean LOAD_GRAPH = false;
    public static boolean COMPUTE_CL_TREE = false;
    public static boolean SHOW_OUTPUT =false;
    public static boolean DEBUG_MODE = false;
    
    public static int RUNS = 100;
    
    /**
     * CONSTANTS
     */
    public static final int AND_PREDICATE = 1;
    public static final int OR_PREDICATE = 0;
    public static final int INVALID_INT = Integer.MIN_VALUE;
    public static boolean SPECIAL_REGION_PRINT = false;
}
