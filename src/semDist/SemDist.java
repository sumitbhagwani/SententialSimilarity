/*
 * 
 *    Copyright (C) 2009
 *    Mario Jarmasz, Alistair Kennedy and Stan Szpakowicz
 *    School of Information Technology and Engineering (SITE)
 *    University of Ottawa, 800 King Edward Avenue
 *    Ottawa, Ontario, Canada, K1N 6N5
 *    
 *    and
 *    
 *    Olena Medelyan
 *    Department of Computer Science,
 *    The University of Waikato
 *    Private Bag 3105, Hamilton, New Zealand
 *    
 *    This file is part of Open Roget's Thesaurus ELKB.
 *    
 *    Copyright (c) 2009, Mario Jarmasz, Alistair Kennedy, Stan Szpakowicz and Olena Medelyan
 *    All rights reserved.
 *    
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *        * Neither the name of the University of Ottawa nor the
 *          names of its contributors may be used to endorse or promote products
 *          derived from this software without specific prior written permission.  
 *        * All advertising materials mentioning features or use of this software
 *          must display the following acknowledgement:
 *          This product includes software developed by the University of
 *          Ottawa and its contributors.
 *    
 *    THIS SOFTWARE IS PROVIDED BY Mario Jarmasz, Alistair Kennedy, Stan Szpakowicz 
 *    and Olena Medelyan ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 *    BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 *    A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Mario Jarmasz, Alistair 
 *    Kennedy, Stan Szpakowicz and Olena Medelyan BE LIABLE FOR ANY DIRECT, 
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 *    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 *    OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *    EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *     
 */

package semDist;
import java.io.*;
import java.util.*;

import ca.site.elkb.*;

/*******************************************************************************
 * SemDist : program that performs various experiments to calculate semantic
 * distance. This program was used to obtain the results of Roget's Thesaurus
 * and Semantic Similarity paper which can be found at:
 * http://www.site.uottawa.ca/~mjarmasz/pubs/jarmasz_roget_sim.pdf
 * 
 * @author Mario Jarmasz & Alistair Kennedy
 * @version 1.2 Nov 2008 Usage : java SemDist <inputFile> Format of input file:
 *          pairs of comma separated words and phrases, one pair per line. Extra
 *          information can be contained on the line as long as it is separated
 *          by a comma, for example: car,automobile,3.92
 ******************************************************************************/

public class SemDist implements Distance{
	private RogetELKB elkb;	//
	
	private boolean MORPHOLOGY;

	/**
	 * Constructs a new SemDist object.  Requires a year, either 1911 or 1987
	 * to be passed to it.
	 * 
	 * @param year
	 * @deprecated
	 */
	@Deprecated
	public SemDist(int year) {
		// Initialize Roget
		elkb = new RogetELKB(year);
		MORPHOLOGY = false;
	}
	
	/**
	 * Constructs a new SemDist object.  Requires a year, either 1911 or 1987
	 * to be passed to it.  Second parameter for morphology to be applied to
	 * searches.
	 * 
	 * @param year
	 * @param morph
	 * @deprecated
	 */
	@Deprecated
	public SemDist(int year, boolean morph) {
		// Initialize Roget
		elkb = new RogetELKB(year);
		MORPHOLOGY = morph;
	}
	
	/**
	 * Constructor for a new SemDist object.  By default uses 1911 Thesaurus.
	 */
	public SemDist() {
		// Initialize Roget
		elkb = new RogetELKB();
		MORPHOLOGY = false;
	}
	
	/**
	 * Constructor for a new SemDist object.  By default uses 1911 Thesaurus.
	 * Second parameter for morphology to be applied to searches.
	 */
	public SemDist(boolean morph) {
		// Initialize Roget
		elkb = new RogetELKB();
		MORPHOLOGY = morph;
	}

