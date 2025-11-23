package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminCashFinancialResponseHolder;

public class AdminCashFinancialResponseEvent {

	AdminCashFinancialResponseHolder data;

	public AdminCashFinancialResponseEvent(AdminCashFinancialResponseHolder data) {
		this.data = data;
	}

   public AdminCashFinancialResponseHolder getResult(){
	   return this.data;
   }
   
}
