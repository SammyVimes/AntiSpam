package com.danilov.smsfirewall;

public class Sms {
	
	private String address; 
	private String text;
	private long date;
	
	private int id;
	
	public Sms() {
		
	}

	public Sms(final String address, final String text, final long date) {
		this.address = address;
		this.text = text;
		this.date = date;
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}

}
