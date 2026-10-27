package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.api.request.ShopSaleCancelRequest;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.api.response.ShopItemResponseHolder;
import com.driverspa.model.api.response.ShopItemsResponseHolder;
import com.driverspa.model.api.response.ShopMovesResponseHolder;
import com.driverspa.model.api.response.ShopReportResponseHolder;
import com.driverspa.model.api.response.ShopSaleResponseHolder;
import com.driverspa.model.api.response.ShopSalesResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.otto.ws.AdminShopItemDeleteRequestEvent;
import com.driverspa.util.otto.ws.AdminShopItemDeleteResponseEvent;
import com.driverspa.util.otto.ws.AdminShopItemSaveRequestEvent;
import com.driverspa.util.otto.ws.AdminShopItemSaveResponseEvent;
import com.driverspa.util.otto.ws.AdminShopItemsRequestEvent;
import com.driverspa.util.otto.ws.AdminShopItemsResponseEvent;
import com.driverspa.util.otto.ws.AdminShopMovesRequestEvent;
import com.driverspa.util.otto.ws.AdminShopMovesResponseEvent;
import com.driverspa.util.otto.ws.AdminShopReportRequestEvent;
import com.driverspa.util.otto.ws.AdminShopReportResponseEvent;
import com.driverspa.util.otto.ws.AdminShopSaleCancelRequestEvent;
import com.driverspa.util.otto.ws.AdminShopSaleCancelResponseEvent;
import com.driverspa.util.otto.ws.AdminShopSaleCreateRequestEvent;
import com.driverspa.util.otto.ws.AdminShopSaleCreateResponseEvent;
import com.driverspa.util.otto.ws.AdminShopSaleRequestEvent;
import com.driverspa.util.otto.ws.AdminShopSaleResponseEvent;
import com.driverspa.util.otto.ws.AdminShopSalesRequestEvent;
import com.driverspa.util.otto.ws.AdminShopSalesResponseEvent;
import com.driverspa.util.otto.ws.AdminShopStockRequestEvent;
import com.driverspa.util.otto.ws.AdminShopStockResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The shop (docs/shop.md): catalog, purchases and write-offs, sales and the report. Each
 * request event is answered by its response event, with a null result on a connection error
 * (the error itself is shown by BA through ApiErrorEvent).
 */
public class ShopAssist extends BaseAssist {

	/** Moves shown in an item's history. */
	public static final int MOVES_LIMIT = 50;

	public ShopAssist(DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
	}

	@Subscribe
	public void onItemsRequested(AdminShopItemsRequestEvent event) {
		getApi().shopItems(event.getWasherId(), new Callback<ShopItemsResponseHolder>() {
			@Override
			public void success(ShopItemsResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopItemsResponseEvent(data));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopItemsResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onItemSaveRequested(AdminShopItemSaveRequestEvent event) {
		Callback<ShopItemResponseHolder> callback = new Callback<ShopItemResponseHolder>() {
			@Override
			public void success(ShopItemResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopItemSaveResponseEvent(data));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopItemSaveResponseEvent(null));
				onRetrofitError(error);
			}
		};
		if (event.getItemId() == null) getApi().shopItemCreate(event.getRequest(), callback);
		else getApi().shopItemUpdate(event.getItemId(), event.getRequest(), callback);
	}

	@Subscribe
	public void onItemDeleteRequested(AdminShopItemDeleteRequestEvent event) {
		getApi().shopItemDelete(event.getItemId(), new Callback<BaseResponseHolder>() {
			@Override
			public void success(BaseResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopItemDeleteResponseEvent(data));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopItemDeleteResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onStockRequested(final AdminShopStockRequestEvent event) {
		Callback<ShopItemResponseHolder> callback = new Callback<ShopItemResponseHolder>() {
			@Override
			public void success(ShopItemResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopStockResponseEvent(event.getPurchase(), data));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopStockResponseEvent(event.getPurchase(), null));
				onRetrofitError(error);
			}
		};
		if (event.getPurchase()) getApi().shopItemPurchase(event.getItemId(), event.getRequest(), callback);
		else getApi().shopItemWriteoff(event.getItemId(), event.getRequest(), callback);
	}

	@Subscribe
	public void onMovesRequested(final AdminShopMovesRequestEvent event) {
		getApi().shopItemMoves(event.getItemId(), MOVES_LIMIT, event.getOffset(), new Callback<ShopMovesResponseHolder>() {
			@Override
			public void success(ShopMovesResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopMovesResponseEvent(event.getItemId(), data).setOffset(event.getOffset()));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopMovesResponseEvent(event.getItemId(), null).setOffset(event.getOffset()));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onSaleCreateRequested(AdminShopSaleCreateRequestEvent event) {
		getApi().shopSaleCreate(event.getRequest(), new Callback<ShopSaleResponseHolder>() {
			@Override
			public void success(ShopSaleResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopSaleCreateResponseEvent(data));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopSaleCreateResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onSalesRequested(final AdminShopSalesRequestEvent event) {
		getApi().shopSales(event.getWasherId(), event.getDay(), event.getDate(), new Callback<ShopSalesResponseHolder>() {
			@Override
			public void success(ShopSalesResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopSalesResponseEvent(data).setSeq(event.getSeq()));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopSalesResponseEvent(null).setSeq(event.getSeq()));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onSaleRequested(final AdminShopSaleRequestEvent event) {
		getApi().shopSale(event.getSaleId(), new Callback<ShopSaleResponseHolder>() {
			@Override
			public void success(ShopSaleResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopSaleResponseEvent(event.getSaleId(), data));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopSaleResponseEvent(event.getSaleId(), null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onSaleCancelRequested(AdminShopSaleCancelRequestEvent event) {
		getApi().shopSaleCancel(event.getSaleId(), new ShopSaleCancelRequest(event.getReason()),
				new Callback<ShopSaleResponseHolder>() {
			@Override
			public void success(ShopSaleResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopSaleCancelResponseEvent(data));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopSaleCancelResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onReportRequested(final AdminShopReportRequestEvent event) {
		getApi().shopReport(event.getWasherId(), event.getFrom(), event.getTo(), new Callback<ShopReportResponseHolder>() {
			@Override
			public void success(ShopReportResponseHolder data, Response response) {
				BA.getEventBus().post(new AdminShopReportResponseEvent(data).setSeq(event.getSeq()));
			}

			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminShopReportResponseEvent(null).setSeq(event.getSeq()));
				onRetrofitError(error);
			}
		});
	}
}
