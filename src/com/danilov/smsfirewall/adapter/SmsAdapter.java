package com.danilov.smsfirewall.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.danilov.smsfirewall.R;
import com.danilov.smsfirewall.Sms;

public class SmsAdapter extends BaseAdapter{
	private LayoutInflater inflater;
	private List<Sms> messages;
	private int size;

	public  SmsAdapter(final Context context, final List<Sms> messages) {
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.messages = messages;
		size = messages.size();
	}


	@Override
	public int getCount() {
		return size;
	}

	@Override
	public Sms getItem(int position) {
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		TextView sender;
		TextView message;
		TextView date;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewholder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.message, null);
			viewholder = new ViewHolder();
			viewholder.sender = (TextView) convertView.findViewById(R.id.senderView);
			viewholder.message = (TextView) convertView.findViewById(R.id.messageView);
			viewholder.date = (TextView) convertView.findViewById(R.id.dateView);
			convertView.setTag(viewholder);
		} else {
			viewholder = (ViewHolder) convertView.getTag();
		}
		Sms sms = messages.get(position);
		viewholder.sender.setText(sms.getAddress());
		viewholder.message.setText(sms.getText());
        Date date = new Date(sms.getDate());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd MMMMMMMMM");
        String dateString = sdf.format(date);
		viewholder.date.setText(dateString);
		return convertView;
	}
}
