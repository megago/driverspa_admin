package com.driverspa.util.otto.ws;

import com.driverspa.model.AdminSearchFilter;


public class AdminClientRequestEvent {

	private AdminSearchFilter searchFilter;

	public AdminClientRequestEvent() {
		this(new AdminSearchFilter());
	}

	public AdminClientRequestEvent(AdminSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
	public AdminSearchFilter getSearchFilter() {
		return searchFilter;
	}

}
