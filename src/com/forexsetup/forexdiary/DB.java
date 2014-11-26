package com.forexsetup.forexdiary;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DB {

	private static final String DB_NAME = "mydb";
	private static final int DB_VERSION = 1;
	private static final String DB_TABLE = "mytab";
	
	public static final String ID = "_id";
	public static final String PAIR = "pair";
	public static final String LOT = "lot";
	public static final String DATE = "date";
	public static final String ENTRY = "entry";
	public static final String STOP_LOSS = "StopLoss";
	public static final String TAKE_PROFIT = "TakeProfit";
	public static final String OUT_PRICE = "OutPrice";
	public static final String POSITION = "position";
	
	public static final String DB_CREATE = 
			"create table " + DB_TABLE + "(" +
					ID + " integer primary key autoincrement, " +
					PAIR + " text, " +
					LOT + " integer, " +
					DATE + " integer, " +
					ENTRY + " integer, " +
					STOP_LOSS + " integer, " +
					TAKE_PROFIT + " integer, " +
					OUT_PRICE + " integer, " +
					POSITION + " integer" +
			");";
	
	private final Context mCtx;
	private DBHelper mDBHelper;
	private SQLiteDatabase mDB;
	private static DB instance = null;
	
	//Control variables
	public boolean orderForward = false;// set forward order db view
	public String pair = "";
	public long startDate = 0;
	public long endDate = 0;
	
	
	
	// Constructor
	public DB(Context ctx) {
		mCtx = ctx;
		instance = this;
	}
	
	public void open(){
		mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
		mDB = mDBHelper.getWritableDatabase();
	}
	
	public void close() {
		if (mDBHelper != null) mDBHelper.close();
	}
	
	public Cursor getAllData() {
		String order = "";
		String selection = ""; // formula for a quarry
		String selectionArgs[] = null;
		ArrayList<String> alist = new ArrayList<String>(); // arguments for the selection formula
		
		if (!orderForward) {
			order = ID + " DESC";
		}
		if (pair.length() != 0) {
			selection += PAIR + " = ? ";
			alist.add(pair);
		}
		if (startDate != 0 && endDate != 0) {
			if (selection.length() != 0) { selection += " and "; }
			selection += DATE + " between ? and ?";
			alist.add(Long.toString(startDate));
			alist.add(Long.toString(endDate));
		}
		if (!alist.isEmpty()) {
			selectionArgs =  (String[]) alist.toArray(new String[alist.size()]);
		}
		return mDB.query(DB_TABLE, null, selection, selectionArgs, null, null, order);
	}
	
	public Cursor getExportData() {
		
		String order;
		order = "";	//	order = ID + " DESC";
		
		return mDB.query(DB_TABLE, null, null, null, null, null, order);
	}
	
	public void addRec(String pair, int lot ,int date, int entry, 
			int sl, int tp, int oprice, int pos) {
		ContentValues cv = new ContentValues();
		cv.put(PAIR, pair);
		cv.put(LOT, lot);
		cv.put(DATE, date);
		cv.put(ENTRY, entry);
		cv.put(STOP_LOSS, sl);
		cv.put(TAKE_PROFIT, tp);
		cv.put(OUT_PRICE, oprice);
		cv.put(POSITION, pos);
		mDB.insert(DB_TABLE, null, cv);
	}
	
	void delete(long id) {
		mDB.delete(DB_TABLE, ID + " = " + id, null);
	}
	
	void deleteAll() {
		mDB.delete(DB_TABLE, null, null);
	}
	
	void update(String pair, int lot ,int date, int entry, 
			int sl, int tp, int oprice, int pos, long id) {
		ContentValues cv = new ContentValues();
		cv.put(PAIR, pair);
		cv.put(LOT, lot);
		cv.put(DATE, date);
		cv.put(ENTRY, entry);
		cv.put(STOP_LOSS, sl);
		cv.put(TAKE_PROFIT, tp);
		cv.put(OUT_PRICE, oprice);
		cv.put(POSITION, pos);
		mDB.update(DB_TABLE, cv, ID + " = " + id, null);
	}
	
	static DB getInstance() {
		return instance;
	}
	
	// Class 
	private class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
