package com.efficientmail;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

import java.io.IOException;

import java.math.BigInteger;

public class Meter {
	private String meter_file = "";
	
	public Meter(String meter_dir,String lkey) {
		this.meter_file = meter_dir + File.separator + lkey + ".key";
	} 

	public boolean init(int maxcalls) {
		long t1 = System.nanoTime();

		try {
			File file = new File(this.meter_file);
			file.createNewFile();

			FileWriter writer = new FileWriter(file);
			writer.write(1 + "\t" + t1 + "\t" + maxcalls + "\n");
			writer.flush();
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean checkMeter() {
		boolean rv = false;

		try {
			File file = new File(this.meter_file);

			if (file.isFile()) {
				FileReader reader = new FileReader(file);
				char[] buf 		  = new char[255];
				reader.read(buf);
				reader.close();
				
				String arr[] = new String(buf).trim().split("\t");

				System.out.println(arr[0]);
				System.out.println(arr[1]);
				System.out.println(arr[2]);

				int calls    	= Integer.parseInt(arr[0]);
				long nano_diff  = System.nanoTime() - Long.parseLong(arr[1]);
				int maxcalls    = Integer.parseInt(arr[2]);

				if (calls < maxcalls)
					rv = true;
				else
					rv = false;
			}
		}
		catch (IOException e) {
			rv = false;
		}

		return rv;
	}

	public boolean updateMeter() {
		try {	
			File file = new File(this.meter_file);

			if (file.isFile()) {
				FileReader reader = new FileReader(file);
				char[] buf 		  = new char[255];
				reader.read(buf);
				reader.close();
				
				String arr[] 	= new String(buf).trim().split("\t");
				int calls    	= Integer.parseInt(arr[0]) + 1;
				long t1  		= System.nanoTime();

				FileWriter writer = new FileWriter(file);
				writer.write(calls + "\t" + t1 + "\t" + arr[2] + "\n");
				writer.flush();
				writer.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}
} 
