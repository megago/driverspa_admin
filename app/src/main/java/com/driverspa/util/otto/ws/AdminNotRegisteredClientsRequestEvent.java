package com.driverspa.util.otto.ws;

import com.driverspa.model.AdminSearchFilter;


public class AdminNotRegisteredClientsRequestEvent {

	private AdminSearchFilter searchFilter;

	public AdminNotRegisteredClientsRequestEvent() {
		this(new AdminSearchFilter());
	}

	public AdminNotRegisteredClientsRequestEvent(AdminSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
	public AdminSearchFilter getSearchFilter() {
		return searchFilter;
	}

}
