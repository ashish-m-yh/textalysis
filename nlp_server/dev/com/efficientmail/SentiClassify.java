package com.efficientmail;

import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficientmail.SentiClassifyLoader;

public class SentiClassify {
	private POSTaggerME 		pos_tagger;
	private TokenizerME 		tokenizer;
	private SentenceDetectorME 	sent_tok;

	private Map<String,String>  pos_map;
	private Map<String,Double>  mood_map;  
	private Map<String,Double>  cmp;
	private ArrayList<String[]> phrases;
	private Map<String,Boolean> stop_words; 

	final private static Logger logger  = LoggerFactory.getLogger(SentiClassify.class);

	public SentiClassify(SentiClassifyLoader s) {
		this.pos_tagger = s.getPOSTagger();
		this.tokenizer  = s.getWordTok();
		this.sent_tok	= s.getSentTok();		

		this.pos_map	= s.getPOSMap();
		this.mood_map	= s.getMoods();
		this.cmp		= s.getDegrees();
		this.phrases	= s.getPhrases();
		this.stop_words = s.getStopWords();
	}

	private void log(String msg) {
		this.logger.info(msg);
	}

	public float[] detectSentiment(String inputText) {
		float pos_scr   	= 0;
		float neg_scr   	= 0;
		int num_senti_sents = 0;

		final double boost  = 1.20;

        	final String MAIL_REGEX  = "([_A-Za-z0-9-]+)(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})";

		this.log(inputText);

		inputText = inputText.replaceAll("(\\.|\\?|\\!)","$1 "); 
		inputText = inputText.replaceAll("(?i)didnt","did not");
		inputText = inputText.replaceAll("(?i)wouldnt","would not");
		inputText = inputText.replaceAll("(?i)cant","can not");
		inputText = inputText.replaceAll("(?i)shouldnt","should not");
		inputText = inputText.replaceAll("([A-Za-z])n\'t","$1 not");
		inputText = inputText.replaceAll("([A-Za-z])\'s","$1 is");
		inputText = inputText.replaceAll("(I)\'d|(We)\'d|(You)\'d","$1 would");
		inputText = inputText.replaceAll("(?i)[A-Z]*\\s*regards[,\n]|(?i)[A-Z]*\\s*truly[,\n]|(?i)[A-Z]*\\s*faithfully[,\n]"," ");
		inputText = inputText.replaceAll("(?i)cheers[,\n]"," ");
		inputText = inputText.replaceAll("(?i)Well,"," ");
		inputText = inputText.replaceAll("(?i)(would|should|shall|can) (be)?\\s*[a-z]+\\s"," ");
		inputText = inputText.replaceAll(MAIL_REGEX,"");

		Map<String, ArrayList<String[]>> phrase_stem = new HashMap<String,ArrayList<String[]>>();

		for (int n = 0; n < this.phrases.size(); n++) {
			String plist[] = this.phrases.get(n);

			Pattern p  	= Pattern.compile(plist[0],Pattern.CASE_INSENSITIVE);
			Matcher mx 	= p.matcher(inputText);

			String k  	= plist[0].split(" ")[0]; 

			ArrayList<String[]> matching_phrases;

			if (phrase_stem.containsKey(k)) {
				matching_phrases = phrase_stem.get(k);
			}
			else {
				matching_phrases = new ArrayList<String[]>();
			}

			if (mx.find()) { 
				String[] arr = { mx.group(), plist[1], plist[2] }; 
				matching_phrases.add(arr);

				phrase_stem.put(k,matching_phrases);
			}
		}

		Pattern question_pat 	= Pattern.compile(".*?\\?");

		float[] emote_sentiment = this.getEmoteSentiment(inputText);

		String sentences[] 		= this.sent_tok.sentDetect(inputText);		

		String delimiters[]		= { "and", "or", "but", "so", "as", "which", "who", "why", "where", "when", "whenever", "whether", "because",
									"unless", "until", "since", "hence" };

		for (String s: sentences) {
			Matcher question_m = question_pat.matcher(s);

			if ( s.equals(s.toUpperCase()) )
				s = s.toLowerCase();

			if (question_m.find()) 
				continue;

			String[] toks  = this.tokenizer.tokenize(s);
			String[] tokcpy= new String[toks.length];
			System.arraycopy(toks,0,tokcpy,0,tokcpy.length);

			for (int tok_idx = 0; tok_idx < tokcpy.length; tok_idx++) {
				if ( tokcpy[tok_idx].equals(tokcpy[tok_idx].toUpperCase()) ) {
					tokcpy[tok_idx] = tokcpy[tok_idx].toLowerCase();
				}
			}

			String tags[] = this.pos_tagger.tag(tokcpy);
	
			int last_neg_pos	  = 0;	
			int	numgrams		  = 0;
			double mul_factor 	  = 1;
			boolean has_sentiment = false;

			for (int i = 0; i < tags.length; i++) {
				if (numgrams > 0) {
					--numgrams;	
					continue;
				}

				String lc  = toks[i].toLowerCase();

				if (Pattern.matches("\\p{Punct}", lc) || (i-last_neg_pos) > 6) {
					mul_factor = 1;
				}
				else {
					for (String d: delimiters) { 
						if (d.equals(lc)) {
							mul_factor = 1;
							break;
						}
					}
				}

				if ("asap".equals(lc)) {
					neg_scr += 1.5;	
					continue;
				}

				// if (!toks[i].equals(toks[i].toUpperCase()) && "NNP".equals(tags[i]))
				// 	continue;

				if (phrase_stem.containsKey(lc)) {
					ArrayList<String[]> phr_val = phrase_stem.get(lc);

					boolean skip_loop = false;

					for (int ph_iter = 0; ph_iter < phr_val.size(); ph_iter++) {
						String[] tmp = phr_val.get(ph_iter);

						String ph  = tmp[0];
						int ngrams = Integer.parseInt(tmp[2]);
						double wt  = Double.parseDouble(tmp[1]);

						String cmpstr = new String();

						for (int l = 0; l < ngrams; l++) {
							if ((l + i) < toks.length) 
								cmpstr += toks[l+i] + " ";
						}

						cmpstr = cmpstr.trim();

						if (cmpstr.equals(ph)) {
							wt *= mul_factor;

							this.log("Phrase " + ph + " found " + wt + "\n");

							if (wt > 0) {
								pos_scr 	+= (float) Math.round(new Double(wt).floatValue()*100)/100;
								mul_factor = 1;
							}
							else if (wt < 0) {
								neg_scr 	+= (float) Math.round(new Double(wt*-1).floatValue()*100)/100;
								mul_factor = 1;
							}		

							numgrams = ngrams - 1;
							skip_loop = true;

							break;
						}
					}
				
					if (skip_loop)
						continue;
				}

				String pos = this.pos_map.get(tags[i]);
				
				if (this.stop_words.containsKey(lc)) 
					continue;

				boolean allupper = this.isAllUpper(toks[i]);

				if (this.is_negation(lc)) {
					last_neg_pos = i;
					mul_factor *= -0.7;

					this.log(mul_factor + " " + boost + " " + lc + "\n");

					continue;
				}

				try {
					double cmp_boost = cmp.get(lc);
				
					if (mul_factor > 0)
						mul_factor *= cmp_boost;

					continue;
				}
				catch (Exception e) {
				}

				// if (pos == null && tags.length == 1 && !Pattern.matches("\\p{Punct}", lc)) {
				// 	double swn_feeling = 0.0;
					
				// 	try {
				// 		swn_feeling = this.getWordMood(lc, "a");
				// 	}
				// 	catch (Exception e) {
				// 		try {
				// 			swn_feeling = this.getWordMood(lc, "n");
				// 		}
				// 		catch (Exception e1) {
				// 		}
				// 	}
				// } 
				
				lc = lc.replaceAll(",","");	

				if (true) {
					String ord[] 	= { "a", "r", "v", "n" };
					String[] p 		= new String[4];
					if (pos != null)
						p[0] 	   	= pos;
					else
						p[0]		= ord[0];

					int y = 0;

					for (int j = 0; j < ord.length; j++) {
						if (!ord[j].equals(p[0]))
							p[++y] = ord[j];
					}

					for (y = 0; y < p.length; y++) {
						double swn_feeling = 0;

						try {
							swn_feeling = this.getWordMood(lc, p[y]);
						}
						catch (Exception e) {
							continue;
						}

						this.log(tags[i] + "\t" + toks[i] + "\t" + swn_feeling + "\t" + p[y] + " " + mul_factor + "\n");

						if (! Double.isNaN(swn_feeling)) { 
							double strength = (allupper) ? boost : 1; 
							double scr 		= swn_feeling * mul_factor * strength;						

							has_sentiment = true;

							if (scr > 0) {
								this.log(lc + " is positive " + scr + " " + tags[i] + "\n");
								pos_scr 	+= (float) Math.round(new Double(scr).floatValue()*100)/100;
								mul_factor  = 1;
							}
							else if (scr < 0) {
								this.log(lc + " is negative " + scr + " " + tags[i] + "\n");
								scr			*= -1;
								neg_scr 	+= (float) Math.round(new Double(scr).floatValue()*100)/100;
								mul_factor  = 1;
							}
						
							// if setniment found for any part of speech, then break out of the loop
							break;
						}
					}
				}
			}

			if (has_sentiment) {
				++num_senti_sents;
			}
		}

		float[] scrs = { pos_scr + emote_sentiment[0], neg_scr + emote_sentiment[1], num_senti_sents, sentences.length };

//		if (this.isAllUpper(inputText))
//			scrs[1] += 2;

		return scrs;
	}

