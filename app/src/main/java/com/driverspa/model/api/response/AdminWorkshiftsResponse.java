package com.driverspa.model.api.response;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.driverspa.model.Workshift;

public class AdminWorkshiftsResponse extends BaseResponse {

	@SerializedName("work_shifts")
	List<Workshift> workshifts;

	@SerializedName("meta")
	private ListMetaDataResponse meta;

	public ListMetaDataResponse getMeta() {
		return meta;
	}

	public void setMeta(ListMetaDataResponse meta) {
		this.meta = meta;
	}

	public List<Workshift> getWorkshifts() {
		return workshifts;
	}

	public void setWorkshifts(List<Workshift> workshifts) {
		this.workshifts = workshifts;
	}

	// GSON
		public String serialize() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}

		public static AdminWorkshiftsResponse deserialize(String serializedData) {
			Gson gson = new Gson();
			return gson.fromJson(serializedData, AdminWorkshiftsResponse.class);
		}
	}
