package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AuthAdminVerificationResponseHolder;
import com.driverspa.model.api.response.BaseResponseHolder;


public class AuthAdminVerificationResponseEvent{

   private AuthAdminVerificationResponseHolder authVerificationResponse;
   boolean isActiveCarwash;
   // true when activate succeeded but the user must still create a password
   // (new password flow). In that case no carwash bootstrap has happened yet.
   private boolean passwordRequired;
	public AuthAdminVerificationResponseEvent(BaseResponseHolder data, boolean isActiveCarwash){
		authVerificationResponse = new AuthAdminVerificationResponseHolder();
		authVerificationResponse.setMessage(data.getMessage());
		authVerificationResponse.setStatus(data.getStatus());
		this.isActiveCarwash = isActiveCarwash;
	}

	public AuthAdminVerificationResponseEvent(AuthAdminVerificationResponseHolder authVerificationResponse) {
		this.authVerificationResponse = authVerificationResponse;
	}

	public AuthAdminVerificationResponseHolder getAuthLoginResponse() {
		return authVerificationResponse;
	}

	public boolean isActiveCarwash() {
		return isActiveCarwash;
	}

	public boolean isPasswordRequired() {
		return passwordRequired;
	}

	public void setPasswordRequired(boolean passwordRequired) {
		this.passwordRequired = passwordRequired;
	}
}
