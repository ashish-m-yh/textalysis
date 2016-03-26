package com.efficientmail;

import com.efficientmail.SentiClassifyLoader;

import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.security.MessageDigest;
import java.math.BigInteger;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThemeDetect {
	private Map<String,Double>  mood_map;  
	private Map<String,String>  pats;

    final private static Logger logger = LoggerFactory.getLogger(ThemeDetect.class);

	public ThemeDetect(SentiClassifyLoader s) {
		this.mood_map  = s.getMoods();
		this.pats	   = s.getPatterns();
	}

	public Map<String,ArrayList<String>> detectThemes(String inputText) {
		float pos_scr   = 0;
		float neg_scr   = 0;

		ArrayList<String> al_p = new ArrayList<String>();
		ArrayList<String> al_n = new ArrayList<String>();
		ArrayList<String> kw   = new ArrayList<String>();

		final String MAIL_REGEX  = "([_A-Za-z0-9-]+)(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})";

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
		inputText = inputText.replaceAll("\\s+"," ");

		Map<String,Boolean> seen = new HashMap<String,Boolean>(); 

		try {
			Pattern is_pat	= Pattern.compile("\\p{Punct}");
			String[] pos    = { "a", "n", "r", "v" };

			String newstr   = new String(inputText).toLowerCase();

			for (String mypat : this.pats.keySet()) {
				Matcher a_pat	= is_pat.matcher(mypat);
				String category = this.pats.get(mypat); 

				Pattern this_pat   = Pattern.compile(mypat);
				Matcher this_match = this_pat.matcher(newstr);	

				if (!a_pat.find() && this_match.find()) {
					if (! kw.contains(mypat))
						kw.add(mypat);

					if (! al_n.contains(category))
						al_n.add(category);

					this.log(mypat + "\t" + "neg" + "\t" + inputText + "\t" + category);
					continue;
				}
					
				boolean neg   = false;

				ArrayList<String> s = this.getSegment(inputText,mypat);

				for (int x = 0; x < s.size(); x++) {
					String w = s.get(x).toLowerCase();

					double swn_feeling = 0;

					if (this.is_negation(w)) {
						neg   = true;
						continue;
					} 
							 
					for (String p: pos) {
						try {
							swn_feeling = this.getWordMood(w,p);
						}
						catch (Exception e) {
							swn_feeling = 0;
						}

						if (swn_feeling != 0) {
							if (neg)
								swn_feeling *= -1;

							break;
						}
					}

					String mykey   = null;
					String keyword = null; 
					String mysent  = null; 

					if (swn_feeling > 0) {
						mykey   = mypat;
	
						keyword = mykey.replace("\\W","");
						keyword = keyword.replace("\\p","").replace("Punct","").replace("{}","");
						keyword = keyword.replaceAll("\\p{Punct}","").replaceAll("b$","");

						mysent	= "pos";
					}
					else if (swn_feeling < 0) {
						mykey 	= mypat;

						keyword = mykey.replace("\\W","");
						keyword = keyword.replace("\\p","").replace("Punct","").replace("{}","");
						keyword = keyword.replaceAll("\\p{Punct}","").replaceAll("b$","");

						mysent	= "neg";
					}

					if (mykey == null)
						continue;

					String digest = "";

					try {
						MessageDigest msg = MessageDigest.getInstance("MD5");
						msg.update(inputText.getBytes(),0,inputText.length());
			        	digest = new BigInteger(1,msg.digest()).toString(16);
					}
					catch (Exception e) {
						e.printStackTrace();
					}

					if (!"".equals(digest))
						digest = digest + "\t" + keyword;

					if (!seen.containsKey(digest)) {
						if (! mypat.matches("^[a-zA-Z]+.*?")) { 
							boolean suc = false;

							for (int y = x + 1; y < s.size(); y++) {
								if (this.is_negation(s.get(y).toLowerCase())) {
									if ("pos".equals(mysent)) 
										mysent = "neg";
									else if ("neg".equals(mysent))
										mysent = "pos";

									break;
								}

								for (String p: pos) {
									double my_feeling = 0;

									try {
										my_feeling = this.getWordMood(s.get(y).toLowerCase(),p);
									}
									catch (Exception e) {
										my_feeling = 0;
									}

									if (my_feeling != 0)
										suc = true;
								}

								if (suc) 
									break;
							}
						}

						String myentry = keyword + "\t" + mysent + "\t" + inputText + "\t" + category;

						if (!neg) {
							this.log(myentry + "\t" + w);
						}
						else {
							this.log(myentry + "\t" + "suspected negation context");
						}

						if ("pos".equals(mysent)) {
							seen.put(digest,true);

							if (! kw.contains(keyword))
								kw.add(keyword);

							if (! al_p.contains(category))
								al_p.add(category);
						}
						else if ("neg".equals(mysent)) {
							seen.put(digest,true);

							if (! kw.contains(keyword))
								kw.add(keyword);

							if (! al_n.contains(category))
								al_n.add(category);
						}

						break;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		Map<String,ArrayList<String>> mymap = new HashMap<String,ArrayList<String>>();
		mymap.put("pos",al_p);
		mymap.put("neg",al_n);
		mymap.put("kw",kw);

		return mymap;
	}

	private ArrayList<String> getSegment(String msg,String pat_str) {
		if (! msg.matches("[\\.]$")) 
			msg = msg + ".";	

		String direction = "";

		if (pat_str.matches("^[a-zA-Z]+.*?"))
            direction = "forward";
        else
            direction = "backward";

		Pattern	pat     = Pattern.compile(pat_str,Pattern.CASE_INSENSITIVE);
		Matcher matches = pat.matcher(msg);

		ArrayList<String> segment = new ArrayList<String>();
	
		int max_toks   = 5;

		if ("backward".equals(direction)) {
			if (matches.find()) {
				String mystr    = matches.group(0);
				mystr			= mystr.replaceAll("(\\p{Punct})", " $1 ");
				String chunks[] = mystr.split(" ");

				int limit    = chunks.length - 1;
				int i		 = 0;

				while ( (i++ < (max_toks+1)) && ((limit - i) >= 0)) {
					String lc = chunks[limit - i].toLowerCase();

					if (chunks[limit - i].matches(".*?\\p{Punct}.*?") || "and".equals(lc) || "but".equals(lc)) 
						break;

					if (! chunks[limit - i].matches(".*?\\p{Punct}.*?"))
						segment.add(chunks[limit-i]);
				}
			}
		}
		else if ("forward".equals(direction)) {
			if (matches.find()) {
				String mystr    = matches.group(0);
				mystr			= mystr.replaceAll("(\\p{Punct})", " $1 ");
				String chunks[] = mystr.split(" ");
				
				int limit = chunks.length - 1;
				int i     = 0;

				while ( (i++ < max_toks) && (i <= limit) ) {
					String lc = chunks[i].toLowerCase();

					if (chunks[i].matches(".*?\\p{Punct}.*?") || "and".equals(lc) || "but".equals(lc))
						break;

					if (! chunks[i].matches(".*?\\p{Punct}.*?")) 
						segment.add(chunks[i]);
				}
			}
		}

		return segment;
	}

	private double getWordMood(String word, String pos) {
		return this.mood_map.get(word + "#" + pos);
	}

	private void log(String msg) {
		this.logger.info(msg);
	}

	public static boolean is_negation(String s) {
		String[] neg = { "nobody", "no", "not", "nor", "neither", "nothing", "never" };
		
		boolean is_neg = false;

		for (int x = 0; x < neg.length; x++) { 
			if (neg[x].equals(s)) { 
				is_neg = true;	
				break;
			}
		}

		return is_neg;
	}
}
