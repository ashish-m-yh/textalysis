package com.efficientmail;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Closeable;

import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;

public final class SentiClassifyLoader implements Closeable {
	private TokenizerME 		  tokenizer;
	private POSTaggerME 		  pos_tagger;
	private SentenceDetectorME 	  sent_tok;

	private Map<String,String>    pos_map;
	private Map<String,Double>    mood_map;  
	private Map<String,Double>    cmp;
	private ArrayList<String[]>   phrases;
	private Map<String,Boolean>   stop_words; 
	private Map<String,String>    pats;

	private InputStream	wordtok_is;
	private InputStream pos_is;
	private InputStream sent_is;

	public SentiClassifyLoader() {
		String tok_model_file  = "/models/en-token.bin";
		String pos_model_file  = "/models/en-pos-maxent.bin";
		String sent_model_file = "/models/en-sent.bin"; 

		this.pos_map 		= new HashMap<String,String>();
		this.mood_map		= new HashMap<String,Double>();
		this.cmp	   		= new HashMap<String,Double>();
		this.phrases 		= new ArrayList<String[]>();
		this.stop_words 	= new HashMap<String,Boolean>();
		this.pats			= new HashMap<String,String>();

		try {
			wordtok_is 		= this.getClass().getResourceAsStream(tok_model_file);
			this.tokenizer 	= new TokenizerME(new TokenizerModel(this.wordtok_is));

			pos_is     		= this.getClass().getResourceAsStream(pos_model_file);
			this.pos_tagger	= new POSTaggerME(new POSModel(this.pos_is));

			sent_is    		= this.getClass().getResourceAsStream(sent_model_file);
			this.sent_tok	= new SentenceDetectorME(new SentenceModel(this.sent_is));
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		this.pos_map = new HashMap<String,String>();
		this.pos_map.put("JJ", "a");
		this.pos_map.put("JJR","a");
		this.pos_map.put("JJS","a");

		this.pos_map.put("NN", "n");
		this.pos_map.put("NNS","n");

		this.pos_map.put("RB", "r");
		this.pos_map.put("RBR","r");
		this.pos_map.put("RBS","r");

		this.pos_map.put("VB", "v");
		this.pos_map.put("VBP","v");
		this.pos_map.put("VBD","v");
		this.pos_map.put("VBG","v");
		this.pos_map.put("VBN","v");
		this.pos_map.put("VBV","v");
		this.pos_map.put("VBZ","v");

		this.loadMoods();
		this.loadDegrees();
		this.loadPhrases();
		this.loadStopWords();
		this.loadPatterns();
	}

	public TokenizerME getWordTok() {
		return this.tokenizer;
	}
 
	public POSTaggerME getPOSTagger() {
		return this.pos_tagger;
	}

	public SentenceDetectorME getSentTok() {
		return this.sent_tok;
	}

	public Map<String,String> getPOSMap() {
		return this.pos_map;
	} 

	public Map<String,Double> getMoods() {
		return this.mood_map;
	}

	public Map<String,Double> getDegrees() {
		return this.cmp;
	}

	public ArrayList<String[]> getPhrases() {
		return this.phrases;
	}

	public Map<String,Boolean> getStopWords() {
		return this.stop_words;
	}

	public Map<String,String> getPatterns() {
		return this.pats;
	}

	private void loadMoods() {
		String pos_file = "/lexicon/positive_sentiments.txt";
		String neg_file = "/lexicon/negative_sentiments.txt";

		String[] files = { pos_file, neg_file };

		int i = 0;

		InputStream is    = null;
		BufferedReader br = null;
	
		for (String f: files) { 
			try {
				is = this.getClass().getResourceAsStream(f);
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));	

				String line;

				while ((line = br.readLine()) != null) {
					String[] parts = line.split("\t");

					if (parts.length < 3) {
						if (i == 0) {
							this.mood_map.put(parts[0] + "#n",0.15);
							this.mood_map.put(parts[0] + "#a",0.15);
							this.mood_map.put(parts[0] + "#r",0.15);
							this.mood_map.put(parts[0] + "#v",0.15);
						}
						else {
							this.mood_map.put(parts[0] + "#n",-0.15);
							this.mood_map.put(parts[0] + "#n",-0.15);
							this.mood_map.put(parts[0] + "#n",-0.15);
							this.mood_map.put(parts[0] + "#n",-0.15);
						}
					}
					else {
						String key = parts[0] + "#" + parts[1];
						double t   = Double.parseDouble(parts[2]);

						if (i == 1 && t > 0)
							t = t * -1;

						this.mood_map.put(key, t);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (br != null) 
						br.close();

					if (is != null) 
						is.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		
			++i;
		}
	}

	private void loadDegrees() {
		String degree_file= "/lexicon/degrees.txt";
		
		InputStream is 	  = null;
		BufferedReader br = null;

		try {
			is = this.getClass().getResourceAsStream(degree_file);
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));	

			String line;

			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				this.cmp.put(parts[0], Double.parseDouble(parts[1]));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) 
					br.close();

				if (is != null) 
					is.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadPhrases() {
		String phrase_file    = "/lexicon/phrases.txt";

		InputStream is 	  = null;
		BufferedReader br = null;

		try {
			is = this.getClass().getResourceAsStream(phrase_file);
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));	

			String line;

			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				String l 	   = "" + parts[0].split(" ").length;

				String v[] = { parts[0],parts[1],l };
				this.phrases.add(v);
			}

			br.close();
			is.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) 
					br.close();

				if (is != null) 
					is.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadStopWords() {
		String stop_file    = "/lexicon/stopwords.txt";

		InputStream is		= null;
		BufferedReader br	= null;

		try {
			is = this.getClass().getResourceAsStream(stop_file);
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));	

			String line;

			while ((line = br.readLine()) != null)
				this.stop_words.put(line,true);

			br.close();
			is.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) 
					br.close();

				if (is != null) 
					is.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}  

	private void loadPatterns() {
		String pat_file 	  = "/lexicon/keywords";

		InputStream is		= null;
		BufferedReader br	= null;

		try {
			is = this.getClass().getResourceAsStream(pat_file);
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));	

			String line;

			while ((line = br.readLine()) != null) {
				String[] chunks = line.split("\t");
				this.pats.put(chunks[0],chunks[1]);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) 
					br.close();

				if (is != null) 
					is.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (this.wordtok_is != null)
			this.wordtok_is.close();

		if (this.pos_is != null)
			this.pos_is.close();

		if (this.sent_is != null) 
			this.sent_is.close();
	}
}
