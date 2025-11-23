package com.driverspa.util.otto;

public class SearchEvent {
	String query;
	public SearchEvent(String query){
		this.query = query;
	}

	public String getQuery(){
		return this.query;
	}
}
