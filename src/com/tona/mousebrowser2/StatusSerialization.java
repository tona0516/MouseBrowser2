package com.tona.mousebrowser2;

import java.io.Serializable;
import java.util.ArrayList;

public class StatusSerialization implements Serializable{
	private static final long serialVersionUID = 6255752248513019027L;
	private ArrayList<String> LastTimeUrlList;
	public ArrayList<String> getLastTimeUrlList() {
		return LastTimeUrlList;
	}
	public void setLastTimeUrlList(ArrayList<String> lastTimeUrlList) {
		LastTimeUrlList = lastTimeUrlList;
	}
}
