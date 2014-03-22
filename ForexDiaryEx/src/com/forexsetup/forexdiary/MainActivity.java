package com.forexsetup.forexdiary;

import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnItemSelectedListener, OnClickListener{
	
	ListView lvForm;
	EditText etLot;
	EditText etEntry;
	EditText etSL;
	EditText etTP;
	EditText etOprice;
	
	Button btnAdd;
	ToggleButton tbtnBuy;
	SimpleCursorAdapter scAdapter;
	Cursor cursor;
	DB db;
	String spName;//Name of pair list in spinner
	
	MyResourceCursorAdapter mrcAdapter;
	final static boolean  MyAdapter = true;
	
	final int MENU_ADD = 1;
	final int MENU_SORT = 2;
	final int MENU_ABOUT = 3;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		lvForm = (ListView) findViewById(R.id.lvForm);
		etLot = (EditText) findViewById(R.id.etLot);
		etEntry = (EditText) findViewById(R.id.etEntry);
		etSL = (EditText) findViewById(R.id.etSL);
		etTP = (EditText) findViewById(R.id.etTP);
		etOprice = (EditText) findViewById(R.id.etOPrice);
		
		btnAdd = (Button) findViewById(R.id.btnAdd);
		btnAdd.setOnClickListener(this);
		tbtnBuy = (ToggleButton) findViewById(R.id.tbtnBuy);
				
		//create spinner adapter for PAIR
		ArrayAdapter<CharSequence> sAdapter = ArrayAdapter.createFromResource(
				this, R.array.pair, android.R.layout.simple_spinner_item);
		sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) findViewById(R.id.spPair);
		spinner.setAdapter(sAdapter);
		spinner.setOnItemSelectedListener(this);
		
		db = new DB(this);
		db.open();
		
		cursor = db.getAllData();
		startManagingCursor(cursor);
		
		String[] from = new String[] {/*DB.ID,*/ DB.PAIR, DB.LOT, DB.DATE};
		int[] to = new int[] {/*R.id.tv0,*/ R.id.tv1, R.id.tv2, R.id.tv3};
		
		if (MyAdapter) {
			try {
				mrcAdapter = new MyResourceCursorAdapter(this, R.layout.item, cursor/*, true*/);
			}catch (Exception e) {
				Log.d("myLog", e.getMessage());
			}
			lvForm.setAdapter(mrcAdapter);
		} else {
			try {
				scAdapter = new SimpleCursorAdapter(this, R.layout.item,
						cursor, from, to);
			} catch (Exception e) {
				Log.d("myLog", e.getMessage() /* e.getStackTrace().toString() */);
				// e.printStackTrace();
			}
			lvForm.setAdapter(scAdapter);
		}
		registerForContextMenu(lvForm);// need onCreateContextMenu(...);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0 /*groupID*/, MENU_ADD /*ID*/, 1 /*Order*/, "add" /*text*/);
		menu.add(0, MENU_SORT, 2, "sort");
		menu.add(0, MENU_ABOUT, 3, "about");
		super.onCreateOptionsMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menu) {
		switch (menu.getItemId()) {
		case MENU_ADD:
			addRecord();
			break;
		case MENU_SORT:
			break;
		case MENU_ABOUT:
			break;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	void addRecord() {
		int lot, date, entry , sl, tp, oprice, pos = 1;
		String S_lot, S_entry, S_sl, S_tp, S_oprice;
		float fTmp;
		boolean flag, valid = true;
		
		
		// lot 99.00 - 0.01
		S_lot = etLot.getText().toString();
		flag = validateDot2(S_lot);
		if (!flag) {
			// Toast.makeText(this, "Wrong arguments in lot", Toast.LENGTH_SHORT).show();
			etLot.setError("Range 100-0.01");
			valid = false;
		}

		// entry 9 - 0.0001
		S_entry = etEntry.getText().toString();
		flag = validateDot4(S_entry);
		if (!flag) {
			etEntry.setError("Range 9-0.0001");
			valid = false;
		}
		
		// Stop loss 9 - 0.0001
		S_sl = etSL.getText().toString();
		flag = validateDot4(S_sl);
		if (!flag) {
			etSL.setError("Range 9-0.0001");
			valid = false;
		}
		
		// Take profit 9 - 0.0001
		S_tp = etTP.getText().toString();
		flag = validateDot4(S_tp);
		if (!flag) {
			etTP.setError("Range 9-0.0001");
			valid = false;
		}
		
		// Out price 9 - 0.0001
		S_oprice = etOprice.getText().toString();
		flag = validateDot4(S_oprice);
		if (!flag) {
			etOprice.setError("Range 9-0.0001");
			valid = false;
		}
		
		
		// if one or more strings are not validate
		if (!valid) {
			return;
		}
		
		// lot
		fTmp = Float.valueOf(S_lot);
		lot = (int) (100 * fTmp);// *100 for save 2 symbols after point
		
		// buy or sell to lot value 
		if (tbtnBuy.isChecked()) {
			//buy
		} else {
			//sell
			lot = -lot;
		}
		
		//date in seconds UTC time
		date = (int) (System.currentTimeMillis() / 1000L);
		
		// entry
		fTmp = Float.valueOf(S_entry);
		entry = (int) (10000 * fTmp);// *10000 for save 4 symbols after point
		
		// Stop loss
		fTmp = Float.valueOf(S_sl);
		sl = (int) (10000 * fTmp);// *10000 for save 4 symbols after point
		
		// Take profit
		fTmp = Float.valueOf(S_tp);
		tp = (int) (10000 * fTmp);// *10000 for save 4 symbols after point
		
		// Out price
		fTmp = Float.valueOf(S_oprice);
		oprice = (int) (10000 * fTmp);// *10000 for save 4 symbols after point
		
		//database access
		db.addRec(spName, lot, date, entry, sl, tp, oprice, pos);
		cursor.requery();
	}
	
	boolean validateDot2(String text) {
		//  local dot input not support by EditView
		/*
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	    String ds = String.valueOf(dfs.getDecimalSeparator());
		String pattern = "[0-9]{1,2}([" + ds + "]{1}[0-9]{1,2})?";
		*/
		String pattern = "[0-9]{1,2}([.]{1}[0-9]{1,2})?";
		return Pattern.matches(pattern, text);
	}
	
	boolean validateDot4(String text) {
		//  local dot input not support by EditView
		/*
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	    String ds = String.valueOf(dfs.getDecimalSeparator());
		String pattern = "^[0-9]{1}([" + ds + "]{1}[0-9]{1,4})?";
		*/
		String pattern = "^[0-9]{1}([.]{1}[0-9]{1,4})?";
		return Pattern.matches(pattern, text);
	}
	
	protected void onDestroy() {
		super.onDestroy();
		db.close();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		
		spName = parent.getItemAtPosition(position).toString();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnAdd:
			addRecord();
			break;
		}
	}
	
	public class MyResourceCursorAdapter extends ResourceCursorAdapter {

		public MyResourceCursorAdapter(Context context, int layout, Cursor c,
				boolean autoRequery) {
			super(context, layout, c, autoRequery);
			// TODO Auto-generated constructor stub
		}

		//@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		public MyResourceCursorAdapter(Context context, int layout, Cursor c)//,
				//int flags) 
				{
			super(context, layout, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			CharSequence str;
			DateFormat df;
			String operation;
			int lot, entry, sl, tp, oprise;
			
			TextView tv0 = (TextView) view.findViewById(R.id.tv0);
			TextView tv1 = (TextView) view.findViewById(R.id.tv1);
			TextView tv2 = (TextView) view.findViewById(R.id.tv2);
			TextView tv3 = (TextView) view.findViewById(R.id.tv3);
			
			TextView tv4 = (TextView) view.findViewById(R.id.tv4);
			TextView tv5 = (TextView) view.findViewById(R.id.tv5);
			TextView tv6 = (TextView) view.findViewById(R.id.tv6);
			TextView tv7 = (TextView) view.findViewById(R.id.tv7);
			
			lot = cursor.getInt(cursor.getColumnIndex(DB.LOT));
			if (Integer.signum(lot) < 0) {
				lot = -lot;
				operation = "sell";
			} else {
				operation = "buy";
			}
			
			tv0.setText(cursor.getString(cursor.getColumnIndex(DB.PAIR)));
			tv1.setText( String.format("%1.2f", /*Float.toString(*/ lot * 0.01f/*)*/));// format output
			// divide by 100 to normalize
			tv2.setText(operation);
			
			df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			str = df.format(new Date(1000L * cursor.getInt(cursor.getColumnIndex(DB.DATE))));
			tv3.setText(str);
			
			entry = cursor.getInt(cursor.getColumnIndex(DB.ENTRY));
			tv4.setText(String.format("%1.4f",  entry * 0.0001f));
			sl = cursor.getInt(cursor.getColumnIndex(DB.STOP_LOSS));
			tv5.setText(String.format("%1.4f",  sl * 0.0001f));
			tp = cursor.getInt(cursor.getColumnIndex(DB.TAKE_PROFIT));
			tv6.setText(String.format("%1.4f",  tp * 0.0001f));
			oprise = cursor.getInt(cursor.getColumnIndex(DB.OUT_PRICE));
			tv7.setText(String.format("%1.4f",  oprise * 0.0001f));
		}

	}
}