package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AdminFinancialStatisticResponseHolder;

public class AdminFinancialStatisticsResponseEvent {

	AdminFinancialStatisticResponseHolder data;

	public AdminFinancialStatisticsResponseEvent(AdminFinancialStatisticResponseHolder data) {
		this.data = data;
	}

   public AdminFinancialStatisticResponseHolder getResult(){
	   return this.data;
   }
   
}
