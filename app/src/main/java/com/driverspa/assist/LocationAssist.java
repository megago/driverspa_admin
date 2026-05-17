package com.driverspa.assist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.CurrentGeoPosition;
import com.driverspa.util.RetrofitClient;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.LocationRequestEndEvent;
import com.driverspa.util.otto.LocationRequestEvent;
import com.driverspa.util.otto.LocationResponseEvent;
import com.driverspa.util.otto.TurnOnGPSRequestEvent;

public class LocationAssist extends BaseAssist {
	private LocationManager locationManager;

	private Location lastLocation;

	// Flag for GPS status
	boolean isGPSEnabled = false;

	// Flag for network status
	boolean isNetworkEnabled = false;

	// Flag for GPS status
	boolean canGetLocation = false;

	// Define a listener that responds to location updates
	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
//			lastLocation = location;
			if(isBetterLocation(location, lastLocation)) {
				lastLocation = location;
				makeUseOfNewLocation(lastLocation);
			}
			if(lastLocation == null){
				BA.getEventBus().post(new LocationResponseEvent(lastLocation));
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};

	public LocationAssist(RetrofitClient.DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) BA.getContext().getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * Broadcast location
	 * @param location - location of user
	 */
	protected void makeUseOfNewLocation(Location location) {
//		LogUtil.d(getTag(), location.toString());
		CurrentGeoPosition gpsData = new CurrentGeoPosition(location.getLatitude(),location.getLongitude());
		UserPreferences.putGPSdata(BA.getContext(),gpsData.serialize());
		BA.getEventBus().post(new LocationResponseEvent(location));
	}

	@SuppressLint("MissingPermission")
    @Subscribe
	public void onLocationRequested(LocationRequestEvent event) {
		// Register the listener with the Location Manager to receive location updates
		try{
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (isGPSEnabled) {
				// Getting GPS status
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			}
			else if(isNetworkEnabled) {
				// Getting network status
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			}
			else{
		       BA.getEventBus().post(new TurnOnGPSRequestEvent());
			   UserPreferences.putCityFoundByGPS(BA.getContext(),false);
			}
		}
		catch(Exception e){}
	}


	@Subscribe
	public void onLocationRequestEndReceived(LocationRequestEndEvent event) {
		stopListening();
	}

	protected void stopListening() {
		locationManager.removeUpdates(locationListener);
	}

	private final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if ( currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}