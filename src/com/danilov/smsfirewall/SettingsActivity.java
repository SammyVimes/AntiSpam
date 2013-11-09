package com.danilov.smsfirewall;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;


public class SettingsActivity extends SherlockFragmentActivity {
	
	public static final String BLOCK_CALLS_PARAMETER = "BLOCK_CALLS_PARAMETER";
	public static final String BLOCK_UNKNOWN_PARAMETER = "BLOCK_UNKNOWN_PARAMETER";
	
	private CheckBox blockCalls;
	private CheckBox blockUnknown;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		blockCalls = (CheckBox) findViewById(R.id.blockCalls);
		blockUnknown = (CheckBox) findViewById(R.id.blockUnknown);
		Button button = (Button)findViewById(R.id.suspiciousButton);
		Button whiteListButton = (Button)findViewById(R.id.whiteListButton);
		OnClickListener listener = new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				switch (arg0.getId()) {
					case R.id.suspiciousButton:
						Dialog dlg = new Dialog();
						dlg.show(getSupportFragmentManager(), "mainDialog");
						break;
					case R.id.whiteListButton:
						WhiteListDialog wDlg = new WhiteListDialog();
						wDlg.show(getSupportFragmentManager(), "mainDialog");
						break;
				}
			}
			
		};
		button.setOnClickListener(listener);
		whiteListButton.setOnClickListener(listener);
	}
	
	@Override
	protected void onPause(){
		saveSettings();
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		loadSettings();
		super.onResume();
	}
	
	private void saveSettings(){
		SharedPreferences sPref = getSharedPreferences("preferences", MODE_WORLD_READABLE);
		Editor ed = sPref.edit();
		ed.putBoolean(BLOCK_CALLS_PARAMETER, blockCalls.isChecked());
		ed.putBoolean(BLOCK_UNKNOWN_PARAMETER, blockUnknown.isChecked());
		ed.commit();
	}
	
	private void loadSettings(){
		SharedPreferences sPref = getSharedPreferences("preferences", MODE_WORLD_READABLE);
	    boolean blockUnknowChecked = sPref.getBoolean(BLOCK_UNKNOWN_PARAMETER, false);
	    boolean blockCallsChecked = sPref.getBoolean(BLOCK_CALLS_PARAMETER, false);
	    blockCalls.setChecked(blockCallsChecked);
	    blockUnknown.setChecked(blockUnknowChecked);
	}
	
}
