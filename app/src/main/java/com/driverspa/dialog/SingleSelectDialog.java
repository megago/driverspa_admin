
package com.driverspa.dialog;

import android.app.Activity;
import android.app.AlertDialog;
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
public abstract class SingleSelectDialog {

	public abstract void onDismiss(HashMap<Integer,String> namesSelectedItm);
	
    public static final String NAME = "NAME";
    public static final String ID = "ID";    
	AlertDialog.Builder dialogBuilder;
	AlertDialog dialog = null;
	Activity activity;
	List<HashMap<String,String>> selectedItems;
	boolean[] itemsChecked;
	String[] itemsNames;
	String[] itemsIds;
    String title;
    public static String ALL_CHOICES;
    
	public SingleSelectDialog(Activity activity, List<HashMap<String,String>> model, String title) {
		this.activity = activity;
		this.title = title;
		if (model != null && model.size() > 0) {
			createFilterData(model);
		}		
	}

	public SingleSelectDialog(Activity activity, List<HashMap<String,String>> model, String title,
			List<HashMap<String,String>> selectedItems) {
		this.activity = activity;
		this.title = title;
		this.selectedItems = selectedItems;
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
		itemsNames = new String[model.size()];
		itemsChecked = new boolean[model.size()];
		itemsIds = new String[model.size()];
		IntStream.range(0, model.size()).forEach(i -> {
			itemsNames[i] = model.get(i).get(NAME).toString();
			itemsIds[i] = model.get(i).get(ID).toString();
			if(selectedItems != null){
				itemsChecked[i] = selectedItems.contains(model.get(i));
			}else
				itemsChecked[i] = false;
		});
		init(itemsNames, itemsChecked);
	}

	private void init(String[] items, boolean[] checked) {
		dialogBuilder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT);
		dialogBuilder.setTitle(title);
		dialogBuilder.setCancelable(false);
		dialogBuilder.setPositiveButton("Ок",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onDismiss(getSelectedMap(0));
					}
				});
		
		dialogBuilder.setMultiChoiceItems(items, checked,
				new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1, boolean arg2) {

						IntStream.range(0, itemsNames.length).forEach(i -> {
							itemsChecked[i] = false;
							((AlertDialog) arg0).getListView().setItemChecked(i, false);
						});

						itemsChecked[arg1] = arg2;

						boolean noCheckBoxChecked = IntStream.range(1, itemsNames.length)
								.noneMatch(i -> itemsChecked[i]);
						if(noCheckBoxChecked) {
							itemsChecked[arg1] = true;
							((AlertDialog) arg0).getListView().setItemChecked(arg1, true);
						}
					}
				});

		dialogBuilder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				onDismiss(getSelectedMap(0));
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
