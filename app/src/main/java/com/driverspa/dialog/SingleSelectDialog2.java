
package com.driverspa.dialog;
import com.driverspa.R;
import com.driverspa.BA;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author Yerzhan Tanatov
 */
public abstract class SingleSelectDialog2 {

	public abstract void onDismiss(HashMap<Integer,String> namesSelectedItm);
	
    public static final String FILTER_NAME = "FILTER_NAME";
    public static final String FILTER_ID = "FILTER_ID";
    
	AlertDialog.Builder dialogBuilder;
	AlertDialog dialog = null;
	Activity activity;
	HashMap<Integer,String> selectedItem;
	boolean[] itemsChecked;
	String[] itemsNames;
	String[] itemsIds;
    String title;
    public static String ALL_CHOICES;
    
	public SingleSelectDialog2(Activity activity, List<HashMap<String,String>> model, String title) {
		this.activity = activity;
		this.title = title;
//		ALL_CHOICES = activity.getResources().getString(R.string.history_choice_all);
		if (model != null && model.size() > 0) {
			createFilterData(model);
		}
		
	}

	public SingleSelectDialog2(Activity activity, List<HashMap<String,String>> model, String title,
			HashMap<Integer,String> selectedItem) {
		this.activity = activity;
//		ALL_CHOICES = activity.getResources().getString(R.string.history_choice_all);
		this.title = title;
//		this.selectedItem = selectedItem;
		if (model != null && model.size() > 0) {
			createFilterData(model);
		}
	}

	private HashMap<Integer, String> getSelectedMap(int startIndex) {
		AtomicInteger orderedIndex = new AtomicInteger(0);
		HashMap<Integer, String> selectedMap = new HashMap<Integer, String>();
		IntStream.range(startIndex, itemsChecked.length)
				.filter(i -> itemsChecked[i])
				.forEach(i -> selectedMap.put(orderedIndex.getAndIncrement(), itemsIds[i]));
		return selectedMap;
	}

	void createFilterData(List<HashMap<String,String>> model) {
		itemsNames = new String[model.size() + 1];
		itemsChecked = new boolean[model.size() + 1];
		itemsIds = new String[model.size() + 1];
		itemsNames[0] = ALL_CHOICES;
		itemsChecked[0] = true;
		itemsIds[0] = "";
		IntStream.range(0, model.size()).forEach(i -> {
			itemsNames[i + 1] = model.get(i).get(FILTER_NAME).toString();
			itemsIds[i + 1] = model.get(i).get(FILTER_ID).toString();
			itemsChecked[i + 1] = selectedItem != null
					&& selectedItem.get(FILTER_NAME).equals(itemsNames[i + 1]) ? true : false;
		});
		init(itemsNames, itemsChecked);
	}

	private void init(String[] items, boolean[] checked) {

		dialogBuilder = new MaterialAlertDialogBuilder(activity);
		dialogBuilder.setTitle(title);
		dialogBuilder.setPositiveButton(BA.str(R.string.ok_word),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onDismiss(getSelectedMap(1));
					}
				});

		
		dialogBuilder.setMultiChoiceItems(items, checked,
				new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1,
							boolean arg2) {
						if (!itemsNames[arg1].equals(ALL_CHOICES)) {

							IntStream.range(1, itemsNames.length).forEach(i -> {
								itemsChecked[i] = false;
								itemsChecked[0] = false;
								((AlertDialog) arg0).getListView().setItemChecked(i, false);
							});

							itemsChecked[arg1] = arg2;
							itemsChecked[0] = false;
							((AlertDialog) arg0).getListView().setItemChecked(0, false);

							boolean noCheckBoxChecked = IntStream.range(1, itemsNames.length)
									.noneMatch(i -> itemsChecked[i]);
							if (noCheckBoxChecked) {
								itemsChecked[0] = true;
								((AlertDialog) arg0).getListView().setItemChecked(0, true);
							}
						} else if (itemsChecked[0]) {
							IntStream.range(1, itemsNames.length).forEach(i -> {
								((AlertDialog) arg0).getListView().setItemChecked(i, false);
								itemsChecked[i] = false;
							});
						} else {
							itemsChecked[0] = true;
							((AlertDialog) arg0).getListView().setItemChecked(0, true);
						}
					}
				});

		dialogBuilder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				onDismiss(getSelectedMap(1));
			}
		});
		dialog = dialogBuilder.create();
	}

	public void show() {
		dialogBuilder.show();
	}

	public void clearFilter() {
		IntStream.range(0, itemsChecked.length).forEach(i -> itemsChecked[i] = false);
	}

	public String[] getItemsIds() {
		return itemsIds;
	}

	public boolean[] getItemsChecked() {
		return itemsChecked;
	}

	public void setItemsChecked(final boolean[] itemsChecked) {
		this.itemsChecked = itemsChecked;
		dialogBuilder.setMultiChoiceItems(itemsNames, itemsChecked,
				new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1,
							boolean arg2) {
						if (!itemsNames[arg1].equals(ALL_CHOICES)) {
							
										IntStream.range(1, itemsNames.length).forEach(i -> {
								itemsChecked[i] = false;
								itemsChecked[0] = false;
								((AlertDialog) arg0).getListView().setItemChecked(i, false);
										});

							itemsChecked[arg1] = arg2;
							itemsChecked[0] = false;
							((AlertDialog) arg0).getListView().setItemChecked(0, false);
										boolean noCheckBoxChecked = IntStream.range(1, itemsNames.length)
												.noneMatch(i -> itemsChecked[i]);
							if (noCheckBoxChecked) {
								itemsChecked[0] = true;
								((AlertDialog) arg0).getListView().setItemChecked(0, true);
							}
						} else if (itemsChecked[0]) {
										IntStream.range(1, itemsNames.length).forEach(i -> {
								((AlertDialog) arg0).getListView().setItemChecked(i, false);
								itemsChecked[i] = false;
										});
						} else {
							itemsChecked[0] = true;
							((AlertDialog) arg0).getListView().setItemChecked(0, true);
						}
					}
	    });
	}
}
