package Syntactics;

import WordSimilarity.DISCOSimilarity;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;


/**
 *
 * @author sumit
 */
public class WordOrderSimilarity
{
public static double[] WordOrderSimLiAndCosine(String[] first, String[] second, double threshold, DISCOSimilarity testObj )
    {
        /*
        Word Order Similarity as suggested by:
        Sentence Similarity Based on Semantic Nets and Corpus Statistics
        Yuhua Li, David McLean, Zuhair A. Bandar, James D. O’Shea, and Keeley Crockett
        */
        double toReturn[] = new double[2];
        Set totalSet = new HashSet();
        Set firstSet = new HashSet();
        Set secondSet = new HashSet();
        for(int i=0; i<first.length; i++)  {totalSet.add(first[i]) ;  firstSet.add(first[i]);}
        for(int i=0; i<second.length; i++) {totalSet.add(second[i]); secondSet.add(second[i]);}

        String all[] = new String[totalSet.size()];
        totalSet.toArray(all);

        double order1[] = new double[all.length];
        double order2[] = new double[all.length];
        double orderDiff = 0.0;
        double orderSum  = 0.0;

        for(int i=0; i<all.length; i++)
        {
            if(firstSet.contains(all[i])) order1[i]=1;
            else
            {
                for(int j=0;j<first.length;j++)
                {
                    if(WordSimilarity(first[j], all[i], testObj)>threshold) {order1[i]=1; break;}
                }
            }
            if(secondSet.contains(all[i])) order2[i]=1;
            else
            {
                for(int j=0;j<second.length;j++)
                {
                    if(WordSimilarity(second[j], all[i], testObj)>threshold) {order2[i]=1; break;}
                }
            }
            orderDiff += Math.pow(order1[i] - order2[i],2);
            orderSum  += Math.pow(order1[i] + order2[i],2);
        }

        double norm1 = 0.0;
        double norm2 = 0.0;
        double dotProduct = 0.0;
        for(int l=0;l<all.length;l++)
        {
            order1[l] = order1[l]*getInfoContent(all[l]);
            order2[l] = order2[l]*getInfoContent(all[l]);
            norm1 += Math.pow(order1[l],2);
            norm2 += Math.pow(order2[l],2);
            dotProduct += order1[l]*order2[l];
        }

        double cosineSim = dotProduct/(Math.pow(norm1, 0.5)*Math.pow(norm2, 0.5));
        double negScore = Math.sqrt(orderDiff)/Math.sqrt(orderSum);
        toReturn[0] = (1.0-negScore);
        toReturn[1] = cosineSim;
        return toReturn;
    }

    public static double[] SimZhang(String[] first, String[] second, DISCOSimilarity testObj )
    {
        //Word Order Similarity and Word Distance Based Similarity
        //Suggested by:
        //Calculating Statistical Similarity between Sentences
        //Junsheng Zhang, Yunchuan Sun, Huili Wang, Yanqing He

        Set firstSet = new HashSet();
        Set secondSet = new HashSet();
        for(int i=0; i<first.length; i++)
        {
            for(int j=i+1; j<first.length; j++)
            {
                firstSet.add(first[i]+"#"+first[j]);
            }
        }
        for(int i=0; i<second.length; i++)
        {
            for(int j=i+1; j<second.length; j++)
            {
                secondSet.add(second[i]+"#"+second[j]);
            }
        }
        Set union = new HashSet(firstSet);
        Set intersection = new HashSet(firstSet);
        union.addAll(secondSet);
        intersection.retainAll(secondSet);

        double scoreIntersection = 0.0;
        double firstScore = 0.0;
        double secondScore = 0.0;
        Iterator itI = intersection.iterator();
        while(itI.hasNext())
        {
            String[] words = (itI.next()).toString().split("#");
            scoreIntersection += Math.pow(WordSimilarity(words[0], words[1],testObj),2);
        }
        Iterator itF = firstSet.iterator();
        while(itF.hasNext())
        {
            String[] words = (itF.next()).toString().split("#");
            firstScore += Math.pow(WordSimilarity(words[0], words[1], testObj),2);
        }
        Iterator itS = secondSet.iterator();
        while(itS.hasNext())
        {
            String[] words = (itS.next()).toString().split("#");
            secondScore += Math.pow(WordSimilarity(words[0], words[1], testObj),2);
        }

        double[] toReturn = new double[2];
        toReturn[0] = (intersection.size()*1.0/union.size());
        toReturn[1] = scoreIntersection/(Math.pow(firstScore, 0.5)*Math.pow(secondScore, 0.5));
        return toReturn;
    }

    public static double[] JaccardDiceSentSim(String[] first, String[] second)
    {
        double[] scores = new double[2];
        Set firstSet = new HashSet();
        Set secondSet = new HashSet();
        firstSet.addAll(Arrays.asList(first));
        secondSet.addAll(Arrays.asList(second));
        Set union = new HashSet(firstSet);
        Set intersection = new HashSet(firstSet);
        union.addAll(secondSet);
        intersection.retainAll(secondSet);
        scores[0]=(intersection.size()*1.0/union.size());//Jaccard
        scores[1]=(intersection.size()*2.0/(firstSet.size()+secondSet.size()));//Dice
        return scores;
    }

