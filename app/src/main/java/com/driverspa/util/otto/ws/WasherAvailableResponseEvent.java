package com.driverspa.util.otto.ws;

import java.util.ArrayList;

import com.driverspa.model.WasherPublic;


public class WasherAvailableResponseEvent extends WasherBaseResponseEvent {

	public WasherAvailableResponseEvent(ArrayList<WasherPublic> washers) {
		super(washers);
	}

}
