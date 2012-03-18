/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

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
public class BingTest {
    
    public static void main(String args[]) {
        
        String AppId = "08582DA2762622603CCEECDFEB790432174C059B";
    
        BingSearchServiceClientFactory factory = BingSearchServiceClientFactory.newInstance();
        BingSearchClient client = factory.createBingSearchClient();
    
        SearchRequestBuilder builder = client.newSearchRequestBuilder();
        builder.withAppId(AppId);
        builder.withQuery("bread butter");
        builder.withSourceType(SourceType.WEB);
        builder.withVersion("2.0");
        builder.withMarket("en-us");
        builder.withAdultOption(AdultOption.OFF);
        
        builder.withWebRequestCount(10L);
        builder.withWebRequestOffset(0L);
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_HOST_COLLAPSING);
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_QUERY_ALTERATIONS);
        
        SearchResponse response = client.search(builder.getResult());    
        
        System.out.println("Number of hits: " + response.getWeb().getTotal());        
                        
    }       
       
}
