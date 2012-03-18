/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sententialsimilarity;

import WordNet.WordNetSense;
import WordNet.WordNetType;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.smu.tspell.wordnet.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import opennlp.tools.tokenize.*;
import java.lang.String.*;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author shrutiranjans
 */
public class LeskWSD {
    
    LexicalizedParser lp;
    IDictionary dict;
    HashMap<String, String> stopwordsDict = new HashMap<String, String>();
    HashMap<String, String> punctuationMarks = new HashMap<String, String>();
    
    public LeskWSD(LexicalizedParser lp) {
        this.lp = lp;
        String path = "/usr/local/WordNet-3.0/dict/";
        URL url = null;
        try { 
            url = new URL("file", null, path); 
        } 
        catch(MalformedURLException e) {
            e.printStackTrace(); 
        }
        if(url == null) return;
    
        // construct the dictionary object and open it
        dict = new edu.mit.jwi.Dictionary(url);
        dict.open();
        
        try {
            String stopListPath = "data/nltk_stoplist.txt";
            File textFile = new File(stopListPath);
            BufferedReader br = new BufferedReader(new FileReader(textFile));
            String stopwordsLine = br.readLine();
            br.close();
            
            String[] stopwords = stopwordsLine.split(",");
            for (int i=0; i<stopwords.length; i++) {
                stopwordsDict.put(stopwords[i], stopwords[i]);
            }
            
            String[] punctuations = {",", ".", "?", "!", ":", ";", "\"", "-", "--", "'", "-LRB-", "-RRB-"};//, "/", "\\", "<", ">", "#", "&", "*", "(", ")", "{", "}", "[", "]", "~", "|"};
            for (int i=0; i<punctuations.length; i++) {
                punctuationMarks.put(punctuations[i], punctuations[i]);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public String[] Preprocess(String sentence)
    {
        TokenizerFactory<CoreLabel> tokenizerFactory =
		PTBTokenizer.factory(new CoreLabelTokenFactory(), "");        
        List<CoreLabel> rawWords2 =
		tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
        
        ArrayList<String> newList = new ArrayList<String>();
        
        try {
            for (int i=0; i<rawWords2.size(); i++) {
                String word = rawWords2.get(i).word().toLowerCase();
                
                if (word.equals("n't"))
                    word = "not";
                else if (word.equals("'s"))
                    word = "is";                
                
                if (!(stopwordsDict.containsKey(word) || punctuationMarks.containsKey(word))) {
                    newList.add(word);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        String[] tokens = new String[newList.size()];
        for (int i=0; i<tokens.length; i++) {
            tokens[i] = newList.get(i);
        }
        
        return tokens;
    }
    
    public int SynsetMatching(ISynset syn, Set sentence, String nv)
    {
        //sentence doesn't has the syn's word
        int score = 0;
        String[] desc = GetSynsetDesc(syn, nv);
        int scores[] = new int[desc.length];
        for(int j=0; j<desc.length;j++)
        {
            scores[j] = 0;
            String[] descrip = Preprocess(desc[j]);
            for(int i=0; i<descrip.length;i++)
            {
                if(sentence.contains(descrip[i])) scores[j] += 1;
            }
            score += scores[j];
        }        
        return score;
    }

    public String GetSynsetDescString(ISynset syn, String nv)
    {
        String[] descs = GetSynsetDesc(syn, nv);
        String desc = "";
        for(int j=0; j<descs.length;j++) desc+=descs[j];
        return desc;
    }

    public String[] GetSynsetDesc(ISynset syn, String nv)
    {
        String[] desc =  new String[6];
        for(int i=0; i<desc.length; i++) desc[i]="";
        desc[0] = syn.getGloss();
        if(nv.equals("NOUN"))
        {
            List<ISynsetID> hypernyms = syn.getRelatedSynsets(Pointer.HYPERNYM);
            for (int i=0; i<hypernyms.size(); i++)
                desc[1] += this.dict.getSynset(hypernyms.get(i)).getGloss() + ". ";
            
            List<ISynsetID> hyponyms = syn.getRelatedSynsets(Pointer.HYPONYM);
            for (int i=0; i<hyponyms.size(); i++)
                desc[2] += this.dict.getSynset(hyponyms.get(i)).getGloss() + ". ";
            
            List<ISynsetID> holonyms = syn.getRelatedSynsets(Pointer.HOLONYM_SUBSTANCE);
            for (int i=0; i<holonyms.size(); i++)
                desc[3] += this.dict.getSynset(holonyms.get(i)).getGloss() + ". ";
            
            List<ISynsetID> meronyms = syn.getRelatedSynsets(Pointer.MERONYM_SUBSTANCE);
            for (int i=0; i<meronyms.size(); i++)
                desc[4] += this.dict.getSynset(meronyms.get(i)).getGloss() + ". ";
            
//            NounSynset synN = (NounSynset)syn;
//            Synset[] hyponyms = synN.getHyponyms();
//            for(int i=0; i<hyponyms.length; i++) desc[1] += (hyponyms[i].getDefinition() + ". ");
//            
//            Synset[] hypernyms = synN.getHypernyms();
//            for(int i=0; i<hypernyms.length; i++) desc[2] += (hypernyms[i].getDefinition() + ". ");
//
//            Synset[] holonyms = synN.getSubstanceHolonyms();
//            for(int i=0; i<holonyms.length; i++) desc[3] += (holonyms[i].getDefinition()+". ");
//
//            Synset[] meronyms = synN.getSubstanceMeronyms();
//            for(int i=0; i<meronyms.length; i++) desc[4] += (meronyms[i].getDefinition()+". ");

            //Synset[] usages = synN.getUsages();
            //for(int i=0; i<usages.length; i++) desc[5] += (usages[i].getDefinition()+". ");
        }
        if(nv.equals("VERB"))
        {
            List<ISynsetID> entailments = syn.getRelatedSynsets(Pointer.ENTAILMENT);
            for (int i=0; i<entailments.size(); i++)
                desc[1] += this.dict.getSynset(entailments.get(i)).getGloss() + ". ";
            
            List<ISynsetID> hypernyms = syn.getRelatedSynsets(Pointer.HYPERNYM);
            for (int i=0; i<hypernyms.size(); i++)
                desc[2] += this.dict.getSynset(hypernyms.get(i)).getGloss() + ". ";
            
            List<ISynsetID> hyponyms = syn.getRelatedSynsets(Pointer.HYPONYM);
            for (int i=0; i<hyponyms.size(); i++)
                desc[3] += this.dict.getSynset(hyponyms.get(i)).getGloss() + ". ";
            
            List<ISynsetID> outcomes = syn.getRelatedSynsets(Pointer.CAUSE);
            for (int i=0; i<outcomes.size(); i++)
                desc[4] += this.dict.getSynset(outcomes.get(i)).getGloss() + ". ";
            
//            VerbSynset synV = (VerbSynset)syn;
//            Synset[] entails = synV.getEntailments();
//            for(int i=0; i<entails.length; i++) desc[1] += (entails[i].getDefinition() + ". ");
//
//            Synset[] hypernyms = synV.getHypernyms();
//            for(int i=0; i<hypernyms.length; i++) desc[2] += (hypernyms[i].getDefinition() + ". ");
//
//            Synset[] troponyms = synV.getTroponyms();
//            for(int i=0; i<troponyms.length; i++) desc[3] += (troponyms[i].getDefinition()+". ");
//
//            Synset[] outcomes = synV.getOutcomes();
//            for(int i=0; i<outcomes.length; i++) desc[4] += (outcomes[i].getDefinition()+". ");

            //Synset[] usages = synV.getUsages();
            //for(int i=0; i<usages.length; i++) desc[5] += (usages[i].getDefinition()+". ");
        }
        return desc;
    }
    
    public WordNetSense[] LeskJWI(String sentence)
    {
        ArrayList<TaggedWord> taggedWords = LexicalSimilarity.Preprocess(LexicalSimilarity.StanfordParse(sentence, lp));
        Set totalSet = new HashSet();
        WordNetSense[] senses = new WordNetSense[taggedWords.size()];
        try {
            //int[] senses = new int[processedSentence.length];
            for(int j=0; j<taggedWords.size(); j++) {
                String word = taggedWords.get(j).word();
                int sense = -1;
                WordNetType type = WordNetType.DEFAULT;
                senses[j] = new WordNetSense(word, sense, type); 
                totalSet.add(word);
            }

            for(int i=0; i<taggedWords.size(); i++)
            {
                String word = taggedWords.get(i).word();
                String posTag = taggedWords.get(i).tag();
                //System.out.println(posTag);
                Set newset = new HashSet(totalSet);
                newset.remove(word);

                POS pos = POS.NOUN;
                String partOfSpeech = "NOUN";
                WordNetType posType = WordNetType.NOUN;
                boolean flag = false;

                if(posTag.length()>=2 && posTag.substring(0, 2).equals("VB"))
                {
                    pos = POS.VERB;
                    partOfSpeech = "VERB";
                    posType = WordNetType.VERB;
                    flag = true;
                }
                else if(posTag.length()>=2 && posTag.substring(0, 2).equals("NN"))
                {
                    pos = POS.NOUN;
                    partOfSpeech = "NOUN";
                    posType = WordNetType.NOUN;
                    flag = true;
                }

                if (flag) {
                    List<IWordID> wordSenses = new ArrayList<IWordID>();
                    List<Integer> wordSenseNumbers = new ArrayList<Integer>();
                    WordnetStemmer wst = new WordnetStemmer(dict);
                    List<String> lemmas = wst.findStems(word, pos);
                    for (int j=0; j<lemmas.size(); j++) {
                        String lemmaWord = lemmas.get(j);
                        IIndexWord idxWord = dict.getIndexWord(lemmaWord, pos);
                        List<IWordID> currentSenses = idxWord.getWordIDs();
                        ArrayList<Integer> currentSenseNumbers = new ArrayList<Integer>();
                        for (int k=0; k<currentSenses.size(); k++)
                            currentSenseNumbers.add(new Integer(k+1));
                        wordSenseNumbers.addAll(currentSenseNumbers);
                        wordSenses.addAll(currentSenses);
                    }
                    int bestScore = 0;
                    String bestGlossary = "";
                    for(int j=0; j<wordSenses.size();j++)
                    {
                        IWordID currentSense = wordSenses.get(j);
                        IWord currentWordSense = dict.getWord(currentSense);
                        ISynset currentSynset = currentWordSense.getSynset();
                        int score = SynsetMatching(currentSynset, newset, partOfSpeech);
                        if(score>=bestScore) {
                            bestScore = score; 
                            senses[i].word = currentWordSense.getLemma();
                            senses[i].sense = wordSenseNumbers.get(j);
                            senses[i].type = posType;
                            bestGlossary = currentSynset.getGloss();
                        }
                    }
                    //System.out.println("best glossary for " + word + ": " + bestGlossary);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        for (int i=0; i<senses.length; i++) {
            
        }
        
        return senses;
    }

    public static void morphTest(LexicalizedParser lp) {
        //String sentence = "Ranjan, my insanely crazy friend, is slicing an onion.";
        String sentence = "Ranjan is running.";
        TokenizerFactory<CoreLabel> tokenizerFactory =
		PTBTokenizer.factory(new CoreLabelTokenFactory(), "");        
        List<CoreLabel> rawWords2 =
		tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
        Tree parse = lp.apply(rawWords2);
        ArrayList<TaggedWord> taggedWords = parse.taggedYield();
        
        for (int i=0; i<taggedWords.size(); i++) {
            String word = taggedWords.get(i).word();
            String tag = taggedWords.get(i).tag();
            String morphedWord = Morphology.lemmaStatic(word, tag, true);
            System.out.println(word + ", " + tag + ", " + morphedWord);
        }
    }
    
    public static void leskTest() {
        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
        LeskWSD testObj = new LeskWSD(lp);
        String sentence1 = "A bird picks up a plastic cup containing a liquid with it's beak and puts the cup into a bowl.";
        //String sentence2 = "The man is riding a bicycle";
        
        WordNetSense[] senses1 = testObj.LeskJWI(sentence1);  
        for (int i=0; i<senses1.length; i++)
            System.out.println(senses1[i].word + ", " + senses1[i].sense);
        
    }
    
    public static void main(String args[]) {
        
//        String sentence = "Ranjan is running.";
//        try {
//            
//            WordNetSense[] senses = Lesk(sentence);
//            for (int i=0; i<senses.length; i++)
//                System.out.println(senses[i].sense);
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
//        morphTest(lp);
        
        leskTest();
        
        
    }
    
}