    public static double getInfoContent(String word)
    {
        return 1;
    }

        public static double WordSimilarity(String word1, String word2, DISCOSimilarity testObj)
    {
        return testObj.similarity2(word1, word2);
    }

        public static String[] Preprocess(String sentence)
    {
        InputStream modelIn = null;
        String tokens[] = null;

        try { modelIn = new FileInputStream("data/en-token.bin"); }
        catch (FileNotFoundException ex) {ex.printStackTrace();       }

        try
        {
          TokenizerModel model = new TokenizerModel(modelIn);
          Tokenizer tokenizer = new TokenizerME(model);
          char punctuationMarks[] = {',', '.', '?', '!', ':', ';', '"'};
          sentence = sentence.trim();
          sentence = sentence.toLowerCase();
          for (int j=0; j<punctuationMarks.length; j++)
          {
            sentence = sentence.replace(punctuationMarks[j],' ');
          }
          tokens = tokenizer.tokenize(sentence);
        }
        catch (IOException e) {}
        finally
        {if (modelIn != null) { try { modelIn.close();} catch (IOException e) { }}}

        for (int i = 0; i < tokens.length; i++)
        {
            if(tokens[i].compareToIgnoreCase("n't")==0) tokens[i] = "not";
            //System.out.println(tokens[i]);
        }
        return tokens;
    }

public static int getLevenshteinDistance (String[] s, String[] t)
    {
      if (s.length == 0 || t.length == 0) {
        throw new IllegalArgumentException("Strings must not be null");
      }

      int n = s.length; // length of s
      int m = t.length; // length of t

      if (n == 0) {
        return m;
      } else if (m == 0) {
        return n;
      }

      int p[] = new int[n+1]; //'previous' cost array, horizontally
      int d[] = new int[n+1]; // cost array, horizontally
      int _d[]; //placeholder to assist in swapping p and d

      // indexes into strings s and t
      int i; // iterates through s
      int j; // iterates through t

      String t_j; // jth character of t

      int cost; // cost

      for (i = 0; i<=n; i++) {
         p[i] = i;
      }

      for (j = 1; j<=m; j++) {
         t_j = t[j-1];
         d[0] = j;

         for (i=1; i<=n; i++) {
            cost = (s[i - 1] == null ? t_j == null : s[i - 1].equals(t_j)) ? 0 : 1;
            // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
            d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);
         }

         // copy current distance counts to 'previous row' distance counts
         _d = p;
         p = d;
         d = _d;
      }

      // our last action in the above loop was to switch d and p, so p now
      // actually has the most recent cost counts
      return p[n];
    }

    public static void main(String args[]) throws FileNotFoundException, IOException
    {
        DISCOSimilarity testObj = new DISCOSimilarity();
        BufferedReader in = new BufferedReader(new FileReader("/home/sumit/Academics/8th sem/CS697/train/STS.input.MSRpar.txt"));
        String text = "";
        double scores[] = new double[2];
        PrintWriter out1 = new PrintWriter(new FileWriter("/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.MSRpar.LD.txt"));
        //PrintWriter out2 = new PrintWriter(new FileWriter("/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.MSRvid.WOCosineSim.txt"));
        //PrintWriter out3 = new PrintWriter(new FileWriter("/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.MSRvid.SimZhang1.txt"));
        //PrintWriter out4 = new PrintWriter(new FileWriter("/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.MSRvid.SimZhang2.txt"));
        //PrintWriter out5 = new PrintWriter(new FileWriter("/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.SMTeuroparl.Jaccard.txt"));
        //PrintWriter out6 = new PrintWriter(new FileWriter("/home/sumit/Academics/8th sem/CS697/train/STS.myOutput.SMTeuroparlyou.Dice.txt"));
        int count = 0 ;
        //System.out.println(getLevenshteinDistance(Preprocess("hello world"),Preprocess("Hello World")));
       
        while (in.ready())
        {
            System.out.println(count);
            count +=1 ;
            text = in.readLine();
            StringTokenizer tokenizer = new StringTokenizer(text,"\t");
            String s1 = tokenizer.nextToken();
            String s2 = tokenizer.nextToken();
            String[] preS1 = Preprocess(s1);
            String[] preS2 = Preprocess(s2);
            //scores = WordOrderSimLiAndCosine(preS1,preS2,0.7, testObj);
            
            double maxLength = Math.max(preS1.length, preS2.length);
            scores[0] = 1-(getLevenshteinDistance(preS1, preS2)/maxLength);
            out1.println(scores[0]);
            //out2.println(scores[1]);
            //scores = SimZhang(preS1,preS2, testObj);
            //out3.println(scores[0]);
            //out4.println(scores[1]);
            //scores = JaccardDiceSentSim(preS1, preS2);
            //out5.println(scores[0]);
            //out6.println(scores[1]);
        }
        out1.close();
        //out2.close();
        //out3.close();
        //out4.close();
        //out5.close();
        //out6.close();
        in.close();
        
    }
}

