package com.driverspa.util.otto.ws;

import com.driverspa.model.AdminSearchFilter;


public class AdminQueuedBooksRequestEvent {

	private AdminSearchFilter searchFilter;

	public AdminQueuedBooksRequestEvent() {
		this(new AdminSearchFilter());
	}

	public AdminQueuedBooksRequestEvent(AdminSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
	public AdminSearchFilter getSearchFilter() {
		return searchFilter;
	}

}
