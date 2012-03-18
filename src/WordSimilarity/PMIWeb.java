/*
 * Use a web corpus to evaluate the PMI word similarity between two words.
 * Bing is used for the purpose of this project
 */
package WordSimilarity;

import com.google.code.bing.search.client.BingSearchClient;
import com.google.code.bing.search.client.BingSearchClient.SearchRequestBuilder;
import com.google.code.bing.search.client.BingSearchServiceClientFactory;
import com.google.code.bing.search.schema.AdultOption;
import com.google.code.bing.search.schema.SearchResponse;
import com.google.code.bing.search.schema.SourceType;
import com.google.code.bing.search.schema.web.WebSearchOption;

/**
 *
 * @author shrutiranjans
 */
public class PMIWeb {
    
    private String appId = "08582DA2762622603CCEECDFEB790432174C059B";
    
    private long search(String query) {
        
        BingSearchServiceClientFactory factory = BingSearchServiceClientFactory.newInstance();
        BingSearchClient client = factory.createBingSearchClient();
        SearchRequestBuilder builder = client.newSearchRequestBuilder();
        
        builder.withAppId(this.appId);
        builder.withQuery(query);
        builder.withSourceType(SourceType.WEB);
        builder.withVersion("2.0");
        builder.withMarket("en-us");
        builder.withAdultOption(AdultOption.OFF);
        
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_HOST_COLLAPSING);
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_QUERY_ALTERATIONS);
        
        SearchResponse response = client.search(builder.getResult());        
        return response.getWeb().getTotal();
        
    }
    
    public double score1(String word1, String word2) {
        
        long hitsCombination, hits1, hits2;
        double finalScore;
        String query;
        
        query = word1 + " " + word2;
        hitsCombination = this.search(query);
        
        query = word1;
        hits1 = this.search(query);
        
        query = word2;
        hits2 = this.search(query);
        
        if (hits1 == 0 || hits2 == 0)
            finalScore = 0;
        else {
            long normFactor = Math.max(hits1, hits2);
            finalScore = hitsCombination*1.0/normFactor;
        }
        
        return finalScore;
        
    }
    
    public double score4(String word1, String word2, String[] context1, String[] context2) {
        
        int i, maxIndex1 = 0, maxIndex2 = 0;
        double maxScore = 0, currScore;        
        
        for (i=0; i<context1.length; i++) {
            currScore = this.score1(word1, context1[i]);
            if (currScore > maxScore) {
                maxScore = currScore;
                maxIndex1 = i;
            }
        }
        
        maxScore = 0;
        for (i=0; i<context2.length; i++) {
            currScore = this.score1(word2, context2[i]);
            if (currScore > maxScore) {
                maxScore = currScore;
                maxIndex2 = i;
            }
        }
                
        long hitsCombination, hits1, hits2;
        double finalScore;
        String query;
        
        query = word1 + " " + context1[maxIndex1] + " " + word2 + " " + context2[maxIndex2];
        hitsCombination = this.search(query);
        
        query = word1 + " " + context1[maxIndex1];
        hits1 = this.search(query);
        
        query = word2 + " " + context2[maxIndex2];
        hits2 = this.search(query);
        
        if (hitsCombination == 0)
            finalScore = -1;
        else
            finalScore = Math.log(hits1) + Math.log(hits2) - Math.log(hitsCombination);
        
        return finalScore;
        
    }
    
    public static void main(String args[]) {
        
        String eachLine = "bread,butter,6.19";
        String[] tokens = eachLine.split(",");
        String word1 = tokens[0].toLowerCase();
        String word2 = tokens[1].toLowerCase();
        String humanScore = tokens[2];
        
        PMIWeb testInstance = new PMIWeb();
        System.out.println("PMI score: " + testInstance.score1(word1, word2));       
    }
    
}
