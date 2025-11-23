package com.driverspa.util.otto.ws;

import com.driverspa.model.api.response.AuthAdminVerificationResponseHolder;
import com.driverspa.model.api.response.BaseResponseHolder;


public class AuthAdminVerificationResponseEvent{

   private AuthAdminVerificationResponseHolder authVerificationResponse;
   boolean isActiveCarwash;
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
}
