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
    public static final String INPUT_DIR = "../Dataset_Preprocessed/small_dataset";
    public static final int MIN_AUTH_ID = 1;
    public static final int MAX_AUTH_ID = 100000;
    public static final int NUM_KEYWORDS = 1000;
    public static final double MATCHING_THRESHOLD = 0.20;
    
    public static boolean LOAD_GRAPH = false;
    //public static boolean COMPUTE_INV_LIST = false;
    
    public static final int AND_PREDICATE = 1;
    public static final int OR_PREDICATE = 0;
}
