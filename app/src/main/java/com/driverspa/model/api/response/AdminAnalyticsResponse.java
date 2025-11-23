package com.driverspa.model.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.driverspa.model.Statistic;

public class AdminAnalyticsResponse {

	@SerializedName("analytical_statistics")
	private List<Statistic> statistics;

	public List<Statistic> getStatistics() {
		return statistics;
	}

	public void setStatistics(List<Statistic> statistics) {
		this.statistics = statistics;
	}
}
