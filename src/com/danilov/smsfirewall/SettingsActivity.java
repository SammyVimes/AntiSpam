package com.danilov.smsfirewall;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class SettingsActivity extends SherlockFragmentActivity {
	
	public static final String BLOCK_CALLS_PARAMETER = "BLOCK_CALLS_PARAMETER";
	public static final String BLOCK_UNKNOWN_PARAMETER = "BLOCK_UNKNOWN_PARAMETER";
	public static final String SHOW_NOTIFICATION_PARAMETER = "SHOW_NOTIFICATION_PARAMETER";
	public static final String STORE_SPAM_DAYS = "STORE_SPAM_DAYS";
	
	private CheckBox blockCalls;
	private CheckBox blockUnknown;
	private CheckBox showNotification;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		blockCalls = (CheckBox) findViewById(R.id.blockCalls);
		blockUnknown = (CheckBox) findViewById(R.id.blockUnknown);
		showNotification =  (CheckBox) findViewById(R.id.showNotification);
		Button button = (Button)findViewById(R.id.suspiciousButton);
		Button whiteListButton = (Button)findViewById(R.id.whiteListButton);
		Button sendLogsButton = (Button) findViewById(R.id.sendLogsBtn);
		Button clearLogsButton = (Button) findViewById(R.id.clearLogsBtn);
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
					case R.id.sendLogsBtn:
						sendLogsEmail();
						break;
					case R.id.clearLogsBtn:
						DeleteLogsDialog d = new DeleteLogsDialog();
						d.show(getSupportFragmentManager(), "DeleteLogsDialog");
						break;
				}
			}
			
		};
		button.setOnClickListener(listener);
		whiteListButton.setOnClickListener(listener);
		sendLogsButton.setOnClickListener(listener);
		clearLogsButton.setOnClickListener(listener);
		
		refreshLogsState();
	}
	
	private void refreshLogsState() {
		TextView logView = (TextView) findViewById(R.id.logFileSizeView);
		String filePath = MyAndroidLogger.PATH + MyAndroidLogger.fileName;
		File f = new File(filePath);
		boolean fileExists = f.exists();
		long size = f.length();
		String logText = fileExists ? "Size: " + (size / 1024) + " kb" : "File not exists";  
		logView.setText(logText);
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
	
	private void sendLogsEmail() {
		String filePath = MyAndroidLogger.PATH + MyAndroidLogger.fileName;
		File f = new File(filePath);
		boolean fileExists = f.exists();
		if (!fileExists) {
		    Toast.makeText(this, "Log file not found", Toast.LENGTH_LONG).show();
			return;
		}
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"senya.danilov@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "АнтиСпам - логи");
		i.putExtra(Intent.EXTRA_TEXT   , "Привет. Вот логи.");
		i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
		i.setType("message/rfc822");
		try {
		    startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, "There are no email clients installed. Can't send logs:(", Toast.LENGTH_LONG).show();
		}
	}
	
	private void saveSettings(){
		SharedPreferences sPref = getSharedPreferences("preferences", MODE_WORLD_READABLE);
		Editor ed = sPref.edit();
		ed.putBoolean(BLOCK_CALLS_PARAMETER, blockCalls.isChecked());
		ed.putBoolean(BLOCK_UNKNOWN_PARAMETER, blockUnknown.isChecked());
		ed.putBoolean(SHOW_NOTIFICATION_PARAMETER, showNotification.isChecked());
		EditText t = (EditText) findViewById(R.id.storeSpamDays);
		int days = 0;
		try {
			days = Integer.valueOf(t.getText().toString());
			if (days < 0) {
				days = 1;
			}
		} catch (Exception e) {
			Util.Log(e.getMessage());
		}
		ed.putInt(STORE_SPAM_DAYS, days);
		ed.commit();
	}
	
	private void loadSettings(){
		SharedPreferences sPref = getSharedPreferences("preferences", MODE_WORLD_READABLE);
	    boolean blockUnknowChecked = sPref.getBoolean(BLOCK_UNKNOWN_PARAMETER, false);
	    boolean blockCallsChecked = sPref.getBoolean(BLOCK_CALLS_PARAMETER, false);
	    boolean showNotificationsChecked = sPref.getBoolean(SHOW_NOTIFICATION_PARAMETER, false);
		int days = sPref.getInt(STORE_SPAM_DAYS, 1);
		EditText t = (EditText) findViewById(R.id.storeSpamDays);
		t.setText(String.valueOf(days));
	    blockCalls.setChecked(blockCallsChecked);
	    blockUnknown.setChecked(blockUnknowChecked);
	    showNotification.setChecked(showNotificationsChecked);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.settings_activity, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch(item.getItemId()){
			case R.id.rate:
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("market://details?id=com.danilov.smsfirewall"));
				try {
					this.startActivity(i);
				} catch (Exception e) {
					Util.toaster(this, "Market is not installed");
					e.printStackTrace();
				}
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public static class DeleteLogsDialog extends EasyDialogFragment {
		
		@Override
		public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
			if(savedInstanceState != null){
				restoreSavedInstanceState(savedInstanceState);
			}
		    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
		        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						String filePath = MyAndroidLogger.getSdPath() + MyAndroidLogger.fileName;
						File f = new File(filePath);
						boolean fileExists = f.exists();
						if (fileExists) {
							f.delete();
						}
						filePath = MyAndroidLogger.getInternalPath() + MyAndroidLogger.fileName;
						f = new File(filePath);
						fileExists = f.exists();
						if (fileExists) {
							f.delete();
						}
						((SettingsActivity) getActivity()).refreshLogsState();
					}
		        	
		        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						((SettingsActivity) getActivity()).refreshLogsState();
						dismiss();
					}
				})
		        .setMessage("Delete logs?").setTitle("Logs deleting");
		    return builder.create();
		}
		
	}
	
}
