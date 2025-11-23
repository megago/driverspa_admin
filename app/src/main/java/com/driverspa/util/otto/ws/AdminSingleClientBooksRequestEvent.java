package com.driverspa.util.otto.ws;

import com.driverspa.model.AdminSearchFilter;


public class AdminSingleClientBooksRequestEvent {

	private AdminSearchFilter searchFilter;

	public AdminSingleClientBooksRequestEvent() {
		this(new AdminSearchFilter());
	}

	public AdminSingleClientBooksRequestEvent(AdminSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	public AdminSearchFilter getSearchFilter() {
		return searchFilter;
	}

}
