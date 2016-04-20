package eu.davidea.examples.flexibleadapter.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;

/**
 * @author Davide Steduto
 * @since 20/04/2016
 */
public class BottomSheetDialogFragment extends android.support.design.widget.BottomSheetDialogFragment {

	public static final String TAG = BottomSheetDialogFragment.class.getSimpleName();
	public static final String ARG_LAYOUT = "layout";

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

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), getTheme());
		bottomSheetDialog.setContentView(getArguments().getInt(ARG_LAYOUT));

		return bottomSheetDialog;
	}

}