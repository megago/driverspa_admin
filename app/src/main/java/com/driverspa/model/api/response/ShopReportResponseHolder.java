package com.driverspa.model.api.response;

import com.driverspa.model.ShopReport;
import com.google.gson.annotations.SerializedName;

public class ShopReportResponseHolder extends BaseResponseHolder {

	@SerializedName("data")
	private ShopReport response;

	public ShopReport getResponse() {
		return response;
	}
}
