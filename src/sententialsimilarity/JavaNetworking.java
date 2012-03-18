/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import java.net.URL;
import java.net.URLConnection;
import java.io.*;

/**
 *
 * @author shrutiranjans
 */
public class JavaNetworking {
    
    public static String getUrlSourceCode(String urlString) throws IOException {
        StringBuilder urlCode = new StringBuilder();
        
        URL url = new URL(urlString);
        URLConnection hpCon = url.openConnection();
        
        System.setProperty("http.proxyHost", "nknproxy.iitk.ac.in");
        System.setProperty("http.proxyPort", "3128");
        System.setProperty("http.proxyHost", "nknproxy.iitk.ac.in");
        System.setProperty("http.proxyPort", "3128");
        
        String login = "sranjans:harikaho";
        String encodedLogin = new sun.misc.BASE64Encoder().encode(login.getBytes());
        hpCon.setRequestProperty("Proxy-Authorization", "Basic " + encodedLogin);
        
        BufferedReader buffer = new BufferedReader(new InputStreamReader(hpCon.getInputStream(), "UTF-8"));
        String eachLine;
        
        while ((eachLine = buffer.readLine()) != null)
            urlCode.append(eachLine);
        buffer.close();
        
        return urlCode.toString();
    }
    
    public static void main(String args[]) throws IOException {
        
        String url = "http://www.bing.com/search?q=rambo stallone";
        String urlSourceCode = getUrlSourceCode(url);
        System.out.println(urlSourceCode.length());
        
        String resultString = "<span class=\"sb_count\" id=\"count\">";
        int additionalLength = 8;
        int index = urlSourceCode.indexOf(resultString) + resultString.length() + additionalLength;
        
        long hits = 0;
        char c;
        while ((c = urlSourceCode.charAt(index)) != ' ') {
            if (c != ',')
                hits = hits*10 + (c - 48);
            index++;
        }
        System.out.println(hits);
        
    }
    
}
