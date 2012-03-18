/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import WordSimilarity.DISCOSimilarity;
import WordSimilarity.WordNetSimilarity;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author shrutiranjans
 */
public class StanfordParserTest
{
    public static LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
    //public static LexicalizedParser lp = new LexicalizedParser("englishFactored.ser.gz");
        
    public static void printScores(String inputFile, String outputFile, int whichMeasure)
    {
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
            String eachLine;
            int count = 1;
            int phraseSizeLimit = 2;
            while ((eachLine = br.readLine()) != null)
            {
                String[] sentences = eachLine.split("\t");
                                
                if (whichMeasure == 0) {
                    System.out.println(count);
                    Tree parse0 = lp.apply(sentences[0]);
                    Tree parse1 = lp.apply(sentences[1]);
                    GrammaticalStructure gs0 = gsf.newGrammaticalStructure(parse0);
                    GrammaticalStructure gs1 = gsf.newGrammaticalStructure(parse1);
                    List<TypedDependency> tdl0 = (List<TypedDependency>) gs0.allTypedDependencies();
                    List<TypedDependency> tdl1 = (List<TypedDependency>) gs1.allTypedDependencies();
                    HashMap<String, String> set0 = new HashMap();
                    HashMap<String, String> set1 = new HashMap();
                    for(int i=0;i<tdl0.size();i++)
                    {
                        String dep = tdl0.get(i).dep().value().toString().toLowerCase();
                        String gov = tdl0.get(i).gov().value().toString().toLowerCase();
                        String reln = tdl0.get(i).reln().toString().toLowerCase();
                        set0.put(dep+" "+gov+" ",reln);
                        set0.put(gov+" "+dep+" ",reln);
                    }
                    for(int i=0;i<tdl1.size();i++)
                    {
                        String dep = tdl1.get(i).dep().value().toString().toLowerCase();
                        String gov = tdl1.get(i).gov().value().toString().toLowerCase();
                        String reln = tdl1.get(i).reln().toString().toLowerCase();
                        set1.put(dep+" "+gov+" ",reln);
                        set1.put(gov+" "+dep+" ",reln);
                    }
                    ArrayList<ArrayList<TaggedWord>> taggedWordsList1 = getPhrases(parse0, phraseSizeLimit);
                    ArrayList<ArrayList<TaggedWord>> taggedWordsList2 = getPhrases(parse1, phraseSizeLimit);
                    Set setPhrases0 = new HashSet();
                    Set setPhrases1 = new HashSet();
                    for (int i=0; i<taggedWordsList1.size(); i++)
                    {
                        ArrayList<TaggedWord> taggedWords1 = taggedWordsList1.get(i);
                        if (taggedWords1.size() > 1) 
                        {
                            String temp = "";
                            Boolean CDFlag = true;
                            String tags = "";
                            for (int j=0; j<taggedWords1.size(); j++)
                            {
                                //bw.write(taggedWords1.get(j).word() + " ");
                                if(taggedWords1.get(j).tag().toString().equals("CD")) 
                                {
                                    CDFlag = false;
                                }
                                else
                                {
                                    tags += taggedWords1.get(j).tag().toString();
                                    tags += " ";
                                }
                                temp += taggedWords1.get(j).word();
                                temp += " ";
                            }
                            if(set0.containsKey(temp) && CDFlag) 
                            {
                                bw.write(temp+" "+set0.get(temp)+"\n");
                                System.out.print(temp+" "+set0.get(temp)+" "+tags+"\n");
                                setPhrases0.add(temp);
                            }
                            
                            //bw.write(" : ");
                            
                        }
                    }
                    //bw.write(" | ");
                    
                    for (int i=0; i<taggedWordsList2.size(); i++)
                    {
                        ArrayList<TaggedWord> taggedWords2 = taggedWordsList2.get(i);
                        if (taggedWords2.size() > 1) 
                        {
                            String temp = "";
                            Boolean CDFlag = true;
                            String tags = "";
                            for (int j=0; j<taggedWords2.size(); j++)
                            {
                                //bw.write(taggedWords2.get(j).word() + " ");
                                if(taggedWords2.get(j).tag().toString().equals("CD")) 
                                {
                                    CDFlag = false;
                                }
                                else
                                {
                                    tags += taggedWords2.get(j).tag().toString();
                                    tags += " ";
                                }
                                temp += taggedWords2.get(j).word();
                                temp += " ";
                            }
                            if(set1.containsKey(temp) && CDFlag) 
                            {
                                bw.write(temp+" "+set1.get(temp)+"\n");
                                System.out.print(temp+" "+set1.get(temp)+" "+tags+"\n");
                                setPhrases1.add(temp);
                            }
                            
                            //bw.write(" : ");
                        }
                    }
                    //bw.write("\n");
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

        String outputFile1 = "/home/sumit/Academics/8th sem/CS697/train/STS.NERMWE.MSRparDISCO.txt";
        String outputFile2 = "/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.MSRvidDISCO.txt";
        String outputFile3 = "/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.SMTeuroparlDISCO.txt";

        
        printScores2(inputFile1, outputFile1, whichMeasure);
        //printScores(inputFile2, outputFile2, whichMeasure);
        //printScores(inputFile3, outputFile3, whichMeasure);

        //getHyphenatedWords(inputFile1,outputFile1);
    }

    public static void getHyphenatedWords(String inputFile, String outputFile)
    {
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
            String eachLine;
            int count = 1;
            while ((eachLine = br.readLine()) != null)
            {
                String[] sentences = eachLine.split("\t");
                System.out.println(count);
                Tree parse0 = lp.apply(sentences[0]);
                Tree parse1 = lp.apply(sentences[1]);
//                GrammaticalStructure gs0 = gsf.newGrammaticalStructure(parse0);
//                GrammaticalStructure gs1 = gsf.newGrammaticalStructure(parse1);
//                List<TypedDependency> tdl0 = (List<TypedDependency>) gs0.allTypedDependencies();
//                List<TypedDependency> tdl1 = (List<TypedDependency>) gs1.allTypedDependencies();
                ArrayList<TaggedWord> list0=  parse0.taggedYield();
                ArrayList<TaggedWord> list1=  parse1.taggedYield();
                for(int j=0; j<list0.size();j++)
                {
                    String word = list0.get(j).value();
                    String tag = list0.get(j).tag().toString();
                    if(word.contains("-") && !tag.contains(":"))
                    {
                        bw.write(word +" "+tag+" # ");
                        //System.out.print(word +" "+tag+"\n");
                    }
                }
                bw.write(" | ");
                for(int j=0; j<list1.size();j++)
                {
                    String word = list1.get(j).value();
                    String tag = list1.get(j).tag().toString();
                    if(word.contains("-") && !tag.contains(":"))
                    {
                        bw.write(word +" "+tag+" # ");
                        //System.out.print(word +" "+tag+"\n");
                    }
                }
                bw.write("\n");

                count++;
            }
            br.close();
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static ArrayList<ArrayList<TaggedWord>> getPhrases(Tree parse, int phraseSizeLimit) {
        ArrayList<ArrayList<TaggedWord>> newList = new ArrayList<ArrayList<TaggedWord>>();
        List<Tree> leaves = parse.getLeaves();
        
        if (leaves.size() <= phraseSizeLimit) {
            
            ArrayList<TaggedWord> phraseElements = LexicalSimilarity.Preprocess(parse.taggedYield());
            newList.add(phraseElements);           
        }            
        else {
            Tree[] childrenNodes = parse.children();
            for (int i=0; i<childrenNodes.length; i++) {
                Tree currentParse = childrenNodes[i];
                newList.addAll(getPhrases(currentParse, phraseSizeLimit));
            }
        }
        return newList;
    }

    public static void printScores2(String inputFile, String outputFile, int whichMeasure)
    {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
            String serializedClassifier = "english.muc.7class.distsim.crf.ser.gz";
            AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
            String eachLine;
            int count = 1;
            while ((eachLine = br.readLine()) != null) {
                String[] sentences = eachLine.split("\t");

                if (whichMeasure == 0) {
                    System.out.println(count);
                    bw.write("#" + sentences[0] + "#\n");
                    bw.write(StanfordNER.parseNERMWE(sentences[0], lp, classifier).toString() + "\n");
                    bw.write("#" + sentences[1] + "#\n");
                    bw.write(StanfordNER.parseNERMWE(sentences[1], lp, classifier).toString() + "\n");
                    bw.write("************************\n");
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

    public static void main(String args[]) {
//        int phraseSizeLimit = 3;
//        
//        String sentence = "Amgen Rovers shares gained 93 cents, or 1.45 percent, to $65.05 in afternoon trading on Nasdaq";
//        Tree parse = lp.apply(sentence);
//        ArrayList<ArrayList<TaggedWord>> phrases = getPhrases(parse, phraseSizeLimit);
//        for (int i=0; i<phrases.size(); i++) {
//            ArrayList<TaggedWord> phrase = phrases.get(i);
//            for (int j=0; j<phrase.size(); j++) {
//                System.out.print(phrase.get(j).word() + ":" + phrase.get(j).tag() + " ");
//            }
//            System.out.println();
//        }
//        
//        System.out.println("************************");
//        
//        sentence = "The technology-laced Nasdaq Composite Index <.IXIC> climbed 19.11 points, or 1.2 percent, to 1,615.02.";
//        parse = lp.apply(sentence);
//        phrases = getPhrases(parse, phraseSizeLimit);
//        for (int i=0; i<phrases.size(); i++) {
//            ArrayList<TaggedWord> phrase = phrases.get(i);
//            for (int j=0; j<phrase.size(); j++) {
//                System.out.print(phrase.get(j).word() + ":" + phrase.get(j).tag() + " ");
//            }
//            System.out.println();
//        }
        
//        String sentence = "Amgen Rovers shares gained 93 cents, or 1.45 percent, to $65.05 in afternoon trading on Nasdaq";
//        String sentence = "The 30-year bond <US30YT=RR> firmed 24/32, taking its yield to 4.18 percent, after hitting another record low of 4.16 percent.";
//        ArrayList<TaggedWord> taggedWords = LexicalSimilarity.PreprocessPhrase(LexicalSimilarity.StanfordParse(sentence, lp));
//        for (int i=0; i<taggedWords.size(); i++) {
//            System.out.println(taggedWords.get(i).word() + " : " + taggedWords.get(i).tag().toString().equals("CD"));
//        }
//        String temp = "";
//        System.out.println("##");
//
//        for (int j=0; j<taggedWords.size(); j++)
//        {
//            Boolean CDFlag = true;
//            if(taggedWords.get(j).tag().toString().equals("CD")) CDFlag = false;
//            temp += taggedWords.get(j).word();
//            temp += " ";
//            if(CDFlag) { System.out.println(temp); }
//        }
//

        

       getCorrelationScore(0);
        
    }
    
}
