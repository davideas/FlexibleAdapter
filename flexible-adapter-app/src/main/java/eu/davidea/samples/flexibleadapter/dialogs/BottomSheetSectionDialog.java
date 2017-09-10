package eu.davidea.samples.flexibleadapter.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.ListView;

import eu.davidea.common.SimpleOnTouchListener;
import eu.davidea.samples.flexibleadapter.R;

/**
 * @author Davide Steduto
 * @since 20/04/2016
 */
@SuppressWarnings({"ConstantConditions", "unchecked"})
public class BottomSheetSectionDialog extends android.support.design.widget.BottomSheetDialogFragment
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String TAG = BottomSheetSectionDialog.class.getSimpleName();
    public static final String ARG_LAYOUT = "layout";

    private android.support.design.widget.BottomSheetDialog mBottomSheetDialog;
    private ArrayAdapter mAdapterItemType;
    private ArrayAdapter mAdapterReference;
    private ListPopupWindow mPopupItemType;
    private ListPopupWindow mPopupReference;
    private int mItemType = 0, mReferencePosition = -1, mChildPosition = 0;

    public BottomSheetSectionDialog() {
    }

    /**
     * Use from Activities.
     *
     * @param layoutResId custom layout to use for the bottom sheet
     * @return a new instance of BottomSheetSectionDialog
     */
    public static BottomSheetSectionDialog newInstance(@LayoutRes int layoutResId) {
        return newInstance(layoutResId, null);
    }

    /**
     * Use from Fragments.
     *
     * @param layoutResId custom layout to use for the bottom sheet
     * @param fragment    target fragment
     * @return a new instance of BottomSheetSectionDialog
     */
    public static BottomSheetSectionDialog newInstance(@LayoutRes int layoutResId, @Nullable Fragment fragment) {
        BottomSheetSectionDialog bottomSheetFragment = new BottomSheetSectionDialog();
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
        mBottomSheetDialog = new android.support.design.widget.BottomSheetDialog(getActivity(), R.style.AppTheme_BottomSheetDialog);
        mBottomSheetDialog.setContentView(getArguments().getInt(ARG_LAYOUT));
        mBottomSheetDialog.findViewById(R.id.select_item_type).setOnClickListener(this);
        mBottomSheetDialog.findViewById(R.id.select_item_type).setOnTouchListener(new SimpleOnTouchListener(getContext()));
        mBottomSheetDialog.findViewById(R.id.select_reference_button).setOnClickListener(this);
        mBottomSheetDialog.findViewById(R.id.select_reference_button).setOnTouchListener(new SimpleOnTouchListener(getContext()));
        mBottomSheetDialog.findViewById(R.id.new_item).setOnClickListener(this);
        createPopUps();
        return mBottomSheetDialog;
    }

    private void createPopUps() {
        //Create the Adapter
        if (mAdapterReference == null) {
            mAdapterItemType = new ArrayAdapter(getContext(), R.layout.reference_spinner_item, new String[]{"Simple Item", "Expandable", "Expandable Section", "Section"});
            mAdapterReference = new ArrayAdapter(getContext(), R.layout.reference_spinner_item, getListener().getReferenceList());
        }
        //Setting up the popups
        Log.d(TAG, "Setting up the Popups");
        //Item Type
        mPopupItemType = new ListPopupWindow(getContext());
        mPopupItemType.setAnchorView(mBottomSheetDialog.findViewById(R.id.select_item_type));
        mPopupItemType.setModal(true);
        mPopupItemType.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
        mPopupItemType.setAnimationStyle(android.R.style.Animation_Dialog);
        mPopupItemType.setAdapter(mAdapterItemType);
        mPopupItemType.setVerticalOffset(-100);

        //Header Reference
        mPopupReference = new ListPopupWindow(getContext());
        mPopupReference.setAnchorView(mBottomSheetDialog.findViewById(R.id.select_reference_button));
        mPopupReference.setModal(true);
        mPopupReference.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
        mPopupReference.setAnimationStyle(android.R.style.Animation_Dialog);
        mPopupReference.setAdapter(mAdapterReference);
        mPopupReference.setVerticalOffset(-100);
        if (mAdapterReference.getCount() > 6)
            mPopupReference.setHeight(getResources().getDimensionPixelSize(R.dimen.popup_max_height));
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.new_item) {
            getListener().onParameterSelected(mItemType, mReferencePosition, mChildPosition);
            dismiss();
        } else if (view.getId() == R.id.select_item_type) {
            mPopupItemType.show();
            ListView listView = mPopupItemType.getListView();
            listView.setTag(R.id.select_item_type);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            listView.setOnItemClickListener(this);
        } else if (view.getId() == R.id.select_reference_button) {
            mPopupReference.show();
            ListView listView = mPopupReference.getListView();
            listView.setTag(R.id.select_reference_button);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            listView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = (ListView) parent;
        if (listView.getTag().equals(R.id.select_item_type)) {
            Button buttonList = mBottomSheetDialog.findViewById(R.id.select_item_type);
            buttonList.setText(listView.getItemAtPosition(position).toString());
            mItemType = position;
            mPopupItemType.dismiss();
        } else if (listView.getTag().equals(R.id.select_reference_button)) {
            Button buttonList = mBottomSheetDialog.findViewById(R.id.select_reference_button);
            buttonList.setText(listView.getItemAtPosition(position).toString());
            mReferencePosition = getListener().getReferenceList().indexOf(listView.getItemAtPosition(position));
            mPopupReference.dismiss();
        }
    }

}