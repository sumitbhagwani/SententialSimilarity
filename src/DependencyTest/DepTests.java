/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DependencyTest;

import WordSimilarity.DISCOSimilarity;
import WordSimilarity.LSASimilarity;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.lucene.queryParser.ParseException;
import WordSimilarity.LSASimilarity.*;
/**
 *
 * @author sumit
 */
public class DepTests {

    public static String[] getUsefulInfo(String sent1, LexicalizedParser lp, GrammaticalStructureFactory gsf, Boolean morph)
    {
        String[] toReturn= new String[4];//Subject, Verb, Direct Object, Indirect Object
        Tree parse = (Tree) lp.apply(sent1);
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = (List<TypedDependency>) gs.typedDependencies(true);
        Map<String, Integer> possibleNominalSubject = new HashMap<String, Integer>();
        Map<String, Integer> possibleMainVerb = new HashMap<String, Integer>();
        Map<String, Integer> possibleDirectObject = new HashMap<String, Integer>();
        Map<String, Integer> possibleIndirectObject = new HashMap<String, Integer>();

        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(),"");
        List<CoreLabel> rawWords1 = tokenizerFactory.getTokenizer(new StringReader(sent1)).tokenize();
        Tree parse1 = lp.apply(rawWords1);
        ArrayList<TaggedWord> taggedWords1 = parse1.taggedYield();

