package com.driverspa.admin.fragment;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog.Builder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.HashMap;

import butterknife.ButterKnife;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.dialog.MultipleSelectDialog;
import com.driverspa.model.Washer;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AuthAdminLogoutRequestEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminSettingsFragment extends AdminBaseFragment {
    String phone = UserPreferences.getUserPhone(BA.getContext());

	public interface ActivityActions{
	}

	Washer washer;
//	@BindView(R.id.toggleGeoLocation)
//	ToggleButton geoLocation;
//	@BindView(R.id.info_check)
//	CheckBox check;
	
	private ActivityActions activityActions;
	MultipleSelectDialog servicesDialog;
	HashMap<String,Double> menuServicesPrices = new HashMap<String,Double>();
	
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_admin_settings, container,false);
			
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);				
		}
		
	    @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {	       
	        ButterKnife.bind(this, view);

//	 	   check.setChecked(UserPreferences.isUserNotificationEnabled(getActivity()));
//
//	 	   geoLocation.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			   @Override
//			   public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
//				   if (!isChecked)
//					   turnGPSOn();
//				   else
//					   turnGPSOff();
//			   }
//		   }) ;
	    }
	    
		@Override
		public void onDestroy() {
			super.onDestroy();	
//			UserPreferences.setUserLocation(getActivity(), geoLocation.isChecked());
//			UserPreferences.setUserNotification(getActivity(), check.isChecked());
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

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);			
			activityActions = (ActivityActions) activity;
		}

		@Override
		public void onDetach() {
			super.onDetach();
			activityActions = null;
	  }		

//	   @OnClick(R.id.logout)
	   public void onLogoutButtonClicked(){
		   Builder dialog = new MaterialAlertDialogBuilder(getActivity());
			dialog.setTitle("Вы действительно хотите выйти?");
			dialog.setNegativeButton("Отмена", null);
			dialog.setPositiveButton("Ок", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					BA.getEventBus().post(new AuthAdminLogoutRequestEvent());
				}
			});
			dialog.show();
	   }

	   public void turnGPSOn()
	   {
	        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
	        intent.putExtra("enabled", true);
	        getActivity().sendBroadcast(intent);

	       String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	       if(!provider.contains("gps")){ //if gps is disabled
	           final Intent poke = new Intent();
	           poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); 
	           poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	           poke.setData(Uri.parse("3")); 
	           getActivity().sendBroadcast(poke);
	       }
	   }
	   // automatic turn off the gps
	   public void turnGPSOff()
	   {
	       String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	       if(provider.contains("gps")){ //if gps is enabled
	           final Intent poke = new Intent();
	           poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
	           poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	           poke.setData(Uri.parse("3")); 
	           this.getActivity().sendBroadcast(poke);
	       }
	   }
	   
	   public boolean isGPSOn(){
		   String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		   return provider.contains("gps");
	   }


	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
	}


}
