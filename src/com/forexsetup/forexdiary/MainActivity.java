package com.forexsetup.forexdiary;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

//import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends FragmentActivity implements
		OnItemSelectedListener, OnClickListener, LoaderCallbacks<Cursor> {

	ListView lvForm;
	EditText etLot;
	EditText etEntry;
	EditText etSL;
	EditText etTP;
	EditText etOprice;
	
	Button btnAdd;
	ToggleButton tbtnBuy;
	AdView adBottom;
	
	Cursor cursor_old; // test pre v11 and newer android api
	DB db;
	boolean oldCursor = false;// test pre v11 and newer android api
	//Loader<Cursor> CLoader;
	MyCursorLoader CLoader;
	
	String spName;//Name of pair list in spinner
	ArrayAdapter<CharSequence> sAdapter; //spinner adapter
	Spinner spinner;
	
	boolean edit_mode = false;  // Necessary to do edit function
	long record_id = 0;			// Necessary to do edit function
	
	MyResourceCursorAdapter mrcAdapter;
	//final static boolean  MyAdapter = true;
	final static String LOG = "myLog";
	
	private static final int C_MENU_EDIT = 10; //context menu
	private static final int C_MENU_DELETE = 11;
	private static final int C_MENU_CANCEL = 12;	
	
	static final int MENU_ADD = 1; // options menu
	static final int MENU_SORT = 2;
	static final int MENU_ADVANCED = 3;
	static final int MENU_ABOUT = 4;
	//static final int MENU_PREF = 5;
	
	static final int RECORD_ADD = 100; // options item menu 
	static final int RECORD_UPDATE = 101;
	
	static final int DIALOG_SORT = 201; // dialogs
	static final int DIALOG_ABOUT =202;
	
	private static final int REQUEST_1 = 1;
	
	SharedPreferences sPref;
	
	//@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		adBottom = (AdView) this.findViewById(R.id.ad);
		
		// Set AdListener
		adBottom.setAdListener(new AdListener() {
			
			public void onAdLoaded() {
				System.out.println("Ad loaded. " );
				}
			public void onAdFailedToLoad(int errorCode) {
				System.err.println("Ad failed: " + errorCode);
			}

			public void onAdOpened() {
				System.out.println("Ad opened. ");
			}

			public void onAdClosed() {
				System.out.println("Ad closed. ");
			}

			public void onAdLeftApplication() {
				System.out.println("Ad left application. ");
			}

		});
        
		AdRequest adReq = new AdRequest.Builder()
	    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // Эмулятор
	    //.addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4"); // Тестовый телефон Galaxy Nexus
	    .build();
		try {
			adBottom.loadAd(adReq);
		} catch (Exception ex) {
			ex.printStackTrace();
			adBottom.destroy();
		}
		
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
		sAdapter = ArrayAdapter.createFromResource(
				this, R.array.pair, android.R.layout.simple_spinner_item);
		sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item/*.simple_spinner_dropdown_item*/);
		spinner = (Spinner) findViewById(R.id.spPair);
		spinner.setAdapter(sAdapter);
		spinner.setOnItemSelectedListener(this);
		
		db = new DB(this);
		db.open();
		
		if (oldCursor) {
			cursor_old = db.getAllData();
			startManagingCursor(cursor_old);
		} else {
			getSupportLoaderManager().initLoader(0, null, this);// for CursorLoader support FragmentActivity
		}
		
		sPref = PreferenceManager.getDefaultSharedPreferences(this);
		//sPref = getSharedPreferences("MainActivity", 0);
		if (!sPref.contains("orderForward")) {
			sPref.edit().putBoolean("orderForward", false).commit();
		}
		//sPref.edit().clear();
		db.orderForward = sPref.getBoolean(SortActivity.PREF_ORDER_F, false);
		
		try {
				//SimpleCursorAdapter sca = new SimpleCursorAdapter(context, layout, c, from, to, flafs);
				mrcAdapter = new MyResourceCursorAdapter(this, R.layout.item, /*cursor*/null, true);
			}catch (Exception e) {
				Log.d(LOG, e.getMessage());
			}
			lvForm.setAdapter(mrcAdapter);
		
		registerForContextMenu(lvForm);// need onCreateContextMenu(...);
	}

	/*@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		super.onCreatePanelMenu(featureId, menu);
		return true;
	}*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		//menu.add(0 /*groupID*/, MENU_ADD /*ID*/, 1 /*Order*/, R.string.MenuAdd /*text*/);
		//menu.add(0, MENU_SORT, 2, R.string.MenuSort);
		menu.add(0, MENU_ADVANCED, 3, R.string.MenuAdvanced);
		menu.add(0, MENU_ABOUT, 4, R.string.MenuAbout);
		//menu.add(0, MENU_PREF, 5, R.string.MenuPref);
		super.onCreateOptionsMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menu) {
		switch (menu.getItemId()) {
		case MENU_ADD:
			addRecord(RECORD_ADD, 0);
			break;
		case MENU_SORT:
			showDialog(DIALOG_SORT);
			break;
		case MENU_ADVANCED:
			Intent intent = new Intent(this, SortActivity.class);
			startActivityForResult(intent, REQUEST_1);
			break;
		case MENU_ABOUT:
			showDialog(DIALOG_ABOUT);
			//Toast.makeText(this, "Make by RR Lab", Toast.LENGTH_LONG).show();
			break;
		}
		return true;
	}
	
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		switch (id) {
		// Sorting parameters
		case DIALOG_SORT:
			adb.setTitle(R.string.Sort);
			adb.setItems(R.array.SortItems, sortDialogListener);
			break;
		//Call Statistic parameters activity
		case DIALOG_ABOUT:
			adb.setTitle(R.string.MenuAbout);
			adb.setIcon(R.drawable.ic_launcher);
			String aboutMessage = getString(R.string.AboutMessage);
			try {
				aboutMessage += "\n" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			adb.setMessage(aboutMessage);
			adb.setPositiveButton("OK", null);
			break;
		}
		return adb.create();
	}
	
	android.content.DialogInterface.OnClickListener sortDialogListener = new android.content.DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			//forward
			case 0:
				db.orderForward = true;
				break;
			//backward
			case 1:
				db.orderForward = false;
				break;
			}

			if (oldCursor) {
				cursor_old = db.getAllData();
				mrcAdapter.swapCursor(cursor_old);
				//mrcAdapter.changeCursor(cursor);//There is modified to view
			} else {
				getSupportLoaderManager().getLoader(0).forceLoad(); //instead requery
			}
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_1:
			if (resultCode == RESULT_OK) {
				// must modify
				int startDate[];
				int endDate[];
				db.pair = data.getStringExtra("pair");
				startDate = data.getIntArrayExtra("startTime");
				endDate = data.getIntArrayExtra("endTime");
				if (startDate != null && endDate != null) {
					Calendar cal = new GregorianCalendar(startDate[0],
							startDate[1], startDate[2]);
					db.startDate = cal.getTimeInMillis() / 1000L;
					cal = new GregorianCalendar(endDate[0], endDate[1],
							endDate[2], 23, 59, 59);
					db.endDate = cal.getTimeInMillis() / 1000L;
				} else {
					db.startDate = 0;
					db.endDate = 0;
				}

				if (oldCursor) {
					cursor_old = db.getAllData();
					//There is modified to view
					mrcAdapter.swapCursor(cursor_old);
					// mrcAdapter.changeCursor(cursor);
				} else {
					// instead requery
					getSupportLoaderManager().getLoader(0).forceLoad();
				}
				// SET LIST ORDER 
				if (sPref.contains(SortActivity.PREF_ORDER_F)) {
					db.orderForward = sPref.getBoolean(SortActivity.PREF_ORDER_F, false);
				}
			}
			break;
			
		case 2:
			// There are no results 
			break;
		}
	}
	
	@Override
	public void onCreateContextMenu(android.view.ContextMenu menu, View v,
			android.view.ContextMenu.ContextMenuInfo menuInfo) {
		
		menu.add(0, C_MENU_EDIT, 1, R.string.CMenuEdit);
		menu.add(1, C_MENU_DELETE, 2, R.string.CMenuDelete);
		menu.add(/*groupId*/ 0, /*itemId*/ C_MENU_CANCEL, /*order*/ 3, R.string.CMenuCancel);
		super.onCreateContextMenu(menu, v, menuInfo);
	};
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		//Log.d(LOG, "AdapterContextMenuInfo ID " + acmi.id);
		//Log.d(LOG, "AdapterContextMenuInfo Position " + acmi.position);
		switch (item.getItemId()) {
			// editt selected item of db
		case C_MENU_EDIT:
			record_id = acmi.id;
			edit_mode = true;
			btnAdd.setText(R.string.OK_name); // set O.K. name
			getData(record_id);
			break;
			// delete record from db
		case C_MENU_DELETE:
			db.delete(acmi.id);
			
			if (oldCursor) {
				cursor_old.requery();
			} else {
				getSupportLoaderManager().getLoader(0).forceLoad(); //instead requery
			}

			break;
			// canceling action
		case C_MENU_CANCEL:
			edit_mode = false;
			btnAdd.setText(R.string.add_name);  // set add name
			break;
		}
		return super.onContextItemSelected(item);
	}
		
	void getData(long record_id) { // get data from 
		int lot, entry, sl, tp, oprice;
		String pair;
		Cursor cursorLocal;
		
		cursorLocal = CLoader.getCursor();
		
		//String operation;
		lot = cursorLocal.getInt(cursorLocal.getColumnIndex(DB.LOT));
		if (Integer.signum(lot) < 0) {
			lot = -lot;
			tbtnBuy.setChecked(false); // sell
		} else {
			tbtnBuy.setChecked(true); // buy
		}

		// set the spinner value 
		pair = cursorLocal.getString(cursorLocal.getColumnIndex(DB.PAIR));
		for(int i=0; i < sAdapter.getCount(); i++) {
		  if(pair.equals(sAdapter.getItem(i).toString())){
		    spinner.setSelection(i);
		    break;
		  }
		}
		
		etLot.setText( String.format(Locale.US, "%1.2f", /*Float.toString(*/ lot * 0.01f/*)*/));// format output
		// divide by 100 to normalize
		
		entry = cursorLocal.getInt(cursorLocal.getColumnIndex(DB.ENTRY));
		etEntry.setText(String.format(Locale.US, "%1.4f",  entry * 0.0001f));
		sl = cursorLocal.getInt(cursorLocal.getColumnIndex(DB.STOP_LOSS));
		etSL.setText(String.format(Locale.US, "%1.4f",  sl * 0.0001f));
		tp = cursorLocal.getInt(cursorLocal.getColumnIndex(DB.TAKE_PROFIT));
		etTP.setText(String.format(Locale.US, "%1.4f",  tp * 0.0001f));
		oprice = cursorLocal.getInt(cursorLocal.getColumnIndex(DB.OUT_PRICE));
		etOprice.setText(String.format(Locale.US, "%1.4f",  oprice * 0.0001f));
	}

	boolean addRecord(int command, long data) {
		int lot, date, entry , sl, tp, oprice, pos = 1;
		String S_lot, S_entry, S_sl, S_tp, S_oprice;
		float fTmp;
		boolean flag, valid = true;
		//int command; // to determinate operation with data
		
		// lot 99.00 - 0.01
		S_lot = etLot.getText().toString();
		flag = validateDot2(S_lot);
		if (!flag) {
			// Toast.makeText(this, "Wrong arguments in lot", Toast.LENGTH_SHORT).show();
			etLot.setError("Range 99-0.01");
			valid = false;
		}

		// entry 9 - 0.0001
		S_entry = etEntry.getText().toString();
		flag = validateDot4(S_entry);
		if (!flag) {
			etEntry.setError("Range 9000-0.0001");
			valid = false;
		}
		
		// Stop loss 9 - 0.0001
		S_sl = etSL.getText().toString();
		flag = validateDot4(S_sl);
		if (!flag) {
			etSL.setError("Range 9000-0.0001");
			valid = false;
		}
		
		// Take profit 9 - 0.0001
		S_tp = etTP.getText().toString();
		flag = validateDot4(S_tp);
		if (!flag) {
			etTP.setError("Range 9000-0.0001");
			valid = false;
		}
		
		// Out price 9 - 0.0001
		S_oprice = etOprice.getText().toString();
		flag = validateDot4(S_oprice);
		if (!flag) {
			etOprice.setError("Range 9000-0.0001");
			valid = false;
		}
		
		
		// if one or more strings are not validate
		if (!valid) {
			return false;
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
		switch (command) {
		case RECORD_ADD:
			db.addRec(spName, lot, date, entry, sl, tp, oprice, pos);	
			break;
		case RECORD_UPDATE:
			db.update(spName, lot, date, entry, sl, tp, oprice, pos, data);
		}

		if (oldCursor) {
			cursor_old.requery();
		} else {
			getSupportLoaderManager().getLoader(0).forceLoad();// instead requery
		}
		return true;
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
		String pattern = "^[0-9]{1,4}([.]{1}[0-9]{1,4})?";
		return Pattern.matches(pattern, text);
	}
	
	protected void onDestroy() {
		//sPref.edit().clear().commit();
		sPref.edit().remove(SortActivity.PREF_PAIR_CHEKED).commit();
		sPref.edit().remove(SortActivity.PREF_PERIOD_CHEKED).commit();
		sPref.edit().remove(SortActivity.PREF_PAIR).commit();
		sPref.edit().remove(SortActivity.PREF_START_TIME).commit();
		sPref.edit().remove(SortActivity.PREF_END_TIME).commit();
		
		adBottom.destroy();
		
		super.onDestroy();
		//db.close(); This is wrong command db closes automatically on program destroy
	}

	@Override
	public void onPause() {
		adBottom.pause();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();// must be first
		adBottom.resume();
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		spName = parent.getItemAtPosition(position).toString();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnAdd:
			if (edit_mode) { // Edit record mode
				if (record_id == 0) {
					edit_mode = false;
					return;
				}
				if (addRecord(RECORD_UPDATE, record_id)) { //if update is successful
					edit_mode = false;
					record_id = 0;
					btnAdd.setText(R.string.add_name);// set ADD name
				}
			} else { // Add new record mode
				addRecord(RECORD_ADD, 0);
				
				if (oldCursor) {
					cursor_old.requery();
				} else {
					getSupportLoaderManager().getLoader(0).forceLoad();// instead requery
				}
			}
			break;
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
		
		CLoader = new MyCursorLoader(this, db);
		return CLoader;//new MyCursorLoader(this, db);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mrcAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {		
	}
	
	static class MyCursorLoader extends CursorLoader {

		DB db;
		Cursor cursor;

		public MyCursorLoader(Context context, DB db) {
			super(context);
			this.db = db;
		}
		
		public Cursor getCursor() {
			return cursor;
		}

		@Override
		public Cursor loadInBackground() {
			cursor = db.getAllData();

			try {
				// TimeUnit.SECONDS.sleep(3);
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return cursor;
		}

	}// end of inner class MyCursorLoader
	
	public class MyResourceCursorAdapter extends ResourceCursorAdapter {

		public MyResourceCursorAdapter(Context context, int layout, Cursor c,
				boolean autoRequery) {
			super(context, layout, c, autoRequery);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			CharSequence str;
			DateFormat df;
			String operation;
			int lot, entry, sl, tp, oprice;
			
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
			
			//df = new SimpleDateFormat("yyyy.MM.dd HH:mm");// * Deprecate *
			df = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
			str = df.format(new Date(1000L * cursor.getInt(cursor.getColumnIndex(DB.DATE))));
			tv3.setText(str);
			
			entry = cursor.getInt(cursor.getColumnIndex(DB.ENTRY));
			tv4.setText(String.format("%1.4f",  entry * 0.0001f));
			sl = cursor.getInt(cursor.getColumnIndex(DB.STOP_LOSS));
			tv5.setText(String.format("%1.4f",  sl * 0.0001f));
			tp = cursor.getInt(cursor.getColumnIndex(DB.TAKE_PROFIT));
			tv6.setText(String.format("%1.4f",  tp * 0.0001f));
			oprice = cursor.getInt(cursor.getColumnIndex(DB.OUT_PRICE));
			tv7.setText(String.format("%1.4f",  oprice * 0.0001f));
		}

	}// end of inner class MyResourceCursorAdapter

}