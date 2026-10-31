package com.driverspa.admin.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminAboutUSActivity;
import com.driverspa.admin.activity.AdminAnalyticsActivity;
import com.driverspa.admin.activity.AdminFinancialActivity;
import com.driverspa.admin.activity.AdminNewWasherActivity;
import com.driverspa.admin.activity.AdminProfileActivity;
import com.driverspa.admin.activity.AdminShiftActivity;
import com.driverspa.admin.activity.AdminShopActivity;
import com.driverspa.admin.activity.AdminStaffsActivity;
import com.driverspa.admin.activity.AdminWasherReviewActivity;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.WasherList;
import com.driverspa.model.WasherPublic;
import com.driverspa.util.Converters;
import com.driverspa.util.Functions;
import com.driverspa.util.HttpClient;
import com.driverspa.util.RetrofitClient;
import com.driverspa.util.TransformationCircle;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.LocalWasherChangeRequestEvent;
import com.driverspa.util.otto.ws.AdminWasherInfoRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminLogoutRequestEvent;
import com.driverspa.util.otto.ws.DeleteDeviceRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

/**
 * The More tab of the home screen: the profile, the current car wash and every
 * screen the left menu used to open.
 */
public class AdminMoreFragment extends AdminBaseFragment {

    @BindView(R.id.moreAvatar)
    ImageView avatar;
    @BindView(R.id.moreAvatarInitial)
    TextView avatarInitial;
    @BindView(R.id.moreName)
    TextView name;
    @BindView(R.id.moreDetails)
    TextView details;
    @BindView(R.id.moreWasherInitial)
    TextView washerInitial;
    @BindView(R.id.moreWasherName)
    TextView washerName;
    @BindView(R.id.moreWasherCity)
    TextView washerCity;
    @BindView(R.id.moreSwitchWasher)
    View switchWasher;
    @BindView(R.id.moreVersion)
    TextView version;

    private final LinkedHashMap<String, String> userRoles = BA.getReference().getRoles();
    private User user;
    private Washer washer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_more, container, false);
        ButterKnife.bind(this, view);
        try {
            version.setText("v" + requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String city = Functions.getCityDescription(UserPreferences.getCity(requireContext()));
        washerCity.setText(city);
        washerCity.setVisibility(TextUtils.isEmpty(city) ? View.GONE : View.VISIBLE);
        return view;
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

    @Subscribe
    public void onUserReceived(UserGetSelfResponseEvent event) {
        user = event.getUser();
        bindProfile();
        bindWasher();
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        bindProfile();
        bindWasher();
    }

    private void bindProfile() {
        if (user == null || name == null) return;
        String firstName = user.getFirstName();
        name.setText(TextUtils.isEmpty(firstName) ? "" : firstName);
        avatarInitial.setText(initial(firstName));

        if (!TextUtils.isEmpty(user.getAvatar())) {
            avatar.setVisibility(View.VISIBLE);
            HttpClient.getPicasso()
                    .load(RetrofitClient.API_URL_IMAGES + user.getAvatar())
                    .fit()
                    .centerCrop()
                    .transform(new TransformationCircle(getActivity()))
                    .into(avatar);
        } else {
            avatar.setVisibility(View.GONE);
        }

        // Staff see their role, everyone sees the phone they sign in with.
        List<String> parts = new ArrayList<>();
        if (!user.isOwner() && washer != null) {
            String role = userRoles.get(Functions.getUserRole(washer, user.getId()));
            if (!TextUtils.isEmpty(role)) parts.add(role);
        }
        String phone = Functions.maskPhoneNumber(user.getPhone(), Converters.PHONE_PATTERN);
        if (!TextUtils.isEmpty(phone)) parts.add(phone);
        details.setText(TextUtils.join(" • ", parts));
    }

    private void bindWasher() {
        if (washer == null || washerName == null) return;
        String title = TextUtils.isEmpty(washer.getName()) || washer.getName().equals("NA")
                ? BA.str(R.string.no_name) : washer.getName();
        washerName.setText(title);
        washerInitial.setText(initial(title));
        switchWasher.setVisibility(user != null && user.isOwner() ? View.VISIBLE : View.GONE);
    }

    private static String initial(String text) {
        return TextUtils.isEmpty(text) ? "" : text.substring(0, 1).toUpperCase();
    }

    /** Owners pick another of their car washes, or add a new one. */
    @OnClick(R.id.moreSwitchWasher)
    public void onSwitchWasherClicked() {
        if (washer == null) return;
        final List<String> names = new ArrayList<>();
        final List<String> ids = new ArrayList<>();
        names.add(washerName.getText().toString());
        ids.add(washer.getId());
        String washerListData = UserPreferences.getOwnerWasherListData(BA.getContext());
        if (washerListData != null) {
            for (WasherPublic wp : WasherList.deserialize(washerListData).getWasherList()) {
                if (wp.getId().equals(washer.getId())) continue;
                names.add(wp.getName() != null ? Functions.shortenName(wp.getName()) : "");
                ids.add(wp.getId());
            }
        }
        names.add(getString(R.string.add_washer));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.switch_washer)
                .setSingleChoiceItems(names.toArray(new String[0]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == names.size() - 1) {
                            open(AdminNewWasherActivity.class);
                        } else if (which > 0) {
                            BA.getEventBus().post(new AdminWasherInfoRequestEvent(ids.get(which)));
                            BA.getEventBus().post(new LocalWasherChangeRequestEvent());
                        }
                    }
                })
                .show();
    }

    @OnClick(R.id.moreProfile)
    public void onProfileClicked() {
        if (!Functions.isUserHasAccess(washer, user)) return;
        open(AdminProfileActivity.class);
    }

    @OnClick(R.id.moreShop)
    public void onShopClicked() {
        open(AdminShopActivity.class);
    }

    @OnClick(R.id.moreStaff)
    public void onStaffClicked() {
        open(AdminStaffsActivity.class);
    }

    @OnClick(R.id.moreShifts)
    public void onShiftsClicked() {
        open(AdminShiftActivity.class);
    }

    @OnClick(R.id.moreFinance)
    public void onFinanceClicked() {
        open(AdminFinancialActivity.class);
    }

    @OnClick(R.id.moreAnalytics)
    public void onAnalyticsClicked() {
        open(AdminAnalyticsActivity.class);
    }

    @OnClick(R.id.moreReviews)
    public void onReviewsClicked() {
        open(AdminWasherReviewActivity.class);
    }

    @OnClick(R.id.moreSettings)
    public void onSettingsClicked() {
        if (!Functions.isUserHasAccess(washer, user, getActivity())) return;
        open(AdminProfileActivity.class);
    }

    @OnClick(R.id.moreAbout)
    public void onAboutClicked() {
        open(AdminAboutUSActivity.class);
    }

    @OnClick(R.id.moreLogout)
    public void onLogoutClicked() {
        AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(requireContext());
        dialog.setTitle(BA.str(R.string.confirm_logout));
        dialog.setNegativeButton(BA.str(R.string.cancel_word), null);
        dialog.setPositiveButton(BA.str(R.string.ok_word), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BA.getEventBus().post(new DeleteDeviceRequestEvent());
                BA.getEventBus().post(new AuthAdminLogoutRequestEvent());
            }
        });
        dialog.show();
    }

    // Through the activity so AdminBaseActivity.startActivity applies OPENING_ANIMATION.
    private void open(Class<?> screen) {
        if (getActivity() == null) return;
        getActivity().startActivity(new Intent(getActivity(), screen)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(OPENING_ANIMATION, false));
    }
}
