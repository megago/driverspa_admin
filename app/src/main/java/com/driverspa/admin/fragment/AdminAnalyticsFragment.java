package com.driverspa.admin.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AnalyticsListAdapter;
import com.driverspa.model.Statistic;
import com.driverspa.model.Washer;
import com.driverspa.util.StatisticLabels;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.ExpandableListView;
import com.driverspa.view.PieChartView;

public class AdminAnalyticsFragment extends AdminBaseFragment {

		public static final String STAT = "statistic";
		public static AdminAnalyticsFragment init(Statistic statistic){
			AdminAnalyticsFragment fragment = new AdminAnalyticsFragment();
			Bundle b = new Bundle();
			b.putString(STAT, statistic.serialize());
			fragment.setArguments(b);
			return fragment;
		}

        public interface ActivityActions {
        }

	    private ActivityActions activityActions;
		Washer washer;
		@BindView(R.id.expandable_list_view)
		ExpandableListView listView;
		AnalyticsListAdapter adapter;
	    List<Statistic> statisticList;
	    @BindView(R.id.pieChart)
		PieChartView mChart;
		Statistic statistic;
		Typeface tf;

		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	        return inflater.inflate(R.layout.fragment_admin_analytics, container, false);
	    }
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);					
		}
		
	    @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {
			ButterKnife.bind(this, view);

			mChart.getDescription().setEnabled(false);
			tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");


			mChart.setUsePercentValues(true);
			mChart.getDescription().setEnabled(false);
			mChart.setExtraOffsets(5, 10, 5, 5);

			mChart.setDragDecelerationFrictionCoef(0.95f);

			mChart.setCenterTextTypeface(tf);
//			mChart.setCenterText(generateCenterText());

			mChart.setDrawHoleEnabled(true);
			mChart.setHoleColor(Color.WHITE);

			mChart.setTransparentCircleColor(Color.WHITE);
			mChart.setTransparentCircleAlpha(110);

			mChart.setHoleRadius(58f);
			mChart.setTransparentCircleRadius(61f);

			mChart.setDrawCenterText(true);

			mChart.setRotationAngle(0);
			// enable rotation of the chart by touch
			mChart.setRotationEnabled(true);
			mChart.setHighlightPerTapEnabled(true);

			Legend l = mChart.getLegend();
			l.setTextColor(Color.WHITE);
			l.setTextSize(7f);
			l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
			l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
			l.setOrientation(Legend.LegendOrientation.VERTICAL);
			l.setDrawInside(false);
			l.setXEntrySpace(7f);
			l.setYEntrySpace(0f);
			l.setYOffset(0f);

			mChart.setEntryLabelColor(Color.WHITE);
			mChart.setEntryLabelTypeface(tf);
			mChart.setEntryLabelTextSize(12f);

			adapter = new AnalyticsListAdapter(getActivity());
			statisticList = new ArrayList<Statistic>();
			if(!TextUtils.isEmpty(getArguments().getString(STAT))) {
				statistic = Statistic.deserialize(getArguments().getString(STAT));
				List<Statistic.StatisticDetail> sortedDetails = statistic.getStatisticsList();
				Collections.sort(sortedDetails,new Comparator<Statistic.StatisticDetail>() {
					@Override
					public int compare(Statistic.StatisticDetail lhs, Statistic.StatisticDetail rhs) {
						if(Float.parseFloat(lhs.getItemValue()) < Float.parseFloat(rhs.getItemValue()))
							return 1;
						else
							return -1;
					}
				});

				statisticList.add(statistic);
				mChart.setData(generatePieData());
				mChart.setCenterText(generateCenterText());
				adapter.set(statistic.getStatisticsList());
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
	}


	protected PieData generatePieData() {

		final List<Statistic.StatisticDetail> chartStatistics = new ArrayList<>();
		for(Statistic.StatisticDetail st: statistic.getStatisticsList()){
			if(!st.getManualInfo()){
				chartStatistics.add(st);
			}
		}
		int count = chartStatistics.size();
		ArrayList<PieEntry> entries1 = new ArrayList<PieEntry>();

		for (int i = 0; i < count; i++) {
			try {
				entries1.add(new PieEntry(Float.parseFloat(chartStatistics.get(i).getItemValue()), StatisticLabels.localize(chartStatistics.get(i).getItemName())));
			}
			catch(Exception e){}
		}

//		PieDataSet ds1 = new PieDataSet(entries1, statistic.getTitle());
////		int templates [][] = {ColorTemplate.LIBERTY_COLORS, ColorTemplate.JOYFUL_COLORS, ColorTemplate.PASTEL_COLORS, ColorTemplate.COLORFUL_COLORS, ColorTemplate.VORDIPLOM_COLORS , ColorTemplate.MATERIAL_COLORS};
//		ds1.setColors(ColorTemplate.JOYFUL_COLORS);
//		ds1.setSliceSpace(2f);
//		ds1.setValueTextColor(Color.WHITE);
//		ds1.setValueTextSize(12f);
//
//		PieData d = new PieData(ds1);
//		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");
//		d.setValueTypeface(tf);
//

		PieDataSet dataSet = new PieDataSet(entries1, StatisticLabels.localize(statistic.getTitle()));
		dataSet.setDrawIcons(false);
		dataSet.setSliceSpace(3f);
		dataSet.setIconsOffset(new MPPointF(0, 40));
		dataSet.setSelectionShift(5f);

		// add a lot of colors

		ArrayList<Integer> colors = new ArrayList<Integer>();

//		for (int c : ColorTemplate.VORDIPLOM_COLORS)
//			colors.add(c);

		for (int c : ColorTemplate.JOYFUL_COLORS)
			colors.add(c);

		for (int c : ColorTemplate.COLORFUL_COLORS)
			colors.add(c);

		for (int c : ColorTemplate.LIBERTY_COLORS)
			colors.add(c);

		for (int c : ColorTemplate.PASTEL_COLORS)
			colors.add(c);

		colors.add(ColorTemplate.getHoloBlue());

		dataSet.setColors(colors);
		//dataSet.setSelectionShift(0f);

		PieData data = new PieData(dataSet);
		data.setValueFormatter(new PercentFormatter());
		data.setValueTextSize(11f);
		data.setValueTextColor(Color.WHITE);
		data.setValueTypeface(tf);
		mChart.setData(data);

		// undo all highlights
		mChart.highlightValues(null);


		return data;
	}

	private SpannableString generateCenterText() {
		SpannableString s = new SpannableString(StatisticLabels.localize(statistic.getTitle()));
//		s.setSpan(new RelativeSizeSpan(1.7f), 0, 14, 0);
//		s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
//		s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
//		s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
//		s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
//		s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
		return s;
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
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
  }
