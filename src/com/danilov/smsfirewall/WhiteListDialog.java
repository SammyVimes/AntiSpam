package com.danilov.smsfirewall;

import com.danilov.smsfirewall.Dialog.DialogListener;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class WhiteListDialog extends Dialog {
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		      Bundle savedInstanceState) {
			String title = getActivity().getResources().getString(R.string.whiteList);
		    getDialog().setTitle(title);
		    View v = inflater.inflate(R.layout.dialog_db, null);
		    v.findViewById(R.id.dialogButtonAdd).setOnClickListener(this);
		    v.findViewById(R.id.dialogButtonYes).setOnClickListener(this);
		    list = (ListView) v.findViewById(R.id.dialogListView);
		    editText = (EditText) v.findViewById(R.id.dialogEditText);
		    loadList();
		    return v;
   }
	
	private void loadList(){
		DBHelperWhitelist helper = new DBHelperWhitelist(getActivity());
		array = helper.getAsNamesList();
		adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_1, array);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String str = adapter.getItem(position);
		MyDialog dialog = new MyDialog();
		dialog.setListener(new DialogListener(str));
		dialog.setMessage(str);
		dialog.show(getActivity().getSupportFragmentManager(), "miniDialog");
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch(id){
		case R.id.dialogButtonAdd:
			Context ctx = getActivity().getBaseContext();
			DBHelperWhitelist dbHelper = new DBHelperWhitelist(ctx);
			String string = editText.getText().toString();
			String name = Util.findNameInList(string, Util.getNameFromContacts(ctx));
			dbHelper.addToDb(name, string);
			editText.setText("");
			loadList();
			break;
		case R.id.dialogButtonYes:
			dismiss();
			break;
		}
	}
	
	@Override
	protected android.content.DialogInterface.OnClickListener getListener(final String word) {
		return new DialogListener(word);
	}
	
	public class DialogListener implements android.content.DialogInterface.OnClickListener{

		private String name;
		
		public DialogListener(final String word) {
			this.name = word;
		}
		
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			new DBHelperWhitelist(getActivity().getBaseContext()).deleteFromDb(name);
			   WhiteListDialog d = (WhiteListDialog) getActivity().getSupportFragmentManager().findFragmentByTag("mainDialog");
			   if(d != null){
				   d.loadList();
			   }
		}
		
	}
}
