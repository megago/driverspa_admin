package com.driverspa.model.api.request;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class AuthAdminRegistrationRequest  implements Parcelable {

	@SerializedName("mobile")
	private String phone;
	@SerializedName("company_name")
	private String companyName;
	// OTP delivery channel: "email" (default), "sms" or "whatsapp".
	@SerializedName("channel")
	private String channel = "email";
	// Email destination — required when channel == "email".
	@SerializedName("email")
	private String email;

	public AuthAdminRegistrationRequest (String companyName, String phone) {
		this.phone = phone;
		this.companyName = companyName;
	}

	public AuthAdminRegistrationRequest (String companyName, String phone, String channel) {
		this.phone = phone;
		this.companyName = companyName;
		this.channel = channel;
	}

	public AuthAdminRegistrationRequest (String companyName, String phone, String channel, String email) {
		this.phone = phone;
		this.companyName = companyName;
		this.channel = channel;
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(phone);
		dest.writeString(companyName);
		dest.writeString(channel);
		dest.writeString(email);
	}

	public AuthAdminRegistrationRequest(Parcel in) {
		phone = in.readString();
		companyName = in.readString();
		channel = in.readString();
		email = in.readString();
	}
	
	public static final Creator<AuthAdminRegistrationRequest> CREATOR = new Creator<AuthAdminRegistrationRequest>() {
		public AuthAdminRegistrationRequest createFromParcel(Parcel in) {
			return new AuthAdminRegistrationRequest(in);
		}

		public AuthAdminRegistrationRequest[] newArray(int size) {
			return new AuthAdminRegistrationRequest[size];
		}
	};
		
}
