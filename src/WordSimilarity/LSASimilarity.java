/*
 * Makes use of LSA Colorado's web interface.
 * For reference, visit http://lsa.colorado.edu/
 */
package WordSimilarity;

import java.net.*;
import java.io.*;

/**
 *
 * @author shrutiranjans
 */
public class LSASimilarity {
    
    public static double LSAWordSimilarity(String word1, String word2) {
        String actionUrl = "http://lsa.colorado.edu/cgi-bin/LSA-pairwise-x.html";
        double finalValue = -1;
        int minValue = -1;
        boolean flag = true;
        
        while (flag) {
            try
            {
                flag = false;
                URL siteUrl = new URL(actionUrl);

                System.setProperty("http.proxyHost", "nknproxy.iitk.ac.in");
                System.setProperty("http.proxyPort", "3128");
                final String authUser = "sranjans";
                final String authPassword = "harikaho";
                Authenticator.setDefault(
                    new Authenticator() {
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                        }
                    }
                );
                System.setProperty("http.proxyUser", authUser);
                System.setProperty("http.proxyPassword", authPassword);

                HttpURLConnection conn = (HttpURLConnection)siteUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                DataOutputStream out = new DataOutputStream(conn.getOutputStream());

                String content = "";
                content += "LSAspace=" + URLEncoder.encode("General_Reading_up_to_1st_year_college 	 (300 factors)", "UTF-8");
                content += "&CmpType=" + URLEncoder.encode("term2term", "UTF-8");
                content += "&LSAFactors=" + URLEncoder.encode("", "UTF-8");
                content += "&txt1=" + URLEncoder.encode(word1+"\r\n\r\n"+word2, "UTF-8");
                //System.out.println(content);
                out.writeBytes(content);
                out.flush();
                out.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String output = "";
                String line;
                while ((line = in.readLine()) != null) {
                    output += line + "\n";
                }
                in.close();

                String tableSearch = "</TABLE";
                String resultString = "";

                char c;
                int index = output.indexOf(tableSearch)-2;
                while ((c = output.charAt(index)) != ' ') {
                    resultString = c + resultString;
                    index--;
                }    
                if (resultString.equals("N/A"))
                    finalValue = -1;
                else
                    finalValue = Double.valueOf(resultString);
                
            }
            catch (Exception ex) {
                flag = true;
                ex.printStackTrace();
            }
        }
        
        return (finalValue-minValue)/2.0;
    }
    
    public static void bhagguSet() {
        
        try {
            String pathInput = "/home/shrutiranjans/Desktop/BhagguSet.txt";
            String pathOutput = "/home/shrutiranjans/Desktop/BhagguSetOutput.txt";
            
            File dataset = new File(pathInput);
            File outputFile = new File(pathOutput);
            BufferedReader br = new BufferedReader(new FileReader(dataset));
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            
            DISCOSimilarity discoRAM = new DISCOSimilarity();
            PMIWeb pmiObj = new PMIWeb();
            int counter = 1;
            
            bw.write("# LSA,DISCO,HUMAN_SCORE");
            
            String eachLine;
            while ((eachLine = br.readLine()) != null) {
                System.out.println(eachLine);
                String[] tokens = eachLine.split(",");
                String word1 = tokens[0].toLowerCase();
                String word2 = tokens[1].toLowerCase();
                String humanScore = tokens[2];
                
                String newLine = "";                
                newLine += Double.toString(LSAWordSimilarity(word1, word2)) + ",";
                newLine += Double.toString(discoRAM.similarity2(word1, word2)) + ",";
                newLine += Double.toString(pmiObj.score1(word1, word2)) + ",";
                newLine += humanScore + "\n";
                
                //System.out.println(newLine);
                bw.write(newLine);
                System.out.println(counter);
                counter++;
            }
            
            br.close();
            bw.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }        
        
    }
    
    public static void main(String args[]) throws IOException {
        String word1 = "gun";
        String word2 = "shoot";
        
        System.out.println(LSAWordSimilarity(word1, word2));
        //testFunction();
        //bhagguSet();
    }
    
}
