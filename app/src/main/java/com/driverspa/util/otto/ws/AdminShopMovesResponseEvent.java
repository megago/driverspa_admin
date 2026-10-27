package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.ShopMovesResponseHolder;

public class AdminShopMovesResponseEvent {

	private final String itemId;
	private final ShopMovesResponseHolder result;

	public AdminShopMovesResponseEvent(String itemId, ShopMovesResponseHolder result) {
		this.itemId = itemId;
		this.result = result;
	}

	public String getItemId() {
		return itemId;
	}

	public ShopMovesResponseHolder getResult() {
		return result;
	}

	/** The first move to load (paging), echoed in the response. */
	private int offset;

	public int getOffset() {
		return offset;
	}

	public AdminShopMovesResponseEvent setOffset(int offset) {
		this.offset = offset;
		return this;
	}
}
