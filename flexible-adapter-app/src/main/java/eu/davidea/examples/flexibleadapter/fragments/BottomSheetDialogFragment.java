package eu.davidea.examples.flexibleadapter.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.ListView;

import eu.davidea.examples.flexibleadapter.R;

/**
 * @author Davide Steduto
 * @since 20/04/2016
 */
public class BottomSheetDialogFragment extends android.support.design.widget.BottomSheetDialogFragment
		implements View.OnClickListener, AdapterView.OnItemClickListener {

	public static final String TAG = BottomSheetDialogFragment.class.getSimpleName();
	public static final String ARG_LAYOUT = "layout";

	private BottomSheetDialog mBottomSheetDialog;
	private ArrayAdapter mAdapter;
	private ListPopupWindow mPopup;
	private int mReferencePosition = -1, mChildPosition = -1;

	public BottomSheetDialogFragment() {
	}

	/**
	 * Use from Activities.
	 *
	 * @param layoutResId custom layout to use for the bottom sheet
	 * @return a new instance of BottomSheetDialogFragment
	 */
	public static BottomSheetDialogFragment newInstance(@LayoutRes int layoutResId) {
		return newInstance(layoutResId, null);
	}

	/**
	 * Use from Fragments.
	 *
	 * @param layoutResId custom layout to use for the bottom sheet
	 * @param fragment    target fragment
	 * @return a new instance of BottomSheetDialogFragment
	 */
	public static BottomSheetDialogFragment newInstance(@LayoutRes int layoutResId, @Nullable Fragment fragment) {
		BottomSheetDialogFragment bottomSheetFragment = new BottomSheetDialogFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LAYOUT, layoutResId);
		bottomSheetFragment.setArguments(args);
		if (fragment != null) bottomSheetFragment.setTargetFragment(fragment, 0);
		return bottomSheetFragment;
	}

	private OnParameterSelectedListener getListener() {
		//Setting the listener
		OnParameterSelectedListener listener = (OnParameterSelectedListener) getTargetFragment();
		if (listener == null && getActivity() instanceof OnParameterSelectedListener) {
			listener = (OnParameterSelectedListener) getActivity();
		}
		return listener;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.AppTheme_BottomSheetDialog);
		mBottomSheetDialog.setContentView(getArguments().getInt(ARG_LAYOUT));
		View buttonList = mBottomSheetDialog.findViewById(R.id.dialog_select_reference_button);
		buttonList.setOnClickListener(this);
		View fab = mBottomSheetDialog.findViewById(R.id.fab);
		fab.setOnClickListener(this);
		createPopUp();
		return mBottomSheetDialog;
	}

	private void createPopUp() {
		//Create the Adapter
		if (mAdapter == null)
			mAdapter = new ArrayAdapter(getContext(), R.layout.reference_row, getListener().getReferenceList());

		//Setting up the popup
		Log.d(TAG, "Setting up the Popup");
		mPopup = new ListPopupWindow(getContext());
		mPopup.setAnchorView(mBottomSheetDialog.findViewById(R.id.dialog_select_reference_button));
		mPopup.setModal(true);
		mPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
		mPopup.setAnimationStyle(android.R.style.Animation_Dialog);
		mPopup.setAdapter(mAdapter);
		if (mAdapter.getCount() > 6)
			mPopup.setHeight(getResources().getDimensionPixelSize(R.dimen.popup_max_height));

	}


	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.fab) {
			getListener().onParameterSelected(mReferencePosition, mChildPosition);
			dismiss();
		} else {
			mPopup.show();
			ListView listView = mPopup.getListView();
			listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
			listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
			listView.setOnItemClickListener(this);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListView listView = (ListView) parent;
		Button buttonList = (Button) mBottomSheetDialog.findViewById(R.id.dialog_select_reference_button);
		buttonList.setText(listView.getItemAtPosition(position).toString());
		mReferencePosition = getListener().getReferenceList().indexOf(listView.getItemAtPosition(position));
		Log.d(TAG, "Header position = " + mReferencePosition);
		mPopup.dismiss();
	}

}