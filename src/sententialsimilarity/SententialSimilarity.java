/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import DependencyTest.MyDependency;
import WordNet.WordNetSense;
import WordNet.WordNetType;
import edu.smu.tspell.wordnet.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import opennlp.tools.tokenize.*;
import java.lang.String.*;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import WordSimilarity.DISCOSimilarity;
import WordSimilarity.LSASimilarity;
import de.linguatools.disco.DISCO;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;


/**
 *
 * @author shrutiranjans
 */
public class SententialSimilarity {

    /**
     * @param args the command line arguments
     */
    
    public static double lexicalSimilarity(String sentence1, String sentence2) {
        DISCOSimilarity discoRAM = new DISCOSimilarity();
        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
        return LexicalSimilarity.LexicalSimilarityScore(sentence1, sentence2, discoRAM, lp);
    }
    
    public static void printScores(String inputFile, String outputFile, int whichMeasure) {
        try {
            DISCOSimilarity discoRAM = new DISCOSimilarity();
            LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
            String serializedClassifier = "english.muc.7class.distsim.crf.ser.gz";
            AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);

            BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
            String eachLine;
            int count = 1;
            while ((eachLine = br.readLine()) != null) {
                String[] sentences = eachLine.split("\t");
                //sentences[0] += ".";
                //sentences[1] += ".";
                
                if (whichMeasure == 0) {
                    System.out.println(count);
                    
                    //bw.write(Double.toString(LexicalSimilarity.LexicalSimilarityScore(sentences[0], sentences[1], discoRAM, lp)) + "\n");
                    bw.write(Double.toString(LexicalSimilarity.LexicalSimilarityScore(sentences[0], sentences[1], discoRAM, lp, classifier)) + "\n");
                }                   
                else
                    System.out.println("Invalid Sentence Similarity Measure Chosen.");
                count++;
            }
            br.close();
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /* whichMeasure takes the follwing values:
     * 0 - lexicalSimilarity
     */
    public static void getCorrelationScore(int whichMeasure) {
        String inputFile1 = "/home/sumit/Academics/8th sem/CS697/train/STS.input.MSRpar.txt";
        String inputFile2 = "/home/sumit/Academics/8th sem/CS697/train/STS.input.MSRvid.txt";
        String inputFile3 = "/home/sumit/Academics/8th sem/CS697/train/STS.input.SMTeuroparl.txt";
        
        String outputFile1 = "/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.MSRparDISCO.txt";
        String outputFile2 = "/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.MSRvidDISCO.txt";
        String outputFile3 = "/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.SMTeuroparlDISCO.txt";
        
        printScores(inputFile1, outputFile1, whichMeasure);
        //printScores(inputFile2, outputFile2, whichMeasure);
        //printScores(inputFile3, outputFile3, whichMeasure);
    }
    

    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
        getCorrelationScore(0);
        
    }
}
