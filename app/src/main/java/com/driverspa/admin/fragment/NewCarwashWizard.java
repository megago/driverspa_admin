package com.driverspa.admin.fragment;
import com.driverspa.R;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import org.codepond.wizardroid.WizardFlow;
import org.codepond.wizardroid.layouts.BasicWizardLayout;

import com.driverspa.BA;
import com.driverspa.util.otto.ws.NewCarwashCreateResponseEvent;

/**
 * Created by Yerzhan Tanatov on 11/04/17.
 */
public class NewCarwashWizard extends BasicWizardLayout {

    /**
     * Note that initially BasicWizardLayout inherits from {@link android.support.v4.app.Fragment} and therefore you must have an empty constructor
     */
    public NewCarwashWizard() {
        super();
    }

    @Override
    public WizardFlow onSetup() {
        setNextButtonText(BA.str(R.string.next));
        setBackButtonText(BA.str(R.string.back));
        setFinishButtonText(BA.str(R.string.done));

        return new WizardFlow.Builder()
                .addStep(WasherCreateStep1.class)
                .addStep(WasherCreateStep2.class)
                .addStep(WasherCreateStep3.class)
                .addStep(WasherCreateStep4.class)
                .create();
    }

    @Subscribe
    public void onCarwashCreateResponseEvent(NewCarwashCreateResponseEvent event){
        if(event.getData() != null) {
            if (BaseAssist.isSuccess(event.getData())) {
                wizard.goNext();
            }
        }
    }

    @Override
    public void onWizardComplete() {
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        BA.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().unregister(this);
    }
}
