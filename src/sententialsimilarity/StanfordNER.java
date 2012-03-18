/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.Tree;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shrutiranjans
 */
public class StanfordNER {
    
    public static ArrayList<TaggedWord> parse(String sentence, LexicalizedParser lp, AbstractSequenceClassifier<CoreLabel> classifier) {
        
        List<List<CoreLabel>> newLabelList = classifier.classify(sentence);
        List<CoreLabel> newLabels = new ArrayList<CoreLabel>();
        
        for (List<CoreLabel> lcl : newLabelList)
            for (CoreLabel cl : lcl) {
                newLabels.add(cl);
                //System.out.println(cl.word() + " : " + cl.getString(AnswerAnnotation.class));
            }
        Tree parse = lp.apply(newLabels);
        List<CoreLabel> taggedLabels = parse.taggedLabeledYield();
        for (int i=0; i<taggedLabels.size(); i++)
            newLabels.get(i).setTag(taggedLabels.get(i).tag());
        
        ArrayList<TaggedWord> finalLabels = new ArrayList<TaggedWord>();        
        for(int i=0; i<newLabels.size(); i++) {
            CoreLabel cl = newLabels.get(i);
            if (cl.getString(AnswerAnnotation.class).equals("O"))
                finalLabels.add(new TaggedWord(cl.word(), cl.tag()));
            else {
                String currentNER = cl.getString(AnswerAnnotation.class);
                String word = "";
                String tag;
                
                boolean flag;
                if (currentNER.equals("LOCATION") || currentNER.equals("ORGANIZATION") || currentNER.equals("PERSON")) {
                    flag = true;
                    tag = "NNP";
                }
                else {
                    flag = false;
                    tag = "CD";
                }
                
                int j=i;
                for (; j<newLabels.size() && currentNER.equals(newLabels.get(j).getString(AnswerAnnotation.class)); j++) {
                    if (flag)
                        word += newLabels.get(j).word() + " ";
                    else
                        word += newLabels.get(j).word() + "_";                    
                }
                word = word.trim();
                finalLabels.add(new TaggedWord(word, tag));
                i = j-1;
            }
        }
        
        return finalLabels;
    }

    // hueristic - all words of MWEs
    public static ArrayList<TaggedWord> parseNERMWE(String sentence, LexicalizedParser lp, AbstractSequenceClassifier<CoreLabel> classifier) {
        ArrayList<TaggedWord> newList = new ArrayList<TaggedWord>();

        int phraseSizeLimit = 2;

        ArrayList<TaggedWord> listNER = LexicalSimilarity.Preprocess(parse(sentence, lp, classifier));
        ArrayList<ArrayList<TaggedWord>> listListMWE = LexicalSimilarity.getPhrases(lp.apply(sentence), phraseSizeLimit);

        // create an ArrayList for the multi-word expressions
        ArrayList<TaggedWord> listMWE = new ArrayList<TaggedWord>();
        for (int i=0; i<listListMWE.size(); i++) {
            ArrayList<TaggedWord> currentPhrase = listListMWE.get(i);
            int currentPhraseSize = currentPhrase.size();

            // hueristically extract tag of phrase
            String word = "";
            String tag = currentPhrase.get(currentPhraseSize-1).tag();

            for (int j=0; j<currentPhrase.size(); j++)
                word += currentPhrase.get(j).word() + " ";
            word = word.trim();
            listMWE.add(new TaggedWord(word, tag));
        }

//        System.out.println(listMWE);
//        System.out.println("**********");
//        System.out.println(listNER);
//        System.out.println("**********");

        for (int i=0; i<listNER.size()-phraseSizeLimit+1; i++) {
            boolean flag = false;
            String currentWord = "";
            String currentTag = listNER.get(i).tag();
            for (int j=i; j<i+phraseSizeLimit; j++)
                currentWord += listNER.get(j).word() + " ";
            currentWord = currentWord.trim();
            for (int j=0; j<listMWE.size(); j++) {
                TaggedWord tw = listMWE.get(j);
                String currentMWE = tw.word();

//                System.out.println("#" + currentMWE + "#");
//                System.out.println("#" + currentWord + "#");
//                System.out.println("************");

                if (currentMWE.compareToIgnoreCase(currentWord) == 0) {
                    currentTag = tw.tag();
                    flag = true;
                    break;
                }
            }

            if (flag) {
                //newList.add(new TaggedWord(listNER.get(i+phraseSizeLimit-1).word(), currentTag));
                newList.add(new TaggedWord(currentWord, currentTag));
                i += phraseSizeLimit-1;
            }
            else {
                newList.add(listNER.get(i));
            }

        }

        for (int i=listNER.size()-phraseSizeLimit+1; i<listNER.size(); i++) {
            newList.add(listNER.get(i));
        }

        return newList;
    }
    

    public static void testModule1() {
        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
        String sentence = "Ramesh Kulkarni and Donald Trump are very good friends.";
        
        String serializedClassifier = "english.muc.7class.distsim.crf.ser.gz";
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
        List<List<CoreLabel>> newLabelList = classifier.classify(sentence);
        List<CoreLabel> newLabels = new ArrayList<CoreLabel>();
        
        for (List<CoreLabel> lcl : newLabelList)
            for (CoreLabel cl : lcl) {
                newLabels.add(cl);
                //System.out.println(cl.word() + " : " + cl.getString(AnswerAnnotation.class));
            }
        Tree parse = lp.apply(newLabels);
        List<CoreLabel> taggedLabels = parse.taggedLabeledYield();
        for (int i=0; i<taggedLabels.size(); i++)
            newLabels.get(i).setTag(taggedLabels.get(i).tag());
        for (CoreLabel cl : newLabels)
            System.out.println(cl.word() + " : " + cl.tag() + " : " + cl.getString(AnswerAnnotation.class));
        
        for(int i=0; i<newLabels.size(); i++) {
            CoreLabel cl = newLabels.get(i);
            if (cl.getString(AnswerAnnotation.class).equals("O"))
                System.out.println(cl.word());
            else {
                int j=i;
                String currentNER = cl.getString(AnswerAnnotation.class);
                for (; j<newLabels.size() && currentNER.equals(newLabels.get(j).getString(AnswerAnnotation.class)); j++) {
                    //CoreLabel newCl = newLabels.get(j);
                    System.out.print(newLabels.get(j).word() + " ");                
                }
                System.out.println();
                i = j-1;
            }
        }
        
//        for (List<CoreLabel> lcl : classifier.classify(sentence)) {
//            for (CoreLabel cl : lcl) {
//                System.out.println(cl.word() + " : " + cl.getString(AnswerAnnotation.class));
//            }
//            //System.out.println();
//        }
    }
    
    public static void main(String args[]) {
        //testModule1();
        String serializedClassifier = "english.muc.7class.distsim.crf.ser.gz";
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
        String sentence = "Antonio Monteiro de Castro, 58, currently director of the group’s Latin America & Caribbean operations, will become chief operating officer from the same date.";
        for (TaggedWord tw : LexicalSimilarity.Preprocess(parse(sentence, new LexicalizedParser("englishPCFG.ser.gz"), classifier)))
            System.out.println(tw.word() + " : " + tw.tag());
    }
    
}
