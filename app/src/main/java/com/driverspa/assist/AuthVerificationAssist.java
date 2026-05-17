package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.stream.IntStream;

import com.driverspa.BA;
import com.driverspa.Reference;
import com.driverspa.model.BookingType;
import com.driverspa.model.Box;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.WasherList;
import com.driverspa.model.WasherPublic;
import com.driverspa.model.api.request.InitialWasherCreateRequest;
import com.driverspa.model.api.response.AuthAdminVerificationResponseHolder;
import com.driverspa.model.api.response.AuthClientVerificationResponseHolder;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.api.response.WasherResponseHolder;
import com.driverspa.model.api.response.WashersResponseHolder;
import com.driverspa.util.Constants;
import com.driverspa.util.L;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.DummyWashersRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;
import com.driverspa.util.otto.ws.AuthClientVerificationRequestEvent;
import com.driverspa.util.otto.ws.AuthClientVerificationResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AuthVerificationAssist extends BaseAssist {
	
	public AuthVerificationAssist(DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
	}

	private ArrayList<Box> createDefaultBoxSettings() {
		return IntStream.range(0, 1)
				.mapToObj(i -> {
					Box boxSetting = new Box();
					boxSetting.setBoxName("Бокс " + (i + 1));
					boxSetting.setBookingType(BookingType.Online);
					return boxSetting;
				})
				.collect(java.util.stream.Collectors.toCollection(ArrayList::new));
	}

	@Subscribe
	public void onAuthRegisterRequested(AuthClientVerificationRequestEvent event) {
		
		logD("onAuthRegisterRequested");		
		getApi().authClientVerify(event.getAuthVerifyRequest(), new Callback<AuthClientVerificationResponseHolder>() {
			
			@Override
			public void success(AuthClientVerificationResponseHolder data, Response response) {
				if( isSuccess(data) ) {
					logD("onAuthRegisterRequested: success");							
					String userId = data.getResponse().getId();
					String userPhone = data.getResponse().getPhone();	
					String userToken = data.getResponse().getToken();
					boolean isAdmin = data.getResponse().isCompany();
					
//					if(isAdmin){
					if(1!=1){
					  displayToast("This user is registered as admin before");
					  getEventsBus().post(new AuthClientVerificationResponseEvent(null));
					}
//					else{
//						writeSelfToDb(data.getResponse());
//						UserPreferences.onUserLogin(BA.getContext(), userId, userPhone, userToken);
//						getEventsBus().post(new AuthClientVerificationResponseEvent(data));
//					}
				} else {
					getEventsBus().post(new AuthClientVerificationResponseEvent(data));
					logD("onAuthRegisterRequested: error " + data.getStatus());
					onDataError(data);
				}
			}
			
			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new AuthClientVerificationResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	
	@Subscribe
	public void onAuthVerificationRequested(final AuthAdminVerificationRequestEvent event) {
		logD("onAuthRegisterRequested");
		getApi().authAdminVerify(event.getAuthVerifyRequest(), new Callback<AuthAdminVerificationResponseHolder>() {
			@Override
			public void success(AuthAdminVerificationResponseHolder verifyData, Response response) {
				if( isSuccess(verifyData) ) {
					logD("onAuthRegisterRequested: success");
					final User user = verifyData.getResponse();
					String userId = user.getId();
					String userPhone = user.getPhone();
					String userToken = user.getToken();
					boolean isAdmin = user.isCompany();
					boolean isOwner = user.isOwner();

//					if(!isAdmin){
					if(1!=1){
						displayToast("This user is registered as client before");
//						ToastUtil.displayAtTop(BA.getContext(), "Вы клиент!!! Не можете зайти как мойка!!!");
						getEventsBus().post(new AuthAdminVerificationResponseEvent(null));
					}
					else{
						UserPreferences.onAdminLogin(BA.getContext(), userId, userPhone, userToken);
						// write down user id and token
						writeSelfToDb(verifyData.getResponse());
						//First request washer list
						getApi().getOwnWashers(
								null,
								new Callback<WashersResponseHolder>() {
									@Override
									public void success(WashersResponseHolder data, Response response) {
									 if(isSuccess(data)){

										if (data.getResponse().getResult() != null && data.getResponse().getResult().size() > 0) {
											UserPreferences.putOwnerWasherListData(BA.getContext(),new WasherList(data.getResponse().getResult()).serialize());
											String washerId = "";
											if(user.isOwner()) {
												WasherPublic washerPublic = data.getResponse().getResult().get(0);
												washerId = washerPublic.getId();
											}
											else{
												washerId = user.getDefaultCarwashId();
											}
											//Request all data of the washer
											getApi().adminWasherInfo(washerId,
													new Callback<WasherResponseHolder>() {
														@Override
														public void success(WasherResponseHolder data, Response response) {
														  if(isSuccess(data)) {
															  Washer washer = data.getResponse().getWasher();
															  //Create washer data preference
															  UserPreferences.putWasherData(BA.getContext(), washer.serialize());
															  UserPreferences.putCity(BA.getContext(),washer.getCity());
															  //Set singleton washer
															  BA.getEventBus().post(new WasherGetResponseEvent(washer));
															  //Response to the UI
															  getEventsBus().post(new AuthAdminVerificationResponseEvent((BaseResponseHolder) data, Constants.APPROVED.equals(washer.getStatus())));
														  }
															else{
															  getEventsBus().post(new AuthAdminVerificationResponseEvent(null));
															  displayToast(data.getMessage());
															  UserPreferences.onAdminLogout(BA.getContext());
															  removeAllDbData();
														  }
															return;
														}

														@Override
														public void failure(RetrofitError error) {
															UserPreferences.onAdminLogout(BA.getContext());
															removeAllDbData();
															onRetrofitError(error);
															getEventsBus().post(new AuthAdminVerificationResponseEvent(null));
														}
													});

										} else {

											//Create washer and redirect to wizard page
											InitialWasherCreateRequest request = new InitialWasherCreateRequest();
											double LAT = 43.240008;
											double LON = 76.912231;
											ArrayList<Double> lonLat = new ArrayList<Double>();
											try {
												request.setCity(UserPreferences.getCity(BA.getContext()));
												Reference.City selectedCity = BA.getReference().getCities().stream()
														.filter(city -> city.getCode().equals(request.getCity()))
														.findFirst()
														.orElse(null);
												if (selectedCity != null) {
													LAT = Double.parseDouble(selectedCity.getLonLat().get(1));
													LON = Double.parseDouble(selectedCity.getLonLat().get(0));
												}
											} catch (Exception e) {
												request.setCity("almaty");
											}

											lonLat.add(LON);
											lonLat.add(LAT);
											request.setLonLat(lonLat);
											request.setName(user.getCompanyName());
											request.setBoxSettings(createDefaultBoxSettings());

											//if there is no washer for this user, create it
											getApi().createInitialWasher(
													request,
													new Callback<WasherResponseHolder>() {

														@Override
														public void success(WasherResponseHolder data, Response response) {
														 if(isSuccess(data)) {
															 Washer washer = data.getResponse().getWasher();
															 UserPreferences.putWasherData(BA.getContext(), washer.serialize());
															 BA.getEventBus().post(new WasherGetResponseEvent(washer));
															 //Response to the UI
															 getEventsBus().post(new AuthAdminVerificationResponseEvent((BaseResponseHolder) data, Constants.APPROVED.equals(washer.getStatus())));
														 }else{
															getEventsBus().post(new AuthAdminVerificationResponseEvent(null));
															UserPreferences.onAdminLogout(BA.getContext());
															removeAllDbData();
														    displayToast(data.getMessage());
														 }
															return;
														}

														@Override
														public void failure(RetrofitError error) {
															UserPreferences.onAdminLogout(BA.getContext());
															removeAllDbData();
															onRetrofitError(error);
															getEventsBus().post(new AuthAdminVerificationResponseEvent(null));
															return;
														}
													});


										}
									 }
										else{
										 UserPreferences.onAdminLogout(BA.getContext());
										 removeAllDbData();
										 displayToast(data.getMessage());
										 getEventsBus().post(new AuthAdminVerificationResponseEvent(null));
									 }
									}

									@Override
									public void failure(RetrofitError error) {
										UserPreferences.onAdminLogout(BA.getContext());
										removeAllDbData();
										onRetrofitError(error);
										getEventsBus().post(new AuthAdminVerificationResponseEvent(null));
										return;
									}
								});
//						getEventsBus().post(new AuthAdminVerificationResponseEvent(data));
					}					

				} else {
					getEventsBus().post(new AuthAdminVerificationResponseEvent(verifyData));
					logD("onAuthRegisterRequested: error " + verifyData.getStatus());
					onDataError(verifyData);
				}
			}
			
			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new AuthAdminVerificationResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void createDummyWashersRequested(DummyWashersRequestEvent event){
		event.getRequestList().forEach(request -> {
			//if there is no washer for this user, create it
			getApi().createTmpWasher(
					request,
					new Callback<WasherResponseHolder>() {

						@Override
						public void success(WasherResponseHolder data, Response response) {
							L.d("Success for " + request.getName());
							return;
						}

						@Override
						public void failure(RetrofitError error) {
							return;
						}
					});

			try {
				Thread.sleep(500);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		});
	}
}
