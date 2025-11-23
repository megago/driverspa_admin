package com.driverspa.util.otto.ws;

import com.driverspa.model.AdminSearchFilter;


public class NotificationNewsListRequestEvent {

	private AdminSearchFilter searchFilter;

	public NotificationNewsListRequestEvent() {
		this(new AdminSearchFilter());
	}

	public NotificationNewsListRequestEvent(AdminSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
	public AdminSearchFilter getSearchFilter() {
		return searchFilter;
	}

}
