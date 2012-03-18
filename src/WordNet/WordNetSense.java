/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WordNet;

/**
 *
 * @author shrutiranjans
 */
public class WordNetSense {
    public String word;
    public int sense;
    public WordNetType type;
    
    public WordNetSense(String word, int sense, WordNetType type) {
        this.word = word;
        this.sense = sense;
        this.type = type;
    }
}
