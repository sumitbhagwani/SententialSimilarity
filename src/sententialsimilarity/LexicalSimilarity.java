/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import WordNet.WordNetSense;
import WordSimilarity.DISCOSimilarity;
import WordSimilarity.LSASimilarity;
import WordSimilarity.WordNetSimilarity;
import edu.mit.jwi.IDictionary;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import java.util.*;
import java.io.*;

import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Morphology;
import java.net.MalformedURLException;
import java.net.URL;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 *
 * @author shrutiranjans
 */
public class LexicalSimilarity {

    public static ArrayList<ArrayList<TaggedWord>> getPhrasesNaive(String sentence, LexicalizedParser lp, AbstractSequenceClassifier<CoreLabel> classifier)
    {
        ArrayList<ArrayList<TaggedWord>> newList = new ArrayList<ArrayList<TaggedWord>>();
        ArrayList<TaggedWord> taggedWords = StanfordNER.parse(sentence,lp,classifier);
        HashMap<String,String> phraseBoundaries = new HashMap<String,String>();
        phraseBoundaries.put(",", ",");
        phraseBoundaries.put("\"", "\"");
        phraseBoundaries.put("''", "''");
        phraseBoundaries.put("``", "``");
        phraseBoundaries.put("--", "--");
        //List<Tree> leaves = parse.getLeaves();
        ArrayList<TaggedWord> temp = new ArrayList<TaggedWord>();
        int index = 0;
        while(index<taggedWords.size())
        {
            if((phraseBoundaries.containsKey(taggedWords.get(index).word())))
            {
                if(temp.size()>0)
                {
                    //System.out.println(temp);
                    ArrayList<TaggedWord> tempCopy = new ArrayList<TaggedWord>(temp);
                    newList.add(Preprocess(tempCopy));
                }
                temp.clear();
            }
            else
            {
                //System.out.println(taggedWords.get(index).toString());
                temp.add(taggedWords.get(index));
            }
            index += 1;
        }
        if(temp.size()>0)
        {
            ArrayList<TaggedWord> tempCopy = new ArrayList<TaggedWord>(temp);
            newList.add(Preprocess(tempCopy));
        }

        //System.out.println(newList);
        return newList;
    }

