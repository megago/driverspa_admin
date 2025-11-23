package com.driverspa.util.otto.ws;

import com.driverspa.model.AdminSearchFilter;


public class AdminBooksRequestEvent {

	private AdminSearchFilter searchFilter;
	
	public AdminBooksRequestEvent() {
		this(new AdminSearchFilter());
	}
	
	public AdminBooksRequestEvent(AdminSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
	public AdminSearchFilter getSearchFilter() {
		return searchFilter;
	}

}
