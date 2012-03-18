/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author shrutiranjans
 */
public class PearsonCorrelation {
    
    public static double Correlation(String filePath1, String filePath2) {
        double correlation = 0;
        try {
            ArrayList scores1 = new ArrayList();
            BufferedReader br1 = new BufferedReader(new FileReader(new File(filePath1)));
            String eachLine;
            while ((eachLine = br1.readLine()) != null) {
                scores1.add(Double.valueOf(eachLine));
            }
            br1.close();
            
            ArrayList scores2 = new ArrayList();
            BufferedReader br2 = new BufferedReader(new FileReader(new File(filePath2)));
            while ((eachLine = br2.readLine()) != null) {
                scores2.add(Double.valueOf(eachLine));
            }
            br2.close();
            
            double sum_sq_x = 0;
            double sum_sq_y = 0;
            double sum_coproduct = 0;
            
        }        
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return correlation;
    }
    
}
