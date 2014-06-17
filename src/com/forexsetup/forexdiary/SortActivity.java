package com.forexsetup.forexdiary;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

public class SortActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {

	final static int DIALOG_DATE_START = 1;
	final static int DIALOG_DATE_END = 2;

	int StartYear, StartMonth, StartDay;
	int EndYear, EndMonth, EndDay;
	int startDate[] = new int[3];//
	int endDate[] = new int[3];//
	String pairName;
	Calendar cal;
	Date date;
	long timeUTC;
	SimpleDateFormat df;

	CheckBox chbPair;
	CheckBox chbPeriod;
	CheckBox chbProfit;
	CheckBox chbOrder;
	Button btnStartTime;
	Button btnEndTime;
	Button btnSortList;
	Button btnCalcPoints;
	TextView tvStartTime;
	TextView tvEndTime;
	Spinner spPair;
	ArrayAdapter<CharSequence> sAdapter; // spinner adapter

	SharedPreferences sPref;// for options saving

	final static String PREF_PAIR = "pair";
	final static String PREF_ORDER_F = "orderForward";
	final static String PREF_START_TIME = "startTime";
	final static String PREF_END_TIME = "endTime";
	final static String PREF_PAIR_CHEKED = "pairCheked";
	final static String PREF_PERIOD_CHEKED = "periodCheked";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sort);

		btnStartTime = (Button) findViewById(R.id.btnStartTime);
		btnEndTime = (Button) findViewById(R.id.btnEndTime);
		btnSortList = (Button) findViewById(R.id.btnSortList);
		btnCalcPoints = (Button) findViewById(R.id.btnCalcPoints);
		// set buttons listener
		btnStartTime.setOnClickListener(this);
		btnEndTime.setOnClickListener(this);
		btnSortList.setOnClickListener(this);
		btnCalcPoints.setOnClickListener(this);

		tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		tvEndTime = (TextView) findViewById(R.id.tvEndTime);

		chbPair = (CheckBox) findViewById(R.id.chbPair);
		chbPeriod = (CheckBox) findViewById(R.id.chbPeriod);
		chbProfit = (CheckBox) findViewById(R.id.chbProfit);
		chbOrder = (CheckBox) findViewById(R.id.chbOrder);
		// set chekBox listener
		chbPair.setOnCheckedChangeListener(checkBoxListener);
		chbPeriod.setOnCheckedChangeListener(checkBoxListener);
		chbProfit.setOnCheckedChangeListener(checkBoxListener);
		chbOrder.setOnCheckedChangeListener(checkBoxListener);

		// create spinner adapter for PAIR
		sAdapter = ArrayAdapter.createFromResource(this, R.array.pair,
				android.R.layout.simple_spinner_item);
		sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		spPair = (Spinner) findViewById(R.id.spPair);
		spPair.setAdapter(sAdapter);
		spPair.setOnItemSelectedListener(this);
		spPair = (Spinner) findViewById(R.id.spPair);

		// preinitialize state
		sPref = PreferenceManager.getDefaultSharedPreferences(this);

		// ORDER MANIPULATION
		chbOrder.setChecked(sPref.getBoolean(PREF_ORDER_F, false));

		// PAIR MANIPULATION
		// pair checked
		chbPair.setChecked(sPref.getBoolean(PREF_PAIR_CHEKED, false));
		if (!chbPair.isChecked()) {
			spPair.setEnabled(false);
		}
		// pair value
		pairName = sPref.getString(PREF_PAIR, "");
		for (int i = 0; i < sAdapter.getCount(); i++) {
			if (pairName.equals(sAdapter.getItem(i).toString())) {
				spPair.setSelection(i);
				break;
			}
		}

		// PERIOD MANIPULATION
		// pair checked
		chbPeriod.setChecked( sPref.getBoolean(PREF_PERIOD_CHEKED, false));
		if (!chbPeriod.isChecked()) {
			btnStartTime.setEnabled(false);
			btnEndTime.setEnabled(false);
		}
		// pair value
		df = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
		cal = Calendar.getInstance();
		
		periodSetting();
	

		if (!chbProfit.isChecked()) {
			// spPair.setEnabled(false);
		}

	}

	OnCheckedChangeListener checkBoxListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {

			switch (buttonView.getId()) {
			case R.id.chbPair:
				if (isChecked) {
					spPair.setEnabled(true);
					sPref.edit().putBoolean(PREF_PAIR_CHEKED, true).commit();
				} else {
					spPair.setEnabled(false);
					sPref.edit().putBoolean(PREF_PAIR_CHEKED, false).commit();
				}
				break;
			case R.id.chbPeriod:
				if (isChecked) {
					btnStartTime.setEnabled(true);
					btnEndTime.setEnabled(true);
					sPref.edit().putBoolean(PREF_PERIOD_CHEKED, true).commit();
				} else {
					btnStartTime.setEnabled(false);
					btnEndTime.setEnabled(false);
					sPref.edit().putBoolean(PREF_PERIOD_CHEKED, false).commit();
				}
				break;
			case R.id.chbProfit:
				if (isChecked) {

				} else {

				}
				break;
			case R.id.chbOrder:
				if (isChecked) {
					sPref.edit().putBoolean(PREF_ORDER_F, true).commit();
				} else {
					sPref.edit().putBoolean(PREF_ORDER_F, false).commit();
				}
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnStartTime:
			showDialog(DIALOG_DATE_START);
			break;
		case R.id.btnEndTime:
			showDialog(DIALOG_DATE_END);
			break;
		case R.id.btnSortList:
			String Str = "";
			Intent intent = new Intent();
			if (chbPair.isChecked()) {
				Str = spPair.getItemAtPosition(spPair.getLastVisiblePosition()).toString();
				sPref.edit().putString(PREF_PAIR, Str).commit();
			}
			intent.putExtra("pair", Str);
			if (chbPeriod.isChecked()) {
				intent.putExtra("startTime", startDate); // preset value 
				intent.putExtra("endTime", endDate); // preset value
			}
			setResult(RESULT_OK, intent);
			finish();
			break;
		}
	}

	// PERIOD MANIPULATION
	public void periodSetting() {
	
		// cal, df was initial in onCreate
		timeUTC = sPref.getLong(PREF_START_TIME, System.currentTimeMillis() / 1000L);
		cal.setTimeInMillis(timeUTC * 1000L);
		tvStartTime.setText(df.format(cal.getTime()));
		
		startDate[0] = cal.get(Calendar.YEAR);
		startDate[1] = cal.get(Calendar.MONTH);
		startDate[2] = cal.get(Calendar.DAY_OF_MONTH);

		timeUTC = sPref.getLong(PREF_END_TIME, System.currentTimeMillis() / 1000L);
		cal.setTimeInMillis(timeUTC * 1000L);
		tvEndTime.setText(df.format(cal.getTime()));
		
		endDate[0] = cal.get(Calendar.YEAR);
		endDate[1] = cal.get(Calendar.MONTH);
		endDate[2] = cal.get(Calendar.DAY_OF_MONTH);
	}
	
	protected Dialog onCreateDialog(int id) {
		DatePickerDialog dpd;

		switch (id) {
		case DIALOG_DATE_START:
			cal = new GregorianCalendar();
			
			StartYear = cal.get(Calendar.YEAR);
			StartMonth = cal.get(Calendar.MONTH);
			StartDay = cal.get(Calendar.DAY_OF_MONTH);
			dpd = new DatePickerDialog(this, CallBackStart, StartYear, StartMonth, StartDay);
			
			return dpd;

		case DIALOG_DATE_END:
			/***
			 * old version deprecated *** d = new Date(); myYear = d.getYear();
			 * // -1900 myMonth = d.getMonth(); myDay = d.getDay();
			 */
			
			EndYear = cal.get(Calendar.YEAR);
			EndMonth = cal.get(Calendar.MONTH);
			EndDay = cal.get(Calendar.DAY_OF_MONTH);
			dpd = new DatePickerDialog(this, CallBackEnd, EndYear/* + 1900 */, EndMonth, EndDay);
			
			return dpd;
		}
		return super.onCreateDialog(id);
	}

	/*
	protected void onPrepareDialog(int id, Dialog dialog,Bundle args) {
		super.onPrepareDialog(id, dialog, args);
	}
	*/
	
	OnDateSetListener CallBackStart = new OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int DayOfMonth) {

			startDate[0] = year;
			startDate[1] = monthOfYear;
			startDate[2] = DayOfMonth;
			cal.set(year, monthOfYear, DayOfMonth);

			tvStartTime.setText(df.format(cal.getTime()));
			timeUTC = cal.getTimeInMillis() / 1000L;
			sPref.edit().putLong(PREF_START_TIME, timeUTC).commit();
		}
	};

	OnDateSetListener CallBackEnd = new OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int DayOfMonth) {

			Calendar cal = new GregorianCalendar(year, monthOfYear, DayOfMonth);
			endDate[0] = cal.get(Calendar.YEAR);
			endDate[1] = cal.get(Calendar.MONTH);
			endDate[2] = cal.get(Calendar.DAY_OF_MONTH);
			
			tvEndTime.setText(df.format(cal.getTime()));
			timeUTC = cal.getTimeInMillis() / 1000L;
			sPref.edit().putLong(PREF_END_TIME, timeUTC).commit();
		}
	};

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
