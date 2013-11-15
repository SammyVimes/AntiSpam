package com.danilov.smsfirewall;

import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.danilov.smsfirewall.adapter.SmsAdapter;

public class SpamStoreActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spam_store);
		DBSpamCacheHelper helper = new DBSpamCacheHelper(this);
		List<Sms> list = helper.getAsList();
		SmsAdapter adapter = new SmsAdapter(getBaseContext(), list);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.spam_store, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch(item.getItemId()){
			case R.id.menu_settings:
				intent = new Intent(this, SettingsActivity.class);
				this.startActivity(intent);
				break;
			case R.id.menu_blacklist:
				intent = new Intent(this, BlackListActivity.class);
				this.startActivity(intent);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

}
