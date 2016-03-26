package com.efficientmail;

public class TextClean {
	public static String getMailBody(String s) {
		s = s.replaceAll("=0A","\n");
		s = s.replaceAll("=\n"," ");
		s = s.replaceAll("\r\n","\n");

		String[] arr = s.split("\n\n");
		String mystr = "";

		for (int i = 1; i < arr.length; i++)
			mystr += arr[i] + " ";

		mystr = mystr.replaceAll("=\r\n"," ").replaceAll("Sent\n?\\s+from\n?\\s+my\n?\\s+iPad|iPhone|BlackBerry"," ").replaceAll("(?s)On[\\s\\S\n]+wrote:[\\S\\s\n]+"," ");

		mystr = mystr.replaceAll("(?i)(?s)[-]+\\s*Original Message\\s*.*"," ");
		mystr = mystr.replaceAll("(?i)(?s)[-]+\\s*Forwarded (Message|by)\\s*.*"," ");
		mystr = mystr.replaceAll("(?s)On.*?,.*?wrote.*"," ");

		return mystr;
	}
} 
