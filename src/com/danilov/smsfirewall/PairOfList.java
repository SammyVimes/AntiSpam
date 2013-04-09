package com.danilov.smsfirewall;

import java.util.ArrayList;

public class PairOfList {
	ArrayList<String> names;
	ArrayList<String> phones;
	
	public PairOfList(ArrayList<String> names, ArrayList<String> phones){
		this.names = names;
		this.phones = phones;
	}
	public ArrayList<String> getNames() {
		return names;
	}
	public ArrayList<String> getPhones() {
		return phones;
	}
	
}
