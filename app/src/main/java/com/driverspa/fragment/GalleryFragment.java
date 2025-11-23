package com.driverspa.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.util.HttpClient;
import com.driverspa.util.RetrofitClient;
import uk.co.senab.photoview.PhotoView;

public class GalleryFragment extends BaseFragment {

	public static final String PHOTO_PARCELABLE = "PHOTO_PARCELABLE";
	public static final String USER_ID = "USER_ID";
	public static final String INDEX_PHOTO = "INDEX_PHOTO";
	
	public interface ActivityActions {
	}
	
	
	private PhotoParcelable photo;
	private PhotoView image;
	
	public static GalleryFragment init(PhotoParcelable photo, int index) {
		GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putParcelable(PHOTO_PARCELABLE, photo);
        args.putInt(INDEX_PHOTO, index);
        fragment.setArguments(args);
        return fragment;
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photo = getArguments().getParcelable(PHOTO_PARCELABLE);                       
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_item, container, false);
        image = (PhotoView) view.findViewById(R.id.gallery_item);
        
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        
        HttpClient.getPicasso()
        .load(RetrofitClient.API_URL_IMAGES+photo.getOrginalUrl())
        .fit()
        .centerInside()
        .into(image, new Callback(){
			@Override
			public void onSuccess() {
				if( progressBar != null )
					progressBar.setVisibility(View.GONE);
			}
			@Override
			public void onError() {
			}
		});
           
        return view;
    }

	@Override
	public void onResume() {
		super.onResume();
		BA.getEventBus().register(this);
	};
	
	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		HttpClient.getPicasso().cancelRequest(image);
	}
	
}
