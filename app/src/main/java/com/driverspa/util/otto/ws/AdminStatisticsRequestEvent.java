package com.driverspa.util.otto.ws;

import com.driverspa.model.AdminSearchFilter;


public class AdminStatisticsRequestEvent {

	private AdminSearchFilter searchFilter;

	public AdminStatisticsRequestEvent() {
		this(new AdminSearchFilter());
	}

	public AdminStatisticsRequestEvent(AdminSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
	public AdminSearchFilter getSearchFilter() {
		return searchFilter;
	}

}
