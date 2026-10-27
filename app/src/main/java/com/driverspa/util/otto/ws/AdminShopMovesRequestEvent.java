package com.driverspa.util.otto.ws;

/** GET shop_item/<id>/moves */
public class AdminShopMovesRequestEvent {

	private final String itemId;

	public AdminShopMovesRequestEvent(String itemId) {
		this.itemId = itemId;
	}

	public String getItemId() {
		return itemId;
	}

	/** The first move to load (paging), echoed in the response. */
	private int offset;

	public int getOffset() {
		return offset;
	}

	public AdminShopMovesRequestEvent setOffset(int offset) {
		this.offset = offset;
		return this;
	}
}
