package com.driverspa.admin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.squareup.otto.Subscribe;

import org.codepond.wizardroid.WizardFlow;
import org.codepond.wizardroid.WizardFragment;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.util.otto.WizardNextStepAllowEvent;
import com.driverspa.util.otto.WizardNextStepVerifyEvent;
import com.driverspa.util.otto.WizardPreviousStepAllowEvent;

/**
 * Created by Yerzhan Tanatov on 11/04/17.
 */
public class CustomCarwashCreateWizard extends WizardFragment implements View.OnClickListener {

    //You must have an empty constructor according to Fragment documentation
    public CustomCarwashCreateWizard() {
    }
    /**
     * Binding the layout and setting buttons hooks
     */
    Button nextButton,previousButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View wizardLayout = inflater.inflate(R.layout.custom_carwash_wizard, container, false);
        nextButton = (Button) wizardLayout.findViewById(R.id.wizard_next_button);
        nextButton.setOnClickListener(this);
        previousButton = (Button) wizardLayout.findViewById(R.id.wizard_previous_button);
        previousButton.setOnClickListener(this);
        return wizardLayout;
    }

    //You must override this method and create a wizard flow by
    //using WizardFlow.Builder as shown in this example
    @Override
    public WizardFlow onSetup() {
        return new WizardFlow.Builder()
                .addStep(WasherCreateStep1.class)
                .addStep(WasherCreateStep2.class)
                .addStep(WasherCreateStep3.class)
                .addStep(WasherCreateStep4.class)
                .create();
    }

    /**
     * Triggered when the wizard is completed.
     * Overriding this method is optional.
     */
//    @Override
//    public void onWizardComplete() {
//        //Do whatever you want to do once the Wizard is complete
//        //in this case I just close the activity, which causes Android
//        //to go back to the previous activity.
//        getActivity().finish();
//    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.wizard_next_button:
                //Tell the wizard to go to next step
                BA.getEventBus().post(new WizardNextStepVerifyEvent(wizard.getCurrentStepPosition()));
                break;
            case R.id.wizard_previous_button:
                //Tell the wizard to go back one step
                wizard.goBack();
                break;
        }
        updateWizardControls();
    }

    @Subscribe
    public void onNextStepAllowed(WizardNextStepAllowEvent event){
        wizard.goNext();
        updateWizardControls();
    }

    @Subscribe
    public void onPreviousStepAllowed(WizardPreviousStepAllowEvent event){
        wizard.goBack();
        updateWizardControls();

    }

    /**
     * Updates the UI according to current step position
     */
    private void updateWizardControls() {
        nextButton.setText(wizard.isLastStep()
                ? BA.str(R.string.done)
                : BA.str(R.string.next));
        previousButton.setEnabled(!wizard.isFirstStep());
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

