package com.forexsetup.forexdiary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DBImport {

//private InputStreamReader isr;
private BufferedReader br;
private	String es = ",";
private	DB db;
	
private	final static int COLUMN_NUMBERS = 8;
	
	public DBImport(InputStreamReader isr, DB db, char es) {
		//this.isr = isr;
		br = new BufferedReader(isr);
		this.db = db;
		this.es = String.valueOf(es);
	}
	
protected String [] readLine() {
		String InputLine;
		String [] elements = null;
		try {
			InputLine = br.readLine();
			if (InputLine == null) {
				return null;
			}
			elements = InputLine.split(es);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return elements;
	}
	
	
	public int writeTable() {
		
		String [] line;
		int lNumber = 0;
		
		line = readLine();
		if (line == null | line.length != COLUMN_NUMBERS) {
			return 0;
		}
		while ((line = readLine()) != null) {
			if (line.length != COLUMN_NUMBERS) {
				return -lNumber;
			}
			db.addRec(line [0]					//pair
					, Integer.valueOf(line [1])//lot
					, Integer.valueOf(line [2])//date
					, Integer.valueOf(line [3])//entry
					, Integer.valueOf(line [4])//sl
					, Integer.valueOf(line [5])//tp
					, Integer.valueOf(line [6])//oprice
					, line [7]//pos
							);
			lNumber++;
		}
		return lNumber;
	}
}
