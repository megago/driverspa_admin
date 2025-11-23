package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.driverspa.model.CarwashTransaction;

public class AdminCarwashTransactionsResponse extends BaseResponse {

	@SerializedName("carwash_money_transactions")
	List<CarwashTransaction> carwashTransactions;

	public List<CarwashTransaction> getCarwashTransactions(){
		return carwashTransactions;
	}
}
