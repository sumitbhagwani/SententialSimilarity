/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WordSimilarity;

import de.linguatools.disco.DISCO;
import java.io.IOException;

public class DISCOSimilarity {
    
    private String discoDir = "/media/Data/en-wikipedia-20080101/";
    private DISCO discoRAM;
    
    public DISCOSimilarity() {
        try {
            discoRAM = new DISCO(discoDir, false);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }    
    
    public double similarity1(String word1, String word2) {
        double finalScore = 0;
        try {
            finalScore = discoRAM.firstOrderSimilarity(word1, word2);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return finalScore;
    }
    
    public double similarity2(String word1, String word2) {
        double finalScore = 0;
        try {
            finalScore = discoRAM.secondOrderSimilarity(word1, word2);
            if (finalScore == -1)
                finalScore = 0;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }        
        return finalScore;
    }
    
    public static void main(String[] args) throws IOException {

        String word1 = "2 years";
        String word2 = "12.32 years";
        
        DISCOSimilarity testObj = new DISCOSimilarity();
        //System.out.println(similarity1(word1, word2, discoRAM));
        for (int i=0; i<1; i++) {
            System.out.println(i + " , " + testObj.similarity2(word1, word2));
        }	
    }
}
