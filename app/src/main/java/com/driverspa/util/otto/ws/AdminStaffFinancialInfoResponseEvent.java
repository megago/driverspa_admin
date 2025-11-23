package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminStaffFinancialResponseHolder;

public class AdminStaffFinancialInfoResponseEvent {

	AdminStaffFinancialResponseHolder data;

	public AdminStaffFinancialInfoResponseEvent(AdminStaffFinancialResponseHolder data) {
		this.data = data;
	}

   public AdminStaffFinancialResponseHolder getResult(){
	   return this.data;
   }
   
}
