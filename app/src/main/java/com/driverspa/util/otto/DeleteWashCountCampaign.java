package com.driverspa.util.otto;

import com.driverspa.model.WashCountCampaign;

/**
 * Created by Yerzhan Tanatov on 03/02/17.
 */
public class DeleteWashCountCampaign {
    WashCountCampaign campaign;

    public DeleteWashCountCampaign(WashCountCampaign campaign) {
        this.campaign = campaign;
    }

    public WashCountCampaign getCampaign() {
        return campaign;
    }

    public void setCampaign(WashCountCampaign campaign) {
        this.campaign = campaign;
    }
}
