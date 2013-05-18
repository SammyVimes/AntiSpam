package com.danilov.smsfirewall;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

public class Dialog extends DialogFragment implements OnClickListener, OnItemClickListener{
	
	ListView list;
	EditText editText;
	ArrayList<String> array = new ArrayList<String>();
	ArrayList<Integer> idArray = new ArrayList<Integer>();
	ArrayAdapter<String> adapter;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		      Bundle savedInstanceState) {
			String title = getActivity().getResources().getString(R.string.suspicious);
		    getDialog().setTitle(title);
		    View v = inflater.inflate(R.layout.dialog_suspicious, null);
		    v.findViewById(R.id.dialogButtonAdd).setOnClickListener(this);
		    v.findViewById(R.id.dialogButtonYes).setOnClickListener(this);
		    list = (ListView) v.findViewById(R.id.dialogListView);
		    editText = (EditText) v.findViewById(R.id.dialogEditText);
		    loadList();
		    return v;
     }
	
	private void loadList(){
		array.clear();
		idArray.clear();
		DBHelperSuspicious dbHelper = new DBHelperSuspicious(getActivity().getBaseContext());
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.query("mytable", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int wordColIndex = c.getColumnIndex("word");
			int idColIndex = c.getColumnIndex("id");
			do {
				idArray.add(c.getInt(idColIndex));
				array.add(c.getString(wordColIndex));
		    } while (c.moveToNext());
		}
		c.close();
		adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_1, array);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		db.close();
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String str = adapter.getItem(position);
		MyDialog dialog = new MyDialog();
		dialog.setMessage(str, idArray.get(position));
		dialog.show(getActivity().getSupportFragmentManager(), "miniDialog");
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch(id){
		case R.id.dialogButtonAdd:
			DBHelperSuspicious db = new DBHelperSuspicious(getActivity().getBaseContext());
			String word = editText.getText().toString();
			db.addToDb(word);
			editText.setText("");
			loadList();
			break;
		case R.id.dialogButtonYes:
			dismiss();
			break;
		}
	}
	
	public static class MyDialog extends DialogFragment{
		
		private static String STRING_KEY = "STRING";
		private static String ID_KEY = "ID";
		
		
		private String word;
		private int id;
		
		public void setMessage(String word, int id){
			this.word = word;
			this.id = id;
		}
		
		
		public AlertDialog onCreateDialog(Bundle savedInstanceState) {
			if(savedInstanceState != null){
				restoreSavedInstanceState(savedInstanceState);
			}
		    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
		        .setNeutralButton(R.string.delete, new DialogListener())
		        .setMessage(getResources().getString(R.string.text)+ " " + word + "?");
		    return builder.create();
		 }
		
		private void restoreSavedInstanceState(Bundle savedInstanceState){
			word = savedInstanceState.getString(STRING_KEY);
			id = savedInstanceState.getInt(ID_KEY);
		}
		
		
		@Override
		public void onSaveInstanceState(Bundle saved){
			saved.putString(STRING_KEY, word);
			saved.putInt(ID_KEY, id);
			super.onSaveInstanceState(saved);
		}
		
		@Override
		public void onDismiss(DialogInterface dialog) {
		   super.onDismiss(dialog);
		}
		
		public class DialogListener implements android.content.DialogInterface.OnClickListener{

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new DBHelperSuspicious(getActivity().getBaseContext()).deleteFromDb(id);
				   Dialog d = (Dialog) getActivity().getSupportFragmentManager().findFragmentByTag("mainDialog");
				   if(d != null){
					   d.loadList();
				   }
			}
			
		}
	
	}
}
	

