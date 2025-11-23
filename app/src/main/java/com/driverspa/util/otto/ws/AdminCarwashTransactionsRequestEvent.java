package com.driverspa.util.otto.ws;

import com.driverspa.model.AdminSearchFilter;

public class AdminCarwashTransactionsRequestEvent {
     private AdminSearchFilter filter;

	 public AdminCarwashTransactionsRequestEvent(AdminSearchFilter filter){
    	 this.filter = filter;
     }

	public AdminSearchFilter getFilter() {
		return filter;
	}

	public void setFilter(AdminSearchFilter filter) {
		this.filter = filter;
	}
}
