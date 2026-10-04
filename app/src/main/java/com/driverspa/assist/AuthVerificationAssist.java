package com.driverspa.assist;
import com.driverspa.R;

import android.text.TextUtils;

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
import com.driverspa.util.otto.ws.AdminBootstrapRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;
import com.driverspa.util.otto.ws.AuthClientVerificationRequestEvent;
import com.driverspa.util.otto.ws.AuthClientVerificationResponseEvent;
import com.driverspa.util.otto.ws.LoginPasswordRequestEvent;
import com.driverspa.util.otto.ws.ResetPasswordRequestEvent;
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
					boxSetting.setBoxName(BA.str(R.string.box_sp) + (i + 1));
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
					if(1!=1){
					  displayToast("This user is registered as admin before");
					  getEventsBus().post(new AuthClientVerificationResponseEvent(null));
					}
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
		logD("onAuthVerificationRequested");
		getApi().authAdminVerify(event.getAuthVerifyRequest(), new Callback<AuthAdminVerificationResponseHolder>() {
			@Override
			public void success(AuthAdminVerificationResponseHolder verifyData, Response response) {
				if( isSuccess(verifyData) ) {
					logD("onAuthVerificationRequested: success");
					final User user = verifyData.getResponse();
					// Sign the user in (store token) so the next call (set_password) is authorized.
					UserPreferences.onAdminLogin(BA.getContext(), user.getId(), user.getPhone(), user.getToken());
					writeSelfToDb(user);

					if (user.isPasswordRequired()) {
						// New password flow: stop here and let the UI collect a password.
						// The carwash bootstrap happens later (after set_password + city).
						AuthAdminVerificationResponseEvent ev = new AuthAdminVerificationResponseEvent(verifyData);
						ev.setPasswordRequired(true);
						getEventsBus().post(ev);
					} else {
						// Legacy OTP-only flow: proceed straight into the app.
						bootstrapAdmin(user);
					}
				} else {
					getEventsBus().post(new AuthAdminVerificationResponseEvent(verifyData));
					logD("onAuthVerificationRequested: error " + verifyData.getStatus());
					onDataError(verifyData);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onLoginPasswordRequested(final LoginPasswordRequestEvent event) {
		logD("onLoginPasswordRequested");
		getApi().loginPassword(event.getRequest(), new Callback<AuthAdminVerificationResponseHolder>() {
			@Override
			public void success(AuthAdminVerificationResponseHolder data, Response response) {
				if (isSuccess(data)) {
					final User user = data.getResponse();
					UserPreferences.onAdminLogin(BA.getContext(), user.getId(), user.getPhone(), user.getToken());
					writeSelfToDb(user);
					bootstrapAdmin(user);
				} else {
					getEventsBus().post(new AuthAdminVerificationResponseEvent(data));
					onDataError(data);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onResetPasswordRequested(final ResetPasswordRequestEvent event) {
		logD("onResetPasswordRequested");
		getApi().resetPassword(event.getRequest(), new Callback<AuthAdminVerificationResponseHolder>() {
			@Override
			public void success(AuthAdminVerificationResponseHolder data, Response response) {
				if (isSuccess(data)) {
					final User user = data.getResponse();
					UserPreferences.onAdminLogin(BA.getContext(), user.getId(), user.getPhone(), user.getToken());
					writeSelfToDb(user);
					bootstrapAdmin(user);
				} else {
					getEventsBus().post(new AuthAdminVerificationResponseEvent(data));
					onDataError(data);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onAdminBootstrapRequested(AdminBootstrapRequestEvent event) {
		logD("onAdminBootstrapRequested");
		User user = UserSelfAssist.getUserFromDb(UserPreferences.getUserId(BA.getContext()));
		if (user == null) {
			getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
			return;
		}
		bootstrapAdmin(user);
	}

	/**
	 * Fetches the signed-in admin's carwash (creating an initial one if none
	 * exists, using the city already stored in preferences) and posts an
	 * {@link AuthAdminVerificationResponseEvent} so the UI can enter the app.
	 */
	private void bootstrapAdmin(final User user) {
		getApi().getOwnWashers(null, new Callback<WashersResponseHolder>() {
			@Override
			public void success(WashersResponseHolder data, Response response) {
				if (isSuccess(data)) {
					if (data.getResponse().getResult() != null && data.getResponse().getResult().size() > 0) {
						UserPreferences.putOwnerWasherListData(BA.getContext(), new WasherList(data.getResponse().getResult()).serialize());
						String washerId;
						if (user.isOwner()) {
							WasherPublic washerPublic = data.getResponse().getResult().get(0);
							washerId = washerPublic.getId();
						} else {
							washerId = user.getDefaultCarwashId();
						}
						getApi().adminWasherInfo(washerId, new Callback<WasherResponseHolder>() {
							@Override
							public void success(WasherResponseHolder data, Response response) {
								if (isSuccess(data)) {
									Washer washer = data.getResponse().getWasher();
									UserPreferences.putWasherData(BA.getContext(), washer.serialize());
									UserPreferences.putCity(BA.getContext(), washer.getCity());
									BA.getEventBus().post(new WasherGetResponseEvent(washer));
									getEventsBus().post(new AuthAdminVerificationResponseEvent((BaseResponseHolder) data, Constants.APPROVED.equals(washer.getStatus())));
								} else {
									getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
									displayToast(data.getMessage());
									UserPreferences.onAdminLogout(BA.getContext());
									removeAllDbData();
								}
							}

							@Override
							public void failure(RetrofitError error) {
								UserPreferences.onAdminLogout(BA.getContext());
								removeAllDbData();
								onRetrofitError(error);
								getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
							}
						});
					} else {
						// No carwash yet — create an initial one using the chosen city.
						String city = UserPreferences.getCity(BA.getContext());
						if (TextUtils.isEmpty(city)) {
							// Signed in via a path that skipped the city step (e.g. the
							// number was registered in the client app first, so it goes
							// straight to password login). Stay signed in and let the UI
							// collect a city, then it re-posts AdminBootstrapRequestEvent.
							getEventsBus().post(buildCityRequiredEvent());
							return;
						}
						InitialWasherCreateRequest request = new InitialWasherCreateRequest();
						double LAT = 43.240008;
						double LON = 76.912231;
						ArrayList<Double> lonLat = new ArrayList<Double>();
						request.setCity(city);
						try {
							Reference.City selectedCity = BA.getReference().getCities().stream()
									.filter(c -> c.getCode().equals(city))
									.findFirst()
									.orElse(null);
							if (selectedCity != null) {
								LAT = Double.parseDouble(selectedCity.getLonLat().get(1));
								LON = Double.parseDouble(selectedCity.getLonLat().get(0));
							}
						} catch (Exception e) {
							L.e("bootstrapAdmin: city lookup failed for " + city + ": " + e);
						}

						lonLat.add(LON);
						lonLat.add(LAT);
						request.setLonLat(lonLat);
						request.setName(user.getCompanyName());
						request.setBoxSettings(createDefaultBoxSettings());

						getApi().createInitialWasher(request, new Callback<WasherResponseHolder>() {
							@Override
							public void success(WasherResponseHolder data, Response response) {
								if (isSuccess(data)) {
									Washer washer = data.getResponse().getWasher();
									UserPreferences.putWasherData(BA.getContext(), washer.serialize());
									// Seed the owner's carwash-list cache with the just-created
									// carwash so the left-menu spinner renders its name immediately.
									// Without this it stays blank until the list is fetched on a
									// later app launch.
									ArrayList<WasherPublic> ownerWashers = new ArrayList<WasherPublic>();
									ownerWashers.add(washer);
									UserPreferences.putOwnerWasherListData(BA.getContext(), new WasherList(ownerWashers).serialize());
									BA.getEventBus().post(new WasherGetResponseEvent(washer));
									getEventsBus().post(new AuthAdminVerificationResponseEvent((BaseResponseHolder) data, Constants.APPROVED.equals(washer.getStatus())));
								} else {
									getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
									UserPreferences.onAdminLogout(BA.getContext());
									removeAllDbData();
									displayToast(data.getMessage());
								}
							}

							@Override
							public void failure(RetrofitError error) {
								UserPreferences.onAdminLogout(BA.getContext());
								removeAllDbData();
								onRetrofitError(error);
								getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
							}
						});
					}
				} else {
					UserPreferences.onAdminLogout(BA.getContext());
					removeAllDbData();
					displayToast(data.getMessage());
					getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
				}
			}

			@Override
			public void failure(RetrofitError error) {
				UserPreferences.onAdminLogout(BA.getContext());
				removeAllDbData();
				onRetrofitError(error);
				getEventsBus().post(new AuthAdminVerificationResponseEvent((AuthAdminVerificationResponseHolder) null));
			}
		});
	}

	/**
	 * Success event flagged cityRequired: the user is signed in but no carwash
	 * exists and no city is stored, so the UI must open the city picker first.
	 */
	private AuthAdminVerificationResponseEvent buildCityRequiredEvent() {
		AuthAdminVerificationResponseHolder holder = new AuthAdminVerificationResponseHolder();
		holder.setStatus("success");
		AuthAdminVerificationResponseEvent ev = new AuthAdminVerificationResponseEvent(holder);
		ev.setCityRequired(true);
		return ev;
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
