package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.BaseResponseHolder;

public class AdminShopItemDeleteResponseEvent {

	private final BaseResponseHolder result;

	public AdminShopItemDeleteResponseEvent(BaseResponseHolder result) {
		this.result = result;
	}

	public BaseResponseHolder getResult() {
		return result;
	}
}
