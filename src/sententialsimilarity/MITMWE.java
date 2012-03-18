/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sententialsimilarity;

import edu.mit.jmwe.data.IMWE;
import edu.mit.jmwe.data.IToken;
import edu.mit.jmwe.data.Token;
import edu.mit.jmwe.detect.CompositeDetector;
import edu.mit.jmwe.detect.Consecutive;
import edu.mit.jmwe.detect.IMWEDetector;
import edu.mit.jmwe.detect.InflectionPattern;
import edu.mit.jmwe.detect.MoreFrequentAsMWE;
import edu.mit.jmwe.detect.ProperNouns;
import edu.mit.jmwe.index.IMWEIndex;
import edu.mit.jmwe.index.MWEIndex;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.Tree;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author sumit
 */
public class MITMWE
{
    static HashSet<String> punctuationsAndSpecialCharacters = new HashSet<String>(Arrays.asList(",", ".", "?", "!", ":", ";", "\"", "-", "--", "'", "-LRB-", "-RRB-", "''", "``", "&"));
    public static List<IMWE<IToken>> detectorExample(IMWEDetector detector, String sentence, LexicalizedParser lp) throws IOException
    {
        Tree parse = lp.apply(sentence);
        ArrayList<TaggedWord> taggedWords = parse.taggedYield();
        
        // construct a test sentence :
        // " She looked up the world record ."
        List < IToken > sentence2 = new ArrayList < IToken >() ;
//        sentence2.add(new Token ( " She " ,    " DT " ) ) ;
//        sentence2.add(new Token ( " looked " , " VBD " , " look " ) ) ;
//        sentence2.add(new Token ( " up " ,     " RP " ) ) ;
//        sentence2.add(new Token ( " the " ,    " DT " ) ) ;
//        sentence2.add(new Token ( " world " , " NN " ) ) ;
//        sentence2.add(new Token ( " record " , " NN " ) ) ;
//        sentence2.add(new Token ( " . " ,    "."));

        for ( TaggedWord tw : taggedWords )
        {
            String word = tw.word();
            String tag = tw.tag();
            if(!punctuationsAndSpecialCharacters.contains(word) || punctuationsAndSpecialCharacters.contains(tag))
            {
                String morph = Morphology.lemmaStatic(word, tag, true);
                Token token = new Token(word,tag,morph);
                System.out.println(token);
                sentence2.add(token);
            }
        }

        // run detector and print out results
        List<IMWE<IToken>> mwes = detector.detect(sentence2) ;
        return mwes;
    }

