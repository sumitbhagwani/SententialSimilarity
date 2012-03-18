/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import edu.smu.tspell.wordnet.*;

/**
 *
 * @author shrutiranjans
 */
public class WordNetInterface {
    WordNetDatabase database = WordNetDatabase.getFileInstance();
    
    public void printWordForms(String word) {
        Synset synset;
        Synset[] synsets = this.database.getSynsets(word, SynsetType.VERB);
        for(int i=0; i<synsets.length; i++) {
            synset = (Synset)(synsets[i]);
            System.out.println(synset.getWordForms()[0] + ": " + synset.getDefinition());
        }        
    }
    
    public static void main(String[] args) {
        WordNetDatabase database = WordNetDatabase.getFileInstance();
//        WordNetInterface wni = new WordNetInterface();
//        String word = "like";
//        Synset[] synsets = database.getSynsets(word, SynsetType.VERB);
//        System.out.println((new WordSense(word, synsets[0]).toString()));
        //wni.printWordForms(word);
        
        Synset synset1 = database.getSynsets("running", SynsetType.NOUN)[0];
        Synset[] synsets = database.getSynsets("running");
    }
}
