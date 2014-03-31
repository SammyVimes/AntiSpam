package com.danilov.smsfirewall;

	import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

	public class MessagesAdapter extends BaseAdapter {


		private LayoutInflater inflater;
		private ArrayList<String> senders;
		private ArrayList<String> messages;
		private ArrayList<String> dates;
		private int size;

		public  MessagesAdapter(Context context, ArrayList<String> senders, ArrayList<String> messages, ArrayList<String> dates) {
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.senders = senders;
			this.messages =  messages;
			this.dates = dates;
			size = messages.size();
		}


		@Override
		public int getCount() {
			return size;
		}

		@Override
		public String getItem(int position) {
			return senders.get(position);
		}
		
		public void setAll(ArrayList<String> senders, ArrayList<String> messages, ArrayList<String> dates, int size){
			this.senders = senders;
			this.messages =  messages;
			this.dates = dates;
			this.size += size;
			notifyDataSetChanged();
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
			viewholder.sender.setText(senders.get(position));
			viewholder.message.setText(messages.get(position));
			viewholder.date.setText(dates.get(position));
		return convertView;
		}
	}
	