    public static void mweTest(String inputFile, String outputFile, IMWEDetector detector, LexicalizedParser lp )
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
            String eachLine;
            int count = 1;
            while ((eachLine = br.readLine()) != null)
            {
                System.out.println(count);
                String[] sentences = eachLine.split("\t");
                //List<IMWE<IToken>> mwes0 = detectorExample(detector,sentences[0],lp);
                //List<IMWE<IToken>> mwes1 = detectorExample(detector,sentences[1],lp);
                Tree parse0 = lp.apply(sentences[0]);
                Tree parse1 = lp.apply(sentences[1]);
                ArrayList<ArrayList<TaggedWord>> tw0 = detectMWE(parse0.taggedYield(), detector);
                ArrayList<ArrayList<TaggedWord>> tw1 = detectMWE(parse1.taggedYield(), detector);
                for(TaggedWord tw : tw0.get(0)) bw.write(tw.toString()+" : ");
                bw.write(" | ");
                for(TaggedWord tw : tw1.get(0)) bw.write(tw.toString()+" : ");
                bw.write("\n");
                count += 1;
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public static void mweTest() throws IOException
    {
        String inputFile1 = "/home/sumit/Academics/8th sem/CS697/train/STS.input.MSRpar.txt";
        String inputFile2 = "/home/sumit/Academics/8th sem/CS697/train/STS.input.MSRvid.txt";
        String inputFile3 = "/home/sumit/Academics/8th sem/CS697/train/STS.input.SMTeuroparl.txt";

        String outputFile1 = "/home/sumit/Academics/8th sem/CS697/train/STS.MITMWE.MSRparDISCO.txt";
        String outputFile2 = "/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.MSRvidDISCO.txt";
        String outputFile3 = "/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.SMTeuroparlDISCO.txt";

        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");

        // get handle to file containing MWE index data ,
        // e . g . , mw eindex _wordn et3 .0 _Semcor1 .6. data
        File idxData = new File("data/mweindex_wordnet3.0_semcor1.6.data");

        // construct an MWE index and open it
        IMWEIndex index = new MWEIndex(idxData) ;
        index.open();

        // make some detectors
        IMWEDetector detector = new Consecutive( index ) ;
//        IMWEDetector pnDetector = ProperNouns.getInstance () ;
//        IMWEDetector goodDetector = new MoreFrequentAsMWE(new InflectionPattern(new Consecutive(index)));
//        IMWEDetector d2 = new CompositeDetector(pnDetector ,goodDetector);
//
        mweTest(inputFile1, outputFile1, detector, lp);
    }

    public static ArrayList<ArrayList<TaggedWord>> detectMWE(ArrayList<TaggedWord> taggedWords, IMWEDetector detector)
    {
        List < IToken > sentence2 = new ArrayList < IToken >() ;
        for ( TaggedWord tw : taggedWords )
        {
            String word = tw.word();
            String tag = tw.tag();
            if(!punctuationsAndSpecialCharacters.contains(word) || punctuationsAndSpecialCharacters.contains(tag))
            {
                String morph = Morphology.lemmaStatic(word, tag, true);
                Token token = new Token(word,tag,morph);
                //System.out.println(token);
                sentence2.add(token);
            }
        }

        // run detector and get mwes
        List<IMWE<IToken>> mwes = detector.detect(sentence2) ;

        ArrayList<TaggedWord> toReturn = new ArrayList<TaggedWord>();
        ArrayList<TaggedWord> toReturn2 = new ArrayList<TaggedWord>();

        for ( IMWE <IToken> mwe : mwes )
        {
            String mweString = mwe.getEntry().toString();
            //System.out.println(mweString);
            List<IToken> originalTokens = mwe.getTokens();
            //System.out.println(originalTokens);
            int len = mweString.length();
            String mweCategory = mweString.charAt(len-1)=='V'?"VB":"NN";
            String mweStringReqd = "";
            for(IToken it : originalTokens) mweStringReqd = mweStringReqd + it.getForm() + " ";
            mweStringReqd = mweStringReqd.trim();
            TaggedWord tw = new TaggedWord(mweStringReqd, mweCategory);
            String[] contents = mweString.split("_");
            int len2 = contents.length;
//            String mweCategory = contents[len-1];
            contents[len2-1] = "";
            String mweStringReqd2 = "";
            for(String s : contents) mweStringReqd2 = mweStringReqd2 + s + " ";
            mweStringReqd2 = mweStringReqd2.trim();
            TaggedWord tw2 = new TaggedWord(mweStringReqd2, mweCategory);
//            System.out.println(tw);
//            System.out.println(mweStringReqd);
//            System.out.println(mweCategory);
//            System.out.println(mwe.getTokens());
            toReturn.add(tw);
            toReturn2.add(tw2);
        }
        ArrayList<ArrayList<TaggedWord>> toRet = new ArrayList<ArrayList<TaggedWord>>();
        toRet.add(toReturn);
        toRet.add(toReturn2);
        return toRet;
        
    }

    public static void main(String [] args) throws IOException
    {
        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");

        // get handle to file containing MWE index data ,
        // e . g . , mw eindex _wordn et3 .0 _Semcor1 .6. data
        File idxData = new File("data/mweindex_wordnet3.0_semcor1.6.data");

        // construct an MWE index and open it
        IMWEIndex index = new MWEIndex(idxData) ;
        index.open () ;

        // make some detectors
        IMWEDetector detector = new Consecutive(index) ;
        //IMWEDetector pnDetector = ProperNouns.getInstance () ;
        //IMWEDetector goodDetector = new MoreFrequentAsMWE(new InflectionPattern(new Consecutive(index)));
        //IMWEDetector d2 = new CompositeDetector(pnDetector ,goodDetector);

        String sentence = "Still, he said, \"I'm absolutely confident we're going to have a bill.\"";
        //String sentence = "The company posted a profit of $54.3 million, or 22 cents per share, in the year-ago period.";

//        List<IMWE<IToken>>mwes = detectorExample(detector,sentence, lp);
//        for ( IMWE <IToken> mwe : mwes )
//        {
//            String mweString = mwe.getEntry().toString();
//            String[] contents = mweString.split("_");
//            int len = contents.length;
//            String mweCategory = contents[len-1];
//            contents[len-1] = "";
//            String mweStringReqd = "";
//            for(String s : contents) mweStringReqd = mweStringReqd + s + " ";
//            mweStringReqd = mweStringReqd.trim();
//            TaggedWord tw = new TaggedWord(mweStringReqd, mweCategory.equals("V")?"VB":"NN");
//            System.out.println(tw);
//            System.out.println(mweStringReqd);
//            System.out.println(mweCategory);
//            System.out.println(mwe.getTokens());
//        }

        Tree parse = lp.apply(sentence);
        ArrayList<TaggedWord> taggedWords = parse.taggedYield();
        ArrayList<ArrayList<TaggedWord>> reqd = detectMWE(taggedWords, detector);
//        for(ArrayList<TaggedWord> al : reqd)
//        {
//            for( TaggedWord tw : al) System.out.println(tw);
//        }
        for( TaggedWord tw : reqd.get(0)) System.out.println(tw);

        
        //mweTest();


    }
}
