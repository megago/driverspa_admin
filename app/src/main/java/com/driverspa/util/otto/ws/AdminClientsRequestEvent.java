package com.driverspa.util.otto.ws;

import com.driverspa.model.AdminSearchFilter;


public class AdminClientsRequestEvent {

	private AdminSearchFilter searchFilter;

	public AdminClientsRequestEvent() {
		this(new AdminSearchFilter());
	}

	public AdminClientsRequestEvent(AdminSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
	public AdminSearchFilter getSearchFilter() {
		return searchFilter;
	}

}
