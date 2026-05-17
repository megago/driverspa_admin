package com.driverspa.admin.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.util.UserPreferences;

public class AdminAboutUSFragment extends AdminBaseFragment {
    String aboutUs = UserPreferences.getAboutUs(BA.getContext());

    public interface ActivityActions {
    }

    @BindView(R.id.web_view)
    WebView webView;
    @BindView(R.id.cover_view)
    View coverView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_about_us, container, false);
        ButterKnife.bind(this,view);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void performClick() {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","info@driverspa.kz", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        }, "btnEmail");

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void performClick() {
                String url = "http://www.driverspa.kz";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }, "btnWebSite");

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void performClick() {
                String url = "https://play.google.com/store/apps/details?id=com.driverspaAdmin";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }, "btnPlayMarket");

        webView.loadData(aboutUs, "text/html; charset=utf-8", null);

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                coverView.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                try{
                }catch (Exception e) {
                    // TODO: handle exception
                }
            }

        });
        return view;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