	/**
	 * The Main method runs the program.
	 * @param args
	 */
	public static void main(String args[]) {
		//double startTime = 0;
		try{
			if(args.length == 1){
				SemDist semDist = new SemDist();
				//startTime = System.currentTimeMillis();
				wordPairs(semDist, args[0]);
			}
			else if(args.length == 2){
				SemDist semDist = new SemDist(Integer.parseInt(args[0]));
				//startTime = System.currentTimeMillis();
				wordPairs(semDist, args[1]);
			}
			else{
				System.out.println("Usage: java -cp .:rogets-1.2.jar semDist.SemDist <filename>");
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}

	    //double elapsedTime = System.currentTimeMillis() - startTime;
	    //double seconds = elapsedTime / 1.0E03;
	    //System.err.println(seconds);
	}

	/**
	 * This method loads a file formatted to deal with Miller and Charles type experiment.
	 * Each line is of the type:
	 * <word1>,<word2>,<distance>
	 * where word1 and word2 are strings and distance is a real number.  The output from
	 * this method is the word pair with the human score, and a score assigned by the
	 * semantic similarity measure.
	 * 
	 * @param fileName
	 */
	private static void wordPairs(SemDist sd, String fileName) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));

			for (;;) {
				String line = br.readLine();

				if (line == null) {
					br.close();
					break;
				} else {
					String[] st = line.split(",");
					//StringTokenizer st = new StringTokenizer(line, ",");
					String word1 = st[0];//st.nextToken().trim();
					String word2 = st[1];//st.nextToken().trim();
					String value = st[2];//st.nextToken().trim();

					// in all of these experiments we're only dealing
					// with pairs of nouns
					int score = sd.getSimilarity(word1, word2, "N.");
					System.out.println(word1 + "\t" + word2 + "\t" + value + "\t" + score);
					//break;
				}
			}
		} catch (Exception e) {
			System.out.println("Error:" + e);
		}
	}
	
	/**
	 * Obtains the maximum similarity between two strings, passed as parameters.  
	 * All Parts of speech are considered.  The returned value is an integer valued
	 * 0, 2, ..., 16, where 16 is the most similar.
	 * 
	 * @param word1
	 * @param word2
	 * @return semantic relatedness between the words
	 */
	public int getSimilarity(String word1, String word2){
		ArrayList<int[]> list1 = elkb.index.getEntryListNumerical(word1, MORPHOLOGY);
		ArrayList<int[]> list2 = elkb.index.getEntryListNumerical(word2, MORPHOLOGY);
		if(list1.size() == 0 || list2.size() == 0){
			//return -1;
			return 0;
		}
		int best = 0;
		for (int i = 0; i < list1.size(); i++) {
			int[] entry1 = list1.get(i);
			for (int j = 0; j < list2.size(); j++) {
				int[] entry2 = list2.get(j);
				int diff = 16;
				for (int k = 0; k < 8; k++) {
					if (entry1[k] != entry2[k]){
						if(2*k < diff){
							diff = 2*k;
						}
					}
				}
				if(best < diff){
					best = diff;
				}
			}
		}
		return best;
	}
	
	/**
	 * Obtains the part of speech for the closest of these two words
	 * 
	 * @param word1
	 * @param word2
	 * @return string array with POS's
	 */
	public String[] getClosestPOS(String word1, String word2){
		String[] toReturn = new String[2];
		ArrayList<int[]> list1 = elkb.index.getEntryListNumerical(word1, MORPHOLOGY);
		ArrayList<int[]> list2 = elkb.index.getEntryListNumerical(word2, MORPHOLOGY);
		if(list1.size() == 0 || list2.size() == 0){
			return toReturn;
			//return 0;
		}
		int best = 0;
		String pos1 = "";
		String pos2 = "";
		for (int i = 0; i < list1.size(); i++) {
			int[] entry1 = list1.get(i);
			for (int j = 0; j < list2.size(); j++) {
				int[] entry2 = list2.get(j);
				int diff = 16;
				for (int k = 0; k < 8; k++) {
					if (entry1[k] != entry2[k]){
						if(2*k < diff){
							diff = 2*k;
						}
					}
				}
				if(best < diff){
					pos1 = elkb.index.convertToPOS(entry1[5]);
					pos2 = elkb.index.convertToPOS(entry2[5]);
					best = diff;
				}
			}
		}
		toReturn[0] = pos1;
		toReturn[1] = pos2;
		return toReturn;
	}
	
	
	/**
	 * Obtains the maximum similarity between two strings, passed as parameters.  
	 * Only words of a given part of speech  are considered.  The returned value 
	 * is an integer valued 0, 2, ..., 16, where 16 is the most similar.
	 * 
	 * @param word1
	 * @param word2
	 * @param pos
	 * @return semantic relatedness between the words
	 */
	public int getSimilarity(String word1, String word2, String pos) {
		return getSimilarity(word1, pos, word2, pos);
	}
	
	/**
	 * Obtains the maximum similarity between two strings, passed as parameters.  
	 * Each word has a specified part of speech, other POS's are not considered.  
	 * The returned value is an integer valued 0, 2, ..., 16, where 16 is the most similar.
	 * 
	 * @param word1
	 * @param pos1
	 * @param word2
	 * @param pos2
	 * @return semantic relatedness between the words
	 */
	public int getSimilarity(String word1, String pos1, String word2, String pos2) {
		ArrayList<int[]> list1 = elkb.index.getEntryListNumerical(word1, MORPHOLOGY);
		ArrayList<int[]> list2 = elkb.index.getEntryListNumerical(word2, MORPHOLOGY);
		if(list1.size() == 0 || list2.size() == 0){
			//return -1;
			return 0;
		}
		int best = 0;
		for (int i = 0; i < list1.size(); i++) {
			int[] entry1 = list1.get(i);
			if(elkb.index.convertToPOS(entry1[5]).equals(pos1)){
				for (int j = 0; j < list2.size(); j++) {
					int[] entry2 = list2.get(j);
					if(elkb.index.convertToPOS(""+entry2[5]).equals(pos2)){
						int diff = 16;
						for (int k = 0; k < 8; k++) {
							if (entry1[k] != entry2[k]){
								if(2*k < diff){
									diff = 2*k;
								}
							}
						}
						if(best < diff){
							best = diff;
						}
					}
				}
			}
		}
		return best;
	}

}
