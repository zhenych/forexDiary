package com.forexsetup.forexdiary;

import java.io.IOException;
import java.io.OutputStreamWriter;

import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;

public class DBExport {
	
public final static char COMMA = ',';
public final static char TAB = '\t';
public final static char SEMICOLON = ';';
private static int FIRST_COLUMN = 1; //starts from this column
	
private OutputStreamWriter osw;
private DB db;
private int colCount;// column count
private Cursor cursor;// to get database
private String es; //element separator
private String ss = "\r\n"; // string separator
	
	public DBExport(OutputStreamWriter osw, DB db, char es) {
		this.osw = osw;
		this.db = db;
		this.es = String.valueOf(es); // element separator
	}
	
	public DBExport(OutputStreamWriter osw, DB db) {
		this( osw, db, COMMA);
	}
	

	protected StringBuffer getTable() {

		//int colCount;// column count
		StringBuffer sBuff; // buffer to create text
		String [] names;
		
		cursor = db.getExportData();
		colCount = cursor.getColumnCount();
		cursor.moveToFirst();
		names = cursor.getColumnNames();
		sBuff = new StringBuffer();
		
		// Header
		for (int i = FIRST_COLUMN; i < colCount; i++) {
			sBuff.append(names[i]).append(es);// add column name and element separator
		}
		sBuff.deleteCharAt(sBuff.lastIndexOf(es));
		sBuff.append(ss);
		
		// Body
		do {
			sBuff.append(getLine()).append(ss);
		}
		while(cursor.moveToNext());
		
		return sBuff;
	}
	
	protected StringBuffer getLine() {
		StringBuffer lineBuff = new StringBuffer();
		
		// add value of column element and element separator
		for (int i = FIRST_COLUMN; i < colCount; i++) {
			
			//cursor.isNull(i);
			switch (DbCompat.getType(cursor, i)) {
			case Cursor.FIELD_TYPE_STRING:
				lineBuff.append(cursor.getString(i));
				break;
				
			case Cursor.FIELD_TYPE_NULL:
				//Not operate
				break;
				
			case Cursor.FIELD_TYPE_INTEGER:
				lineBuff.append(cursor.getInt(i));
				break;
				
			case Cursor.FIELD_TYPE_FLOAT:
				lineBuff.append(cursor.getFloat(i));
				break;
				
			case Cursor.FIELD_TYPE_BLOB:
				//Not operate
				break;
			}
			
			
			lineBuff.append(es);
		}
		//lineBuff.append(ss);
		return lineBuff;
	}
	
	boolean streamOut() {

		try {
			//osw.write(this.getTable().toString());
			osw.append(getTable());
			osw.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;		
	}
	
	// Old API compatibility 
	public static class DbCompat {

	    protected static final int FIELD_TYPE_BLOB = 4;
	    protected static final int FIELD_TYPE_FLOAT = 2;
	    protected static final int FIELD_TYPE_INTEGER = 1;
	    protected static final int FIELD_TYPE_NULL = 0;
	    protected static final int FIELD_TYPE_STRING = 3;

	    @SuppressWarnings("deprecation") 
		static int getType(Cursor cursor, int i) /*throws Exception*/ {
	        SQLiteCursor sqLiteCursor = (SQLiteCursor) cursor;
	        CursorWindow cursorWindow = sqLiteCursor.getWindow();
	        int pos = cursor.getPosition();
	        int type = -1;
	        if (cursorWindow.isNull(pos, i)) {
	            type = FIELD_TYPE_NULL;
	        } else if (cursorWindow.isLong(pos, i)) {
	            type = FIELD_TYPE_INTEGER;
	        } else if (cursorWindow.isFloat(pos, i)) {
	            type = FIELD_TYPE_FLOAT;
	        } else if (cursorWindow.isString(pos, i)) {
	            type = FIELD_TYPE_STRING;
	        } else if (cursorWindow.isBlob(pos, i)) {
	            type = FIELD_TYPE_BLOB;
	        }

	        return type;
	    }
	}
	
}
