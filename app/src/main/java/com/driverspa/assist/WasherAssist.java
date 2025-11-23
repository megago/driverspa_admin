package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Locale;

import com.driverspa.BA;
import com.driverspa.model.WasherList;
import com.driverspa.model.WasherPublic;
import com.driverspa.model.api.request.AddWorkshiftRequest;
import com.driverspa.model.api.request.CampaignOperationRequest;
import com.driverspa.model.api.request.CloseWorkshiftRequest;
import com.driverspa.model.api.request.GroupMenuOperationRequest;
import com.driverspa.model.api.request.StaffOperationRequest;
import com.driverspa.model.api.request.WashCountCampaignOperationRequest;
import com.driverspa.model.api.request.WasherRequest;
import com.driverspa.model.api.response.AdminAnalyticsResponseHolder;
import com.driverspa.model.api.response.AdminCampaignResponseHolder;
import com.driverspa.model.api.response.AdminCampaignsResponseHolder;
import com.driverspa.model.api.response.AdminCarwashTransactionResponseHolder;
import com.driverspa.model.api.response.AdminCarwashTransactionsResponseHolder;
import com.driverspa.model.api.response.AdminCashFinancialResponseHolder;
import com.driverspa.model.api.response.AdminFinancialStatisticResponseHolder;
import com.driverspa.model.api.response.AdminGroupMenuResponseHolder;
import com.driverspa.model.api.response.AdminStaffFinancialResponseHolder;
import com.driverspa.model.api.response.AdminStaffResponseHolder;
import com.driverspa.model.api.response.AdminStaffsResponseHolder;
import com.driverspa.model.api.response.AdminWashCountCampaignResponseHolder;
import com.driverspa.model.api.response.AdminWashCountCampaignsResponseHolder;
import com.driverspa.model.api.response.AdminWorkshiftResponseHolder;
import com.driverspa.model.api.response.AdminWorkshiftsResponseHolder;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.api.response.NotificationNewsResponseHolder;
import com.driverspa.model.api.response.WasherResponseHolder;
import com.driverspa.model.api.response.WasherTimeTableResponseHolder;
import com.driverspa.model.api.response.WashersResponseHolder;
import com.driverspa.util.RetrofitClient.WashmeApi;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.LocalWasherChangeResponseEvent;
import com.driverspa.util.otto.ws.*;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WasherAssist extends BaseAssist {

    public WasherAssist(WashmeApi api, Bus eventBus) {
        super(api, eventBus);
    }

    @Subscribe
    public void onWashersFavouriteRequested(WasherFavouriteRequestEvent event) {
        getApi().washersSearchFavourite(
                event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
                new Callback<WashersResponseHolder>() {
                    @Override
                    public void success(WashersResponseHolder data, Response response) {
                        if (isSuccess(data)) {
                            logD("onWashersFavouriteRequested: success " + response.getUrl());
                            BA.getEventBus().post(new WasherFavouriteResponseEvent(data.getResponse().getResult()));
                        } else {
                            BA.getEventBus().post(new WasherFavouriteResponseEvent(null));
                            //logE("onWashersFavouriteRequested: error");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new WasherFavouriteResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onWashersAvailableRequested(WasherAvailableRequestEvent event) {
        getApi().washersSearchAvailable(
                event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
                new Callback<WashersResponseHolder>() {
                    @Override
                    public void success(WashersResponseHolder data, Response response) {
                        if (isSuccess(data)) {
                            logD("onWashersAvailableRequested: success " + response.getUrl());
                            BA.getEventBus().post(new WasherAvailableResponseEvent(data.getResponse().getResult()));
                        } else {
                            BA.getEventBus().post(new WasherAvailableResponseEvent(null));
                            //logE("onWashersAvailableRequested: error");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new WasherAvailableResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onWashersPromoRequested(WasherPromoRequestEvent event) {
        getApi().washersSearchPromo(
                event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
                new Callback<WashersResponseHolder>() {
                    @Override
                    public void success(WashersResponseHolder data, Response response) {
                        if (isSuccess(data)) {
                            logD("onWashersPromoRequested: success " + response.getUrl());
                            BA.getEventBus().post(new WasherPromoResponseEvent(data.getResponse().getResult()));
                        } else {
                            //logE("onWashersPromoRequested: error");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new WasherPromoResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onWashersNearRequested(WashersNearRequestEvent event) {
        getApi().washersSearchNearBy(
                event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
                new Callback<WashersResponseHolder>() {
                    @Override
                    public void success(WashersResponseHolder data, Response response) {
                        BA.getEventBus().post(new WashersNearResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new WashersNearResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onWashersNearMapRequested(WashersNearMapRequestEvent event) {
        String location = String.format(Locale.US, "%f,%f", event.getLongitude(), event.getLatitide());
        getApi().washersSearchNearBy(event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
                new Callback<WashersResponseHolder>() {
                    @Override
                    public void success(WashersResponseHolder data, Response response) {
                        BA.getEventBus().post(new WashersNearMapResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new WashersNearMapResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onWasherInfoRequested(WasherInfoRequestEvent event) {

        getApi().washerInfo(event.getWasherId(),
                new Callback<WasherResponseHolder>() {
                    @Override
                    public void success(WasherResponseHolder data, Response response) {
                        if (isSuccess(data)) {
                            logD("onWasherFavouriteRequested: success " + response.getUrl());
                            BA.getEventBus().post(new WasherInfoResponseEvent(data.getResponse().getWasher()));
                        } else {
                            //logE("onWasherFavouriteRequested: error: " + data.getResponse().getMessage());
                            BA.getEventBus().post(new WasherInfoResponseEvent(null));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new WasherInfoResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onWasherTimeTableRequested(WasherTimeTableRequestEvent event) {
        getApi().washerTimeTable(event.getWasherId(),
                event.getDay(),
                new Callback<WasherTimeTableResponseHolder>() {
                    @Override
                    public void success(WasherTimeTableResponseHolder data, Response response) {
                        if (isSuccess(data)) {
                            logD("onWasherTimeTableRequested: success " + response.getUrl());
                            BA.getEventBus().post(new WasherTimeTableResponseEvent(data.getResponse()));
                        } else {
                            BA.getEventBus().post(new WasherTimeTableResponseEvent(null));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new WasherTimeTableResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onWasherTimeTableBookRequested(WasherTimeTableBookRequestEvent event) {
        getApi().washerTimeTableWithBook(event.getWasherId(), event.getDay(), event.getDate(),
                new Callback<WasherTimeTableResponseHolder>() {
                    @Override
                    public void success(WasherTimeTableResponseHolder data, Response response) {
                        if (isSuccess(data)) {
                            logD("onWasherTimeTableRequested: success " + response.getUrl());
                            BA.getEventBus().post(new WasherTimeTableBookResponseEvent(data.getResponse()));
                        } else {
                            BA.getEventBus().post(new WasherTimeTableBookResponseEvent(null));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new WasherTimeTableBookResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onWasherFavouriteRequested(FavouriteWasherRequestEvent event) {

        getApi().addToFavourite(new WasherRequest(event.getWasherId()),
                new Callback<WasherResponseHolder>() {
                    @Override
                    public void success(WasherResponseHolder data, Response response) {
                        if (isSuccess(data)) {
                            logD("onWasherFavouriteRequested: success " + response.getUrl());
                            BA.getEventBus().post(new FavouriteWasherResponseEvent(true));
                        } else {
                            BA.getEventBus().post(new FavouriteWasherResponseEvent(false));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new FavouriteWasherResponseEvent(false));
                    }
                });
    }

    @Subscribe
    public void onWasherUnfavouriteRequested(UnfavouriteWasherRequestEvent event) {

        getApi().removeFromFavourite(event.getWasherId(),
                new Callback<BaseResponseHolder>() {
                    @Override
                    public void success(BaseResponseHolder data, Response response) {
                        if (isSuccess(data)) {
                            logD("onWasherUnfavouriteRequested: success " + response.getUrl());
                            BA.getEventBus().post(new UnfavouriteWasherResponseEvent(true));
                        } else {
                            BA.getEventBus().post(new UnfavouriteWasherResponseEvent(false));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new UnfavouriteWasherResponseEvent(false));
                    }
                });
    }

    @Subscribe
    public void onAdminWasherListRequested(AdminWasherListRequestEvent event) {
        getApi().getOwnWashers(
                event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
                new Callback<WashersResponseHolder>() {
                    @Override
                    public void success(WashersResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWasherListResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new AdminWasherListResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onInitialWasherCreateRequested(InitialWasherCreateRequestEvent event) {
        getApi().createInitialWasher(
                event.getRequest(),
                new Callback<WasherResponseHolder>() {

                    @Override
                    public void success(WasherResponseHolder data, Response response) {
                        BA.getEventBus().post(new InitialWasherCreateResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new InitialWasherCreateResponseEvent(null));
                    }
                });
    }


    @Subscribe
    public void onAdminWasherInfoRequested(AdminWasherInfoRequestEvent event) {

        getApi().adminWasherInfo(event.getWasherId(), new Callback<WasherResponseHolder>() {

            @Override
            public void success(WasherResponseHolder data, Response response) {
                logD("onWasherFavouriteRequested: success " + response.getUrl());
                //Set singleton washer
                UserPreferences.putWasherData(BA.getContext(), data.getResponse().getWasher().serialize());
                BA.getEventBus().post(new WasherLocalUpdateRequestEvent(data.getResponse().getWasher()));
                BA.getEventBus().post(new AdminWasherInfoResponseEvent(data));
                BA.getEventBus().post(new LocalWasherChangeResponseEvent());

            }

            @Override
            public void failure(RetrofitError error) {
                onRetrofitError(error);
                BA.getEventBus().post(new LocalWasherChangeResponseEvent());
                BA.getEventBus().post(new AdminWasherInfoResponseEvent(null));
            }
        });
    }

    @Subscribe
    public void editWasherRequested(final EditWasherRequestEvent event) {
        getApi().editWasher(
                event.getWasherId(),
                event.getRequest(),
                new Callback<WasherResponseHolder>() {
                    @Override
                    public void success(WasherResponseHolder data, Response response) {
                        BA.getEventBus().post(new EditWasherResponseEvent(data));
                        BA.getEventBus().post(new AdminWasherInfoRequestEvent(event.getWasherId()));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new EditWasherResponseEvent(null));
                    }
                });
    }


    @Subscribe
    public void onGroupMenuListRequested(AdminGroupMenuListRequestEvent event) {

        //TODO temproary solution
        getApi().adminWasherInfo(event.getWasherId(), new Callback<WasherResponseHolder>() {
            @Override
            public void success(WasherResponseHolder data, Response response) {
                logD("onWasherFavouriteRequested: success " + response.getUrl());
                BA.getEventBus().post(new AdminGroupMenuListResponseEvent(data));
            }

            @Override
            public void failure(RetrofitError error) {
                onRetrofitError(error);
                BA.getEventBus().post(new AdminGroupMenuListResponseEvent(null));
            }
        });

//		getApi().adminGroupMenuList(event.getWasherId(),
//				new Callback<AdminGroupMenuListResponseHolder>() {
//					@Override
//					public void success(AdminGroupMenuListResponseHolder data, Response response) {
//						BA.getEventBus().post(new AdminGroupMenuListResponseEvent(data));
//					}
//
//					@Override
//					public void failure(RetrofitError error) {
//						BA.getEventBus().post(new AdminGroupMenuListResponseEvent(null));
//						onRetrofitError(error);
//					}
//				});
    }

    @Subscribe
    public void onGroupMenuRequested(AdminGroupMenuRequestEvent event) {
        getApi().adminGroupMenu(event.getWasherId(),
                event.getId(),
                new Callback<AdminGroupMenuResponseHolder>() {
                    @Override
                    public void success(AdminGroupMenuResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminGroupMenuResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminGroupMenuResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onGroupMenuChangeRequested(AdminGroupMenuChangeRequestEvent event) {
        getApi().adminChangeGroupMenu(event.getWasherId(),
                event.getId(),
                new GroupMenuOperationRequest(event.getName(), event.getServices(), event.getPrices()),
                new Callback<AdminGroupMenuResponseHolder>() {
                    @Override
                    public void success(AdminGroupMenuResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminGroupMenuChangeResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminGroupMenuChangeResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onGroupMenuAddRequested(AdminGroupMenuAddRequestEvent event) {
        getApi().adminAddGroupMenu(event.getWasherId(),
                new GroupMenuOperationRequest(event.getName(), event.getServices(), event.getPrices()),
                new Callback<AdminGroupMenuResponseHolder>() {

                    @Override
                    public void success(AdminGroupMenuResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminGroupMenuAddResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminGroupMenuAddResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onGroupMenuDeleteRequested(AdminGroupMenuDeleteRequestEvent event) {
        getApi().adminDeleteGroupMenu(event.getWasherId(),
                event.getId(),
                new Callback<AdminGroupMenuResponseHolder>() {
                    @Override
                    public void success(AdminGroupMenuResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminGroupMenuDeleteResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminGroupMenuDeleteResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    //Campaign starts
    @Subscribe
    public void onCampaignListRequested(AdminCampaignListRequestEvent event) {
        getApi().adminCampaignList(event.getWasherId(),
                new Callback<AdminCampaignsResponseHolder>() {
                    @Override
                    public void success(AdminCampaignsResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminCampaignListResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminCampaignListResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onCampaignRequested(AdminCampaignRequestEvent event) {
        getApi().adminCampaign(event.getWasherId(),
                event.getCampaignId(),
                new Callback<AdminCampaignResponseHolder>() {
                    @Override
                    public void success(AdminCampaignResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminCampaignResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminCampaignResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onCampaignTypeChangeRequested(AdminCampaignChangeRequestEvent event) {
        getApi().adminChangeCampaign(event.getWasherId(),
                event.getCampaignId(),
                new CampaignOperationRequest(event.getDescription(), event.getCampaignType(), event.getEndDate(), event.getCampaignDiscount()),
                new Callback<AdminCampaignResponseHolder>() {
                    @Override
                    public void success(AdminCampaignResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminCampaignChangeResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminCampaignChangeResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onCampaignAddRequested(AdminCampaignAddRequestEvent event) {
        getApi().adminAddCampaign(event.getWasherId(),
                new CampaignOperationRequest(event.getDescription(), event.getCampaignType(), event.getEndTime(), event.getCampaignDiscount()),
                new Callback<BaseResponseHolder>() {

                    @Override
                    public void success(BaseResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminCampaignAddResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminCampaignAddResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onCampaignDeleteRequested(AdminCampaignDeleteRequestEvent event) {
        getApi().adminDeleteCampaign(event.getWasherId(),
                event.getCampaignId(),
                new Callback<AdminCampaignResponseHolder>() {
                    @Override
                    public void success(AdminCampaignResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminCampaignDeleteResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminCampaignDeleteResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }


    //Campaign wash count starts
    @Subscribe
    public void onWashCountCampaignListRequested(AdminWashCountCampaignListRequestEvent event) {
        getApi().adminWashCountCampaignList(event.getWasherId(),
                new Callback<AdminWashCountCampaignsResponseHolder>() {
                    @Override
                    public void success(AdminWashCountCampaignsResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWashCountCampaignListResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminWashCountCampaignListResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onWashCountCampaignRequested(AdminWashCountCampaignRequestEvent event) {
        getApi().adminWashCountCampaign(event.getWasherId(),
                event.getCampaignId(),
                new Callback<AdminWashCountCampaignResponseHolder>() {
                    @Override
                    public void success(AdminWashCountCampaignResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWashCountCampaignResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminWashCountCampaignResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onWashCountCampaignTypeChangeRequested(AdminWashCountCampaignChangeRequestEvent event) {
        getApi().adminWashCountChangeCampaign(event.getWasherId(),
                event.getCampaignId(),
                new CampaignOperationRequest(event.getDescription(), event.getCampaignType(), event.getEndDate(), event.getCampaignDiscount()),
                new Callback<AdminWashCountCampaignResponseHolder>() {
                    @Override
                    public void success(AdminWashCountCampaignResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWashCountCampaignChangeResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminWashCountCampaignChangeResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onWashCountCampaignAddRequested(AdminWashCountCampaignAddRequestEvent event) {
        getApi().adminWashCountAddCampaign(event.getWasherId(),
                new WashCountCampaignOperationRequest(event.getDescription(), event.getWashCount()),
                new Callback<BaseResponseHolder>() {

                    @Override
                    public void success(BaseResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWashCountCampaignAddResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminWashCountCampaignAddResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onWashCountCampaignDeleteRequested(AdminWashCountCampaignDeleteRequestEvent event) {
        getApi().adminWashCountDeleteCampaign(event.getWasherId(),
                event.getCampaignId(),
                new Callback<AdminWashCountCampaignResponseHolder>() {
                    @Override
                    public void success(AdminWashCountCampaignResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWashCountCampaignDeleteResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminWashCountCampaignDeleteResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onNotificaitonNewsListRequested(NotificationNewsListRequestEvent event) {
        getApi().getAdminNotificationNews(
                event.getSearchFilter() == null ? null : event.getSearchFilter().toMap(),
                new Callback<NotificationNewsResponseHolder>() {
                    @Override
                    public void success(NotificationNewsResponseHolder data, Response response) {
                        BA.getEventBus().post(new NotificationNewsListResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new NotificationNewsListResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onAddNotificaitonRequested(AddNotificationNewsRequestEvent event) {
        getApi().addAdminNotificationNews(event.getRequest(),
                new Callback<BaseResponseHolder>() {
                    @Override
                    public void success(BaseResponseHolder data, Response response) {
                        BA.getEventBus().post(new AddNotificationNewsResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                        BA.getEventBus().post(new AddNotificationNewsResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onSendClientNotificaitonRequested(SendClientPushNotificationNewsRequestEvent event) {
        getApi().sendAdminNotificationNews(event.getRequest(),
                new Callback<BaseResponseHolder>() {
                    @Override
                    public void success(BaseResponseHolder data, Response response) {
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onRetrofitError(error);
                    }
                });
    }


    @Subscribe
    public void onWasherListRequested(OwnerWasherListGetRequestEvent event) {
        getApi().getOwnWashers(
                null,
                new Callback<WashersResponseHolder>() {
                    @Override
                    public void success(WashersResponseHolder data, Response response) {
                        if (isSuccess(data)) {
                            List<WasherPublic> washerList = data.getResponse().getResult();
                            UserPreferences.putOwnerWasherListData(BA.getContext(), (new WasherList(washerList)).serialize());
                            BA.getEventBus().post(new OwnerWasherListGetResponseEvent(washerList));
                        } else {
                            BA.getEventBus().post(new OwnerWasherListGetResponseEvent(null));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new OwnerWasherListGetResponseEvent(null));
                        return;
                    }
                });
    }

    @Subscribe
    public void onWasherCreateRequested(NewCarwashCreateRequestEvent event) {
        getApi().createInitialWasher(
                event.getRequest(),
                new Callback<WasherResponseHolder>() {

                    @Override
                    public void success(WasherResponseHolder data, Response response) {
                        BA.getEventBus().post(new NewCarwashCreateResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new NewCarwashCreateResponseEvent(null));
                    }
                });
    }


    @Subscribe
    public void onClientCreateRequested(ClientCreateRequestEvent event) {
        getApi().createCompanyClient(
                event.getRequest(),
                new Callback<BaseResponseHolder>() {

                    @Override
                    public void success(BaseResponseHolder data, Response response) {
                        BA.getEventBus().post(new ClientCreateResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new ClientCreateResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onClientEditRequested(ClientEditRequestEvent event) {
        getApi().editCompanyClient(
                event.getRequest().getId(),
                event.getRequest(),
                new Callback<BaseResponseHolder>() {

                    @Override
                    public void success(BaseResponseHolder data, Response response) {
                        BA.getEventBus().post(new ClientEditResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new ClientEditResponseEvent(null));
                    }
                });
    }

    @Subscribe
    public void onClientDeleteRequested(ClientRemoveRequestEvent event) {
        getApi().deleteCompanyClient(
                event.getId(),
                new Callback<BaseResponseHolder>() {

                    @Override
                    public void success(BaseResponseHolder data, Response response) {
                        BA.getEventBus().post(new ClientRemoveResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new ClientRemoveResponseEvent(null));
                    }
                });
    }


    @Subscribe
    public void onStaffListRequested(AdminStaffListRequestEvent event) {
        getApi().adminStaffList(event.getWasherId(),
                new Callback<AdminStaffsResponseHolder>() {
                    @Override
                    public void success(AdminStaffsResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminStaffListResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminStaffListResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onStaffRequested(AdminStaffRequestEvent event) {
        getApi().adminStaff(event.getWasherId(),
                event.getId(),
                new Callback<AdminStaffResponseHolder>() {
                    @Override
                    public void success(AdminStaffResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminStaffResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminStaffResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onStaffChangeRequested(AdminStaffChangeRequestEvent event) {
        getApi().adminChangeStaff(event.getWasherId(),
                event.getUid(),
                new StaffOperationRequest(event.getFirstName(), event.getLastName(), event.getMobile(), event.getRole(), event.getStatus(), event.getSalaryPercentage()),
                new Callback<AdminStaffResponseHolder>() {
                    @Override
                    public void success(AdminStaffResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminStaffChangeResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminStaffChangeResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onStaffAddRequested(AdminStaffAddRequestEvent event) {
        getApi().adminAddStaff(event.getWasherId(),
                new StaffOperationRequest(event.getFirstName(), event.getLastName(), event.getMobile(), event.getRole(), event.getStatus(), event.getSalaryPercentage()),
                new Callback<AdminStaffResponseHolder>() {

                    @Override
                    public void success(AdminStaffResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminStaffAddResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminStaffAddResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onStaffDeleteRequested(AdminStaffDeleteRequestEvent event) {
        getApi().adminDeleteStaff(event.getWasherId(),
                event.getId(),
                new Callback<AdminStaffResponseHolder>() {
                    @Override
                    public void success(AdminStaffResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminStaffDeleteResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminStaffDeleteResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }


    @Subscribe
    public void onWorkshiftAddRequested(AdminWorkshiftAddRequestEvent event) {
        getApi().adminAddWorkshift(new AddWorkshiftRequest(event.getCarwash()),
                new Callback<AdminWorkshiftResponseHolder>() {

                    @Override
                    public void success(AdminWorkshiftResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWorkshiftAddResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminWorkshiftAddResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onWorkshiftRequested(AdminWorkshiftRequestEvent event) {
        getApi().adminWorkshift(event.getId(),
                new Callback<AdminWorkshiftResponseHolder>() {
                    @Override
                    public void success(AdminWorkshiftResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWorkshiftResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminWorkshiftResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }


    @Subscribe
    public void onWorkshiftsRequested(AdminWorkshiftsRequestEvent event) {
        getApi().adminWorkshiftList(event.getWasherId(),
                new Callback<AdminWorkshiftsResponseHolder>() {
                    @Override
                    public void success(AdminWorkshiftsResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWorkshiftsResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminWorkshiftsResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }


    @Subscribe
    public void onWorkshiftCloseRequested(AdminWorkshiftCloseRequestEvent event) {
        getApi().adminWorkshiftClose(new CloseWorkshiftRequest(event.getId()),
                new Callback<AdminWorkshiftResponseHolder>() {
                    @Override
                    public void success(AdminWorkshiftResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminWorkshiftCloseResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminWorkshiftCloseResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }


    @Subscribe
    public void onFinancialStatisticsRequested(AdminFinancialStatisticsRequestEvent event) {
        getApi().adminFinancialStatistics(event.getCarwash(),
                event.getStartDate(),
                new Callback<AdminFinancialStatisticResponseHolder>() {
                    @Override
                    public void success(AdminFinancialStatisticResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminFinancialStatisticsResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminFinancialStatisticsResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }


    @Subscribe
    public void onCarwashTransactionsRequested(AdminCarwashTransactionsRequestEvent event) {
        getApi().adminCarwashTransactionList(event.getFilter().toMap(),
                new Callback<AdminCarwashTransactionsResponseHolder>() {
                    @Override
                    public void success(AdminCarwashTransactionsResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminCarwashTransactionsResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminCarwashTransactionsResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }


    @Subscribe
    public void onAdminCarwashTransactionAddRequested(AdminCarwashTransactionAddRequestEvent event) {
        getApi().adminAddCarwashTransaction(event,
                new Callback<AdminCarwashTransactionResponseHolder>() {
                    @Override
                    public void success(AdminCarwashTransactionResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminCarwashTransactionAddResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminCarwashTransactionAddResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onCarwashTransactionRequested(AdminCarwashTransactionRequestEvent event) {
        getApi().adminCarwashTransaction(event.getId(),
                new Callback<AdminCarwashTransactionResponseHolder>() {
                    @Override
                    public void success(AdminCarwashTransactionResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminCarwashTransactionResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminCarwashTransactionResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onStaffFinancialInfoRequested(AdminStaffFinancialInfoRequestEvent event) {
        getApi().adminStaffFinancialInfo(event.getCarwash(),
                event.getStaffId(),
                event.getStartDate(),
                event.getEndDate(),
                new Callback<AdminStaffFinancialResponseHolder>() {
                    @Override
                    public void success(AdminStaffFinancialResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminStaffFinancialInfoResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminStaffFinancialInfoResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void onStaffCashFinancialRequested(AdminCashFinancialRequestEvent event) {
        getApi().adminCashFinancial(event.getCarwash(),
                event.getStartDate(),
                event.getEndDate(),
                new Callback<AdminCashFinancialResponseHolder>() {
                    @Override
                    public void success(AdminCashFinancialResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminCashFinancialResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminCashFinancialResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }


    @Subscribe
    public void onAnalyticsRequested(AdminAnalyticsRequestEvent event) {
        getApi().adminAnalytics(event.getCarwash(),
                event.getStartDate(),
                event.getEndDate(),
                new Callback<AdminAnalyticsResponseHolder>() {
                    @Override
                    public void success(AdminAnalyticsResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminAnalyticsResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminAnalyticsResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

    @Subscribe
    public void AdminRequestMakeBidRequestRequested(AdminRequestMakeBidRequestEvent event) {
        getApi().adminCompanyBid(event.getFareRequestBid(),
                new Callback<BaseResponseHolder>() {
                    @Override
                    public void success(BaseResponseHolder data, Response response) {
                        BA.getEventBus().post(new AdminRequestMakeBidResponseEvent(data));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BA.getEventBus().post(new AdminRequestMakeBidResponseEvent(null));
                        onRetrofitError(error);
                    }
                });
    }

}
