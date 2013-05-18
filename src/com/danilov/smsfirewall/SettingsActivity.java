package com.danilov.smsfirewall;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;


public class SettingsActivity extends SherlockFragmentActivity {
	
	public static final String BLOCK_PARAMETER = "BLOCK_PARAMETER";
	public static final String CHECKED = "CHECKED";
	public static final String NOT_CHECKED = "NOT_CHECKED";
	
	CheckBox checkBox;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		checkBox = (CheckBox) findViewById(R.id.checkBox1);
		Button button = (Button)findViewById(R.id.suspiciousButton);
		button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Dialog dlg = new Dialog();
				dlg.show(getSupportFragmentManager(), "mainDialog");
			}
			
		});
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
		String parameter = CHECKED;
		if(!checkBox.isChecked()){
			parameter = NOT_CHECKED;
		}
		SharedPreferences sPref = getSharedPreferences("preferences", MODE_WORLD_READABLE);
		Editor ed = sPref.edit();
		ed.putString(BLOCK_PARAMETER, parameter);
		ed.commit();
	}
	
	private void loadSettings(){
		SharedPreferences sPref = getSharedPreferences("preferences", MODE_WORLD_READABLE);
	    String parameter = sPref.getString(BLOCK_PARAMETER, "");
	    if(parameter.equals(NOT_CHECKED)){
	    	checkBox.setChecked(false);
	    }else{
	    	checkBox.setChecked(true);
	    }
	}
	
}