	public static float[] getEmoteSentiment(String str) {
		float pos_sentiment = 0;
		float neg_sentiment = 0;

		Matcher matcher  = null;
	
		Pattern pos_pattern = Pattern.compile("(:\\-?\\))");
		matcher 			= pos_pattern.matcher(str);

		while (matcher.find())
			pos_sentiment += 0.2;

		Pattern neg_pattern = Pattern.compile("(:\\-?\\()");
		matcher     		= neg_pattern.matcher(str);

		while (matcher.find())
			neg_sentiment += 0.2;

		float[] rv = { pos_sentiment, neg_sentiment };

		return rv;
	}

	private double getWordMood(String word, String pos) {
		return this.mood_map.get(word + "#" + pos);
	}

	public static boolean is_negation(String s) {
		String[] neg = { "nobody", "not", "nor", "neither", "nothing", "never" };
		
		boolean is_neg = false;

		for (int x = 0; x < neg.length; x++) { 
			if (neg[x].equals(s)) { 
				is_neg = true;	
				break;
			}
		}

		return is_neg;
	}

	public static boolean isAllUpper(String s) {
		boolean allupper = true;

		for (int x = 0; x < s.length(); x++) {
			boolean islower = Character.isLowerCase(s.charAt(x));
			if (islower) {
				allupper = false;
				break;
			}
		}

		return allupper;
	}
}
