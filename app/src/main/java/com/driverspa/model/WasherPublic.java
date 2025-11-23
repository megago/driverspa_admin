package com.driverspa.model;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.android.clustering.ClusterItem;

public class WasherPublic extends WasherPublicBase implements ClusterItem {

	public WasherPublic() {
		super();
	}
	
	// GSON
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static WasherPublic deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, WasherPublic.class);
	}

	@Override
	public LatLng getPosition() {
		return new LatLng(Double.parseDouble(getLonLat().get(1).toString()),Double.parseDouble(getLonLat().get(0).toString()));
	}

	@Nullable
	@Override
	public String getTitle() {
		return "";
	}

	@Nullable
	@Override
	public String getSnippet() {
		return "";
	}

	@Nullable
	@Override
	public Float getZIndex() {
		return 0f;
	}

}
