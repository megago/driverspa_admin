package com.driverspa.model.api.request;

import com.driverspa.model.Staff;

public class StaffOperationRequest extends Staff {


    public StaffOperationRequest(String firstName,String lastName,String mobile, String role, String status, Integer salaryPercentage){
        super.firstName = firstName;
        super.lastName = lastName;
        super.role = role;
        super.status = status;
        super.mobile = mobile;
        super.salaryPercentage = salaryPercentage;
    }


}
