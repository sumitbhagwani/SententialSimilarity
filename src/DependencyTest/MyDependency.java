package DependencyTest;

import WordSimilarity.DISCOSimilarity;
import edu.stanford.nlp.trees.TypedDependency;

/**
 *
 * @author sumit
 */
public class MyDependency
{
    String relation = "";
    String word1 = "";
    String word2 = "";

    public MyDependency(TypedDependency td)
    {
        this.relation = td.reln().toString();
        this.word1 = td.dep().label().value();
        this.word2 = td.gov().label().value();
    }


    public MyDependency(String relation, String word1, String word2)
    {
        this.relation = relation;
        this.word1 = word1;
        this.word2 = word2;
    }

    public double compareTo(MyDependency d, DISCOSimilarity testObj)
    {
        System.out.print(this.word1+" "+d.word1 + " ## " + this.word2+" "+d.word2+ " ");
        if(this.relation.equalsIgnoreCase(d.relation))
        {
            //DISCOSimilarity testObj = new DISCOSimilarity();
            double score1,score2 = 0.0;
            if(d.word1.equalsIgnoreCase(this.word1))
                score1 = 1.0;
            else
                score1 = testObj.similarity2(d.word1,this.word1);
            if(d.word2.equalsIgnoreCase(this.word2))
                score2 = 1.0;
            else
                score2 = testObj.similarity2(d.word2,this.word2);
            System.out.println(score1*score2);
            return score1*score2;
        }
        System.out.println(-1);
        return -1;
    }

    //public static void MyDependencyTest(TypedDependency td1, TypedDependency td2)
    //{

    //}


    public static void main(String args[])
    {
        //MyDependencyTest();
    }


}
