package com.driverspa.util.otto.ws;


import com.driverspa.model.api.request.FareRequestBid;

public class AdminRequestMakeBidRequestEvent {


	FareRequestBid fareRequestBid;

	public AdminRequestMakeBidRequestEvent(FareRequestBid fareRequestBid) {
		this.fareRequestBid = fareRequestBid;
	}

	public FareRequestBid getFareRequestBid() {
		return fareRequestBid;
	}
}
