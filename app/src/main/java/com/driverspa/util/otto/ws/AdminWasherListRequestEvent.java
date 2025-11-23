package com.driverspa.util.otto.ws;

import com.driverspa.model.SearchFilter;


public class AdminWasherListRequestEvent {

	private SearchFilter searchFilter;
	private String washerName;

	public AdminWasherListRequestEvent(SearchFilter searchFilter, String washerName) {
		this.searchFilter = searchFilter;
		this.washerName = washerName;
	}

	public AdminWasherListRequestEvent(String washerName) {
		this.washerName = washerName;
	}

	public String getWasherName() {
		return washerName;
	}

	public AdminWasherListRequestEvent() {
		this(new SearchFilter());
	}
	
	public AdminWasherListRequestEvent(SearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
	public SearchFilter getSearchFilter() {
		return searchFilter;
	}

}