        //System.out.println(tdl);
        //System.out.println(sent1);
        for(TypedDependency td : tdl)
        {
            Integer freq = 0;
            
            String relation = td.reln().toString();
            int govIndex = Math.max(td.gov().index()-1,0);
            String governor = td.gov().value().toLowerCase().toString();
            int depIndex = Math.max(td.dep().index()-1,0);
            String dependent = td.dep().value().toLowerCase().toString();
            //System.out.println(govIndex + " " +governor);
            if(morph)
            {
                governor = Morphology.lemmaStatic(governor, taggedWords1.get(govIndex).tag(), true);
                dependent = Morphology.lemmaStatic(dependent, taggedWords1.get(depIndex).tag(), true);
            }
            if(relation.equalsIgnoreCase("agent"))//reverse subject object
            {
                freq = possibleMainVerb.get(governor);
                possibleMainVerb.put(governor, (freq == null) ? 1 : freq + 1);

                freq = possibleNominalSubject.get(dependent);
                possibleNominalSubject.put(dependent, (freq == null) ? 1 : freq + 1);
            }
            else if (relation.equalsIgnoreCase("dobj"))
            {
                freq = possibleMainVerb.get(governor);
                possibleMainVerb.put(governor, (freq == null) ? 1 : freq + 1);

                freq = possibleDirectObject.get(dependent);
                possibleDirectObject.put(dependent, (freq == null) ? 1 : freq + 1);
            }
            else if(relation.equalsIgnoreCase("iobj"))
            {
                freq = possibleMainVerb.get(governor);
                possibleMainVerb.put(governor, (freq == null) ? 1 : freq + 1);

                freq = possibleIndirectObject.get(dependent);
                possibleIndirectObject.put(dependent, (freq == null) ? 1 : freq + 1);
            }
            else if(relation.equalsIgnoreCase("nsubj"))
            {
                freq = possibleMainVerb.get(governor);
                possibleMainVerb.put(governor, (freq == null) ? 1 : freq + 1);

                freq = possibleNominalSubject.get(dependent);
                possibleNominalSubject.put(dependent, (freq == null) ? 1 : freq + 1);
            }
            else if(relation.equalsIgnoreCase("nsubjpass"))//Reverse subject and object
            {
                freq = possibleMainVerb.get(governor);
                possibleMainVerb.put(governor, (freq == null) ? 1 : freq + 1);

                freq = possibleDirectObject.get(dependent);
                possibleDirectObject.put(dependent, (freq == null) ? 1 : freq + 1);
            }
            else if(relation.equalsIgnoreCase("pobj"))
            {
                freq = possibleDirectObject.get(dependent);
                possibleDirectObject.put(dependent, (freq == null) ? 1 : freq + 1);
            }
            else if(relation.equalsIgnoreCase("prt"))
            {
                freq = possibleDirectObject.get(governor);
                possibleDirectObject.put(governor, (freq == null) ? 1 : freq + 1);
            }
            else if(relation.equalsIgnoreCase("advcl"))
            {
                freq = possibleMainVerb.get(governor);
                possibleMainVerb.put(governor, (freq == null) ? 1 : freq + 1);
            }  
        }
        //Subject, Verb, Direct Object, Indirect Object
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : possibleNominalSubject.entrySet())
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) maxEntry = entry;
        if(maxEntry!= null) toReturn[0] = maxEntry.getKey();

        maxEntry = null;
        for (Map.Entry<String, Integer> entry : possibleMainVerb.entrySet())
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) maxEntry = entry;
        if(maxEntry!= null) toReturn[1] = maxEntry.getKey();

        maxEntry = null;
        for (Map.Entry<String, Integer> entry : possibleDirectObject.entrySet())
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) maxEntry = entry;
        if(maxEntry!= null) toReturn[2] = maxEntry.getKey();

        maxEntry = null;
        for (Map.Entry<String, Integer> entry : possibleIndirectObject.entrySet())
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) maxEntry = entry;
        if(maxEntry!= null) toReturn[3] = maxEntry.getKey();

        //System.out.println(toReturn[0]+" "+toReturn[1]+" "+toReturn[2]+" "+toReturn[3]);

        return toReturn;
    }

    public static void getUsefulInfoTest()
    {
        String sent1 = "But other sources close to the sale said Vivendi was keeping the door open to further bids and hoped to see bidders interested in individual assets team up.";
        String sent2 = "But other sources close to the sale said Vivendi was keeping the door open for further bids in the next day or two.";
        String PCFGPath = "/home/sumit/Academics/8th sem/CS697/code/stanford-parser-2012-02-03/grammar/englishPCFG.ser.gz";
        LexicalizedParser lp = new LexicalizedParser(PCFGPath);
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        DISCOSimilarity testObj = new DISCOSimilarity();

        String []data = new String[4];
        data = getUsefulInfo(sent1,lp,gsf,false);
        //Subject, Verb, Direct Object, Indirect Object
        for(int j=0; j<data.length; j++)
            System.out.println(data[j]);

        System.out.println();

        data = getUsefulInfo(sent2,lp,gsf,false);
        //Subject, Verb, Direct Object, Indirect Object
        for(int j=0; j<data.length; j++)
            System.out.println(data[j]);
    }

    public static Collection getDependencies(String sent1, LexicalizedParser lp, GrammaticalStructureFactory gsf)
    {
        Tree parse = (Tree) lp.apply(sent1);
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCollapsed(false);// allTypedDependencies();
        //System.out.println(tdl);
        ArrayList<TypedDependency> tdl2 = new ArrayList<TypedDependency>();
        for(TypedDependency td : tdl)
        {
            
            if(td.reln().toString() == null ? "root" != null : !td.reln().toString().equals("root"))
                tdl2.add(td);
        }
        //Collection tdl = gs.typedDependencies();
        //Collection tdl = gs.dependencies();
         
        System.out.println(tdl2);
        return tdl2;
    }

    public static double dependencyMatching(String sent1, String sent2, LexicalizedParser lp, GrammaticalStructureFactory gsf, DISCOSimilarity testObj)
    {
        HashSet C1 = new HashSet(getDependencies(sent1, lp, gsf));
        HashSet C2 = new HashSet(getDependencies(sent2, lp, gsf));
        HashSet Common = new HashSet(C1);
        //Common.retainAll(C2);
        //System.out.println(C1);
        //System.out.println(C2);
        //System.out.println(Common);
        double common = 0;
        for(Object td1 : C1)
        {
            //TypedDependency td11 = (TypedDependency) td1;
            MyDependency td11 = new MyDependency((TypedDependency)td1);
            for(Object td2 : C2)
            {
                MyDependency td22 = new MyDependency((TypedDependency)td2);
                //TypedDependency td22 = (TypedDependency) td2;
                double score = td11.compareTo(td22, testObj);
                if(score>0)
                {
                    //common += 1;
                    common += score;
                }
            }
        }
        return common/(C1.size()*C2.size());
    }

    public static void dependencyMatchingTest()
    {

        String sent1 = "This is an apple.";
        String sent2 = "This is a juicy tasty apple.";
        String PCFGPath = "/home/sumit/Academics/8th sem/CS697/code/stanford-parser-2012-02-03/grammar/englishPCFG.ser.gz";
        LexicalizedParser lp = new LexicalizedParser(PCFGPath);
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        DISCOSimilarity testObj = new DISCOSimilarity();

        System.out.println(dependencyMatching(sent1, sent2, lp, gsf,testObj));

    }

    public static double wordSimilarity(String word1, String word2, DISCOSimilarity testObj)
    {
        return testObj.similarity2(word1, word2);
    }

    public static double rootSimilarity(String sent1, String sent2, LexicalizedParser lp, GrammaticalStructureFactory gsf, DISCOSimilarity testObj)
    {
        String root1 = "";
        String root2 = "";
        try{
        Tree parse = (Tree) lp.apply(sent1);
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCollapsed(false);// allTypedDependencies();
        for(TypedDependency td : tdl)
        {
            if(td.reln().toString() == null ? "root" == null : td.reln().toString().equals("root"))
            {
                root1 = td.dep().value().toLowerCase();
                break;
            }
        }
        }
        catch(Exception e)
        {e.printStackTrace();}
        Tree parse2 = (Tree) lp.apply(sent1);
        GrammaticalStructure gs2 = gsf.newGrammaticalStructure(parse2);
        List<TypedDependency> tdl2 = gs2.typedDependenciesCollapsed(false);// allTypedDependencies();
        for(TypedDependency td : tdl2)
        {
            if(td.reln().toString() == null ? "root" == null : td.reln().toString().equals("root"))
            {
                root2 = td.dep().value().toLowerCase();
                break;
            }
        }
        return testObj.similarity2(root1, root2);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        //dependencyMatchingTest();
        //getUsefulInfoTest();
        String PCFGPath = "/home/sumit/Academics/8th sem/CS697/code/stanford-parser-2012-02-03/grammar/englishPCFG.ser.gz";
        LexicalizedParser lp = new LexicalizedParser(PCFGPath);
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        DISCOSimilarity testObj = new DISCOSimilarity();

        BufferedReader in = new BufferedReader(new FileReader("/home/sumit/Academics/8th sem/CS697/train/STS.input.MSRpar.txt"));
        
        String text = "";
        PrintWriter out1 = new PrintWriter(new FileWriter("/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.MSRpar.LSA.SVDOIO.txt"));
        int count = 0 ;
        while (in.ready())
        {
            System.out.println(count);
            count +=1;
            //if(count>100) break;
            text = in.readLine();
            StringTokenizer tokenizer = new StringTokenizer(text,"\t");
            String s1 = tokenizer.nextToken();
            String s2 = tokenizer.nextToken();
            s1=s1.replaceAll("[<>]","");
            s2=s2.replaceAll("[<>]","");
            //System.out.println(s1);
            //System.out.println(s2);

            String[] data1 = getUsefulInfo(s1, lp, gsf,true);
            String[] data2 = getUsefulInfo(s2, lp, gsf,true);
            double score = 0.0;
            int matched = 0;
            double simScore = 0;
            for(int j =0; j<4; j++)
            {
                if(data1[j]!=null && data2[j]!=null)
                {
                    //score += wordSimilarity(data1[j], data2[j],testObj);
                    simScore  = LSASimilarity.LSAWordSimilarity(data1[j], data2[j]);
                    //if(data1[j] == null ? data2[j] == null : data1[j].equals(data2[j])) { score+=1; }
                    matched += 1;
                    //System.out.println(data1[j]+"  "+data2[j]);
                }
            }
            //System.out.println(matched + " "+ score);
            if(matched == 0) out1.println(0);
            else out1.println(simScore/matched);
            //double score = rootSimilarity(s1, s2, lp, gsf, testObj);
            //String alphaAndDigits = s1.replaceAll("[^\\w\\s,]+","");
            //System.out.println(alphaAndDigits);
            //alphaAndDigits = s2.replaceAll("[^\\p{Alpha}\\p{Digit}]+","");
            //System.out.println(alphaAndDigits);
            //out1.println(score);
            //break;
        }
        System.out.println("Done!");
        out1.close();
        in.close();    
    }

}
