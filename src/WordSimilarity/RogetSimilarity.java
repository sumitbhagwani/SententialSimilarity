/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WordSimilarity;

import semDist.*;

/**
 *
 * @author shrutiranjans
 */
public class RogetSimilarity {
    
    public SemDist sd = new SemDist();
    
    // The Roget Dictionary needs to be located in the home directory
    
    public int rogetSimilarity (String word1, String word2) {
        return this.sd.getSimilarity(word1, word2);
    }
    
    public int rogetSimilarity (String word1, String word2, String pos) {
        return this.sd.getSimilarity(word1, word2, pos);
    }
    
    public int rogetSimilarity (String word1, String pos1, String word2, String pos2) {
        return this.sd.getSimilarity(word1, pos1, word2, pos2);
    }
    
    public static void main(String args[]) {
        String word1 = "pen";
        String pos1 = "N.";
        String word2 = "pencil";
        String pos2 = "N.";
        
        RogetSimilarity rs = new RogetSimilarity();
        int distance = rs.rogetSimilarity(word1, pos1, word2, pos2);
        System.out.println("Roget Similarity : " + distance);
    }
    
}