    public static ArrayList<TaggedWord> StopWordRemoval(ArrayList<TaggedWord> taggedWords) {
        ArrayList<TaggedWord> newList = new ArrayList<TaggedWord>();

        try {
            String path = "data/nltk_stoplist.txt";
            File textFile = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(textFile));
            String stopwordsLine = br.readLine();
            br.close();

            String[] stopwords = stopwordsLine.split(",");
            HashMap<String, String> stopwordsDict = new HashMap<String, String>();
            for (int i=0; i<stopwords.length; i++) {
                stopwordsDict.put(stopwords[i], stopwords[i]);
            }

            for (int i=0; i<taggedWords.size(); i++) {
                String word = taggedWords.get(i).word();
                String posTag = taggedWords.get(i).tag();

                if (!stopwordsDict.containsKey(word.toLowerCase())) {
                    String newWord, newPosTag;
                    newWord = word;
                    newPosTag = posTag;
                    newList.add(new TaggedWord(newWord, newPosTag));
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return newList;
    }

    public static ArrayList<TaggedWord> Preprocess(ArrayList<TaggedWord> taggedWords) {
        ArrayList<TaggedWord> newList = new ArrayList<TaggedWord>();

        String[] punctuationsAndSpecialCharacters = {",", ".", "?", "!", ":", ";", "\"", "-", "--", "'", "-LRB-", "-RRB-", "''", "``", "&"};//, "/", "\\", "<", ">", "#", "&", "*", "(", ")", "{", "}", "[", "]", "~", "|"};
        HashMap<String, String> punctuationMarks = new HashMap<String, String>();
        for (int i=0; i<punctuationsAndSpecialCharacters.length; i++) {
            punctuationMarks.put(punctuationsAndSpecialCharacters[i], punctuationsAndSpecialCharacters[i]);
        }

        for (int i = 0; i < taggedWords.size(); i++)
        {
            String word = taggedWords.get(i).word();
            String posTag = taggedWords.get(i).tag();

            if (!punctuationMarks.containsKey(word)) {

                if (!(posTag.length()>2 && posTag.substring(0, 3).equals("NNP"))) {
                    word = Morphology.lemmaStatic(word, posTag, true);
                    word = word.replace('-', ' ');
                }

                String newWord, newPosTag;
                if(word.equals("n't"))
                    newWord = "not";
                else if(word.equals("'s"))
                    newWord = "is";
                else if(word.equals("'ll"))
                    newWord = "will";
                else if(word.equals("'m") || word.equals("m"))
                    newWord = "am";
                else if(word.equals("im"))
                    newWord = "am";
                else
                    newWord = word;
                newPosTag = posTag;
                newList.add(new TaggedWord(newWord, newPosTag));
            }
        }
        newList = StopWordRemoval(newList);
        return newList;
    }

    public static ArrayList<TaggedWord> StanfordParse(String sentence, LexicalizedParser lp) {

        TokenizerFactory<CoreLabel> tokenizerFactory =
		PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        List<CoreLabel> rawWords2 =
		tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
        Tree parse = lp.apply(rawWords2);
        ArrayList<TaggedWord> taggedWords = parse.taggedYield();

        return taggedWords;

    }

    // mode - min
    public static double LexicalSimilarityScore(String sentence1, String sentence2, DISCOSimilarity discoRAM, LexicalizedParser lp) {
        ArrayList<TaggedWord> taggedWords1 = Preprocess(StanfordParse(sentence1, lp));
        ArrayList<TaggedWord> taggedWords2 = Preprocess(StanfordParse(sentence2, lp));

        return LexicalSimilarityScore(taggedWords1, taggedWords2, discoRAM, lp);
        //return LexicalSimilarityScoreMax(taggedWords1, taggedWords2, discoRAM, lp);
    }

    // mode - 2level
    public static double LexicalSimilarityScore(String sentence1, String sentence2, DISCOSimilarity discoRAM, LexicalizedParser lp, AbstractSequenceClassifier<CoreLabel> classifier) {
//        ArrayList<TaggedWord> taggedWords1 = Preprocess(StanfordNER.parse(sentence1, lp, classifier));
//        ArrayList<TaggedWord> taggedWords2 = Preprocess(StanfordNER.parse(sentence2, lp, classifier));

        ArrayList<TaggedWord> taggedWords1 = Preprocess(StanfordNER.parseNERMWE(sentence1, lp, classifier));
        ArrayList<TaggedWord> taggedWords2 = Preprocess(StanfordNER.parseNERMWE(sentence2, lp, classifier));

        //return LexicalSimilarityScore(taggedWords1, taggedWords2, discoRAM, lp);
        return LexicalSimilarityScoreMax(taggedWords1, taggedWords2, discoRAM, lp);
    }

    // mode - DISCO
    public static double LexicalSimilarityScore(ArrayList<TaggedWord> taggedWords1, ArrayList<TaggedWord> taggedWords2, DISCOSimilarity discoRAM, LexicalizedParser lp) {

        //System.out.println(taggedWords1.size() + "," + taggedWords2.size());

        // array of edge weights with default weight 0
        int length1 = taggedWords1.size();
        int length2 = taggedWords2.size();
        int arrSize = Math.max(length1, length2);
        double[][] array = new double[arrSize][arrSize];
        for (int i=0; i<arrSize; i++) {
            for (int j=0; j<arrSize; j++) {
                array[i][j] = 0;
            }
        }
        for (int i=0; i<length1; i++) {
            for (int j=0; j<length2; j++) {
                String word1 = taggedWords1.get(i).word();
                String word2 = taggedWords2.get(j).word();
                double edgeWeight = 0;

                // LSA Similarity
                //edgeWeight = LSASimilarity.LSAWordSimilarity(word1, word2);

                // DISCO Similarity
                //DISCOSimilarity discoObj = new DISCOSimilarity();
                try {
                    if (word1.compareToIgnoreCase(word2) == 0)
                        edgeWeight = 1;
                    else {
                        edgeWeight = discoRAM.similarity2(word1, word2);
                        //edgeWeight = LSASimilarity.LSAWordSimilarity(word1, word2);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

                array[i][j] = edgeWeight;
            }
        }

        //System.out.println("Hungarian starts " + arrSize);

        double finalScore;
        String sumType = "max";
        //int minLength = Math.min(length1, length2);
        //finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/minLength * 5;
        finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/arrSize * 5;

        return finalScore;
    }

    public static double LexicalSimilarityScoreMin(ArrayList<TaggedWord> taggedWords1, ArrayList<TaggedWord> taggedWords2, DISCOSimilarity discoRAM, LexicalizedParser lp) {

        //System.out.println(taggedWords1.size() + "," + taggedWords2.size());

        // array of edge weights with default weight 0
        int length1 = taggedWords1.size();
        int length2 = taggedWords2.size();
        int arrSize = Math.max(length1, length2);
        double[][] array = new double[arrSize][arrSize];
        for (int i=0; i<arrSize; i++) {
            for (int j=0; j<arrSize; j++) {
                array[i][j] = 0;
            }
        }
        for (int i=0; i<length1; i++) {
            for (int j=0; j<length2; j++) {
                String word1 = taggedWords1.get(i).word();
                String word2 = taggedWords2.get(j).word();
                double edgeWeight = 0;

                // LSA Similarity
                //edgeWeight = LSASimilarity.LSAWordSimilarity(word1, word2);

                // DISCO Similarity
                //DISCOSimilarity discoObj = new DISCOSimilarity();
                try {
                    if (word1.compareToIgnoreCase(word2) == 0)
                        edgeWeight = 1;
                    else {
                        edgeWeight = discoRAM.similarity2(word1, word2);
                        //edgeWeight = LSASimilarity.LSAWordSimilarity(word1, word2);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

                array[i][j] = edgeWeight;
            }
        }

        //System.out.println("Hungarian starts " + arrSize);

        double finalScore;
        String sumType = "max";
        int minLength = Math.min(length1, length2);
        finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/minLength * 5;
        //finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/arrSize * 5;

        return finalScore;
    }

    public static double LexicalSimilarityScoreMax(ArrayList<TaggedWord> taggedWords1, ArrayList<TaggedWord> taggedWords2, DISCOSimilarity discoRAM, LexicalizedParser lp) {

        //System.out.println(taggedWords1.size() + "," + taggedWords2.size());

        // array of edge weights with default weight 0
        int length1 = taggedWords1.size();
        int length2 = taggedWords2.size();
        int arrSize = Math.max(length1, length2);
        double[][] array = new double[arrSize][arrSize];
        for (int i=0; i<arrSize; i++) {
            for (int j=0; j<arrSize; j++) {
                array[i][j] = 0;
            }
        }
        for (int i=0; i<length1; i++) {
            for (int j=0; j<length2; j++) {
                String word1 = taggedWords1.get(i).word();
                String posTag1 = taggedWords1.get(i).tag();
                String word2 = taggedWords2.get(j).word();
                String posTag2 = taggedWords2.get(j).tag();

                ArrayList<TaggedWord> newList1 = new ArrayList<TaggedWord>();
                if (posTag1.length() >= 3 && posTag1.substring(0, 3).equals("NNP")) {
                    newList1.add(taggedWords1.get(i));
                }
                else {
                    String[] words = word1.split(" ");
                    for (int k=0; k<words.length; k++)
                        newList1.add(new TaggedWord(words[k], posTag1));
                }

                ArrayList<TaggedWord> newList2 = new ArrayList<TaggedWord>();
                if (posTag2.length() >= 3 && posTag2.substring(0, 3).equals("NNP")) {
                    newList2.add(taggedWords2.get(j));
                }
                else {
                    String[] words = word2.split(" ");
                    for (int k=0; k<words.length; k++)
                        newList2.add(new TaggedWord(words[k], posTag2));
                }

                double edgeWeight = LexicalSimilarityScoreMin(newList1, newList2, discoRAM, lp);

                array[i][j] = edgeWeight;
            }
        }

        //System.out.println("Hungarian starts " + arrSize);

        double finalScore;
        String sumType = "max";
        //int minLength = Math.min(length1, length2);
        //finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/minLength * 5;
        finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/arrSize * 5;

        return finalScore;
    }

    public static double LexicalSimilarityScoreDISCOWordNet(String sentence1, String sentence2, DISCOSimilarity discoRAM, LexicalizedParser lp, LeskWSD tm, WordNetSimilarity ws) {

        ArrayList<TaggedWord> taggedWords1 = Preprocess(StanfordParse(sentence1, lp));
        ArrayList<TaggedWord> taggedWords2 = Preprocess(StanfordParse(sentence2, lp));

        WordNetSense[] sensesPrev1 = tm.LeskJWI(sentence1);
        WordNetSense[] sensesPrev2 = tm.LeskJWI(sentence2);

        //System.out.println(taggedWords1.size() + "," + taggedWords2.size());

        // array of edge weights with default weight 0
        int length1 = taggedWords1.size();
        int length2 = taggedWords2.size();
        int arrSize = Math.max(length1, length2);
        double[][] array = new double[arrSize][arrSize];
        for (int i=0; i<arrSize; i++) {
            for (int j=0; j<arrSize; j++) {
                array[i][j] = 0;
            }
        }
        for (int i=0; i<length1; i++) {
            for (int j=0; j<length2; j++) {
                String word1 = taggedWords1.get(i).word();
                String posTag1 = taggedWords1.get(i).tag();
                String word2 = taggedWords2.get(j).word();
                String posTag2 = taggedWords2.get(j).tag();
                double edgeWeight = 0;

                // LSA Similarity
                //edgeWeight = LSASimilarity.LSAWordSimilarity(word1, word2);

                // DISCO Similarity
                //DISCOSimilarity discoObj = new DISCOSimilarity();
                try {
                    if (word1.compareToIgnoreCase(word2) == 0)
                        edgeWeight = 1;
                    else if (posTag1.length()>1 && posTag1.substring(0, 2).equals("NN") && posTag2.length()>1 && posTag2.substring(0, 2).equals("NN")) {
                        edgeWeight = ws.linSimilarity(sensesPrev1[i], sensesPrev2[j]);
                    }
                    else if (posTag1.length()>1 && posTag1.substring(0, 2).equals("VB") && posTag2.length()>1 && posTag2.substring(0, 2).equals("VB")) {
                        edgeWeight = ws.linSimilarity(sensesPrev1[i], sensesPrev2[j]);
                    }
                    else {
                        edgeWeight = discoRAM.similarity2(word1, word2);
                        //edgeWeight = LSASimilarity.LSAWordSimilarity(word1, word2);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

                array[i][j] = edgeWeight;
            }
        }

        //System.out.println("Hungarian starts " + arrSize);

        double finalScore;
        String sumType = "max";
        int minLength = Math.min(length1, length2);
        //finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/minLength * 5;
        finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/arrSize * 5;

        return finalScore;
    }

    public static double LexicalSimilarityScoreWordNet(String sentence1, String sentence2, LeskWSD tm, LexicalizedParser lp, WordNetSimilarity ws) {

        ArrayList<TaggedWord> taggedWordsPrev1 = Preprocess(StanfordParse(sentence1, lp));
        ArrayList<TaggedWord> taggedWordsPrev2 = Preprocess(StanfordParse(sentence2, lp));
        ArrayList<TaggedWord> taggedWords1 = new ArrayList<TaggedWord>();
        ArrayList<TaggedWord> taggedWords2 = new ArrayList<TaggedWord>();

        WordNetSense[] sensesPrev1 = tm.LeskJWI(sentence1);
        WordNetSense[] sensesPrev2 = tm.LeskJWI(sentence2);

        //System.out.println("Senses found!");

        ArrayList<WordNetSense> senses1 = new ArrayList<WordNetSense>();
        ArrayList<WordNetSense> senses2 = new ArrayList<WordNetSense>();

        for (int i=0; i<taggedWordsPrev1.size(); i++) {
            String word = taggedWordsPrev1.get(i).word();
            String posTag = taggedWordsPrev1.get(i).tag();
            if (posTag.length()>=2 && posTag.substring(0, 2).equals("NN")) {
                taggedWords1.add(new TaggedWord(word, "NN"));
                senses1.add(sensesPrev1[i]);
            }
            else if(posTag.length()>=2 && posTag.substring(0, 2).equals("VB")) {
                taggedWords1.add(new TaggedWord(word, "VB"));
                senses1.add(sensesPrev1[i]);
            }
        }
        for (int i=0; i<taggedWordsPrev2.size(); i++) {
            String word = taggedWordsPrev2.get(i).word();
            String posTag = taggedWordsPrev2.get(i).tag();
            if (posTag.length()>=2 && posTag.substring(0, 2).equals("NN")) {
                taggedWords2.add(new TaggedWord(word, "NN"));
                senses2.add(sensesPrev2[i]);
            }
            else if(posTag.length()>=2 && posTag.substring(0, 2).equals("VB")) {
                taggedWords2.add(new TaggedWord(word, "VB"));
                senses2.add(sensesPrev2[i]);
            }
        }

        //System.out.println(taggedWords1.size() + "," + taggedWords2.size());

        // array of edge weights with default weight 0
        int length1 = taggedWords1.size();
        int length2 = taggedWords2.size();
        int arrSize = Math.max(length1, length2);
        double[][] array = new double[arrSize][arrSize];
        for (int i=0; i<arrSize; i++) {
            for (int j=0; j<arrSize; j++) {
                array[i][j] = 0;
            }
        }
        for (int i=0; i<length1; i++) {
            for (int j=0; j<length2; j++) {
                String word1 = taggedWords1.get(i).word();
                String posTag1 = taggedWords1.get(i).tag();
                String word2 = taggedWords2.get(j).word();
                String posTag2 = taggedWords2.get(j).tag();
                double edgeWeight = 0;

                // LSA Similarity
                //edgeWeight = LSASimilarity.LSAWordSimilarity(word1, word2);

                // DISCO Similarity
                //DISCOSimilarity discoObj = new DISCOSimilarity();
                try {
                    if (word1.compareToIgnoreCase(word2) == 0)
                        edgeWeight = 1;
                    else {
                        //edgeWeight = ws.wuPalmerSimilarity(senses1.get(i), senses2.get(j));
                        edgeWeight = ws.linSimilarity(senses1.get(i), senses2.get(j));
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

                array[i][j] = edgeWeight;
            }
        }

        //System.out.println("Hungarian starts " + arrSize);

        double finalScore;
        String sumType = "max";
        int minLength = Math.min(length1, length2);
        //finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/minLength * 5;
        if (arrSize == 0)
            finalScore = 0;
        else
            finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/arrSize * 5;

        return finalScore;
    }

    public static ArrayList<TaggedWord> PreprocessPhrase(ArrayList<TaggedWord> taggedWords) {
        ArrayList<TaggedWord> newList = new ArrayList<TaggedWord>();
        taggedWords = Preprocess(taggedWords);

        for (int i=0; i<taggedWords.size(); i++) {
            String posTag = taggedWords.get(i).tag();
            if (posTag.length()>=2) {
                String reducedTag = posTag.substring(0, 2);
                if (reducedTag.equals("CD") || reducedTag.equals("NN") || reducedTag.equals("VB")) {
                    newList.add(taggedWords.get(i));
                }
            }
        }

        return newList;
    }

    public static ArrayList<ArrayList<TaggedWord>> getPhrases(Tree parse, int phraseSizeLimit) {
        ArrayList<ArrayList<TaggedWord>> newList = new ArrayList<ArrayList<TaggedWord>>();
        List<Tree> leaves = parse.getLeaves();

        if (leaves.size() <= phraseSizeLimit) {
            //ArrayList<TaggedWord> phraseElements = PreprocessPhrase(parse.taggedYield());
            ArrayList<TaggedWord> phraseElements = Preprocess(parse.taggedYield());
            if (phraseElements.size() > 0)
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

    public static double BestWordMatchEdgeWeight(ArrayList<TaggedWord> taggedWords1, ArrayList<TaggedWord> taggedWords2, DISCOSimilarity discoRAM) {
        double bestMatchScore = 0;
        for (int i=0; i<taggedWords1.size(); i++) {
            String word1 = taggedWords1.get(i).word();
            for (int j=0; j<taggedWords2.size(); j++) {
                String word2 = taggedWords2.get(j).word();
                double currentScore;
                if (word1.equals(word2))
                    currentScore = 1;
                else
                    currentScore = discoRAM.similarity2(word1, word2);

                if (currentScore > bestMatchScore)
                    bestMatchScore = currentScore;
            }
        }
        return bestMatchScore;
    }

    public static double LexicalSimilarity2Level(String sentence1, String sentence2, DISCOSimilarity discoRAM, LexicalizedParser lp) {
        Tree parse1 = lp.apply(sentence1);
        Tree parse2 = lp.apply(sentence2);

        int phraseSizeLimit = 2;

        ArrayList<ArrayList<TaggedWord>> phrasesList1 = getPhrases(parse1, phraseSizeLimit);
        ArrayList<ArrayList<TaggedWord>> phrasesList2 = getPhrases(parse2, phraseSizeLimit);

        int length1 = phrasesList1.size();
        int length2 = phrasesList2.size();
        int arrSize = Math.max(length1, length2);
        double[][] array = new double[arrSize][arrSize];
        for (int i=0; i<arrSize; i++) {
            for (int j=0; j<arrSize; j++) {
                array[i][j] = 0;
            }
        }
        for (int i=0; i<length1; i++) {
            for (int j=0; j<length2; j++) {
                double edgeWeight = 0;
                ArrayList<TaggedWord> taggedWords1 = phrasesList1.get(i);
                ArrayList<TaggedWord> taggedWords2 = phrasesList2.get(j);
                //edgeWeight = LexicalSimilarityScore(taggedWords1, taggedWords2, discoRAM, lp)/5.0;
                edgeWeight = BestWordMatchEdgeWeight(taggedWords1, taggedWords2, discoRAM);

                array[i][j] = edgeWeight;
            }
        }

        //System.out.println("Hungarian starts " + arrSize);

        double finalScore;
        String sumType = "max";
        //int minLength = Math.min(length1, length2);
        //finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/minLength * 5;
        if (arrSize == 0)
            finalScore = 0;
        else
            finalScore = HungarianAlgorithm.hgAlgorithm(array, sumType)/arrSize * 5;

        return finalScore;
    }

    public static void main(String args[]) {
//        String sentence1 = "A large bird standing on a table picks up a plastic glass containing liquid and places it in a bowl of something.";
//        String sentence2 = "A bird picks up a plastic cup containing a liquid with it's beak and puts the cup into a bowl.";
//        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
//        LeskWSD tm = new LeskWSD(lp);
//        WordNetSimilarity ws = new WordNetSimilarity();
//
//        System.out.println(LexicalSimilarityScoreWordNet(sentence1, sentence2, tm, lp, ws));
        String sentence = "The broader Standard & Poor's 500 Index <.SPX> shed 2.38 points, or 0.24 percent, at 995.10.";
        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
        Tree parse = lp.apply(sentence);
        ArrayList<TaggedWord> taggedWords = parse.taggedYield();
        taggedWords = Preprocess(taggedWords);
        for (int i=0; i<taggedWords.size(); i++)
            System.out.println(taggedWords.get(i).word());

    }

}