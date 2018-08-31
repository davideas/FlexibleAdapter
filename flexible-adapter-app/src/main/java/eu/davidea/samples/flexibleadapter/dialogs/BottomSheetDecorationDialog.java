package eu.davidea.samples.flexibleadapter.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import eu.davidea.flexibleadapter.common.FlexibleItemDecoration;
import eu.davidea.samples.flexibleadapter.R;

/**
 * @author Davide Steduto
 * @since 08/06/2017
 */
@SuppressWarnings("ConstantConditions")
public class BottomSheetDecorationDialog extends android.support.design.widget.BottomSheetDialogFragment
        implements CompoundButton.OnCheckedChangeListener {

    public static final String TAG = BottomSheetDecorationDialog.class.getSimpleName();
    public static final String ARG_LAYOUT = "layout";
    private static final int MAX_OFFSET = 64; //dpi
    private static final int STEP_INCREMENT = 2; //dpi


    private android.support.design.widget.BottomSheetDialog mBottomSheetDialog;
    private SeekBar mSeekBar;
    private OnDecorationSelectedListener mListener;

    public BottomSheetDecorationDialog() {
    }

    /**
     * Use from Activities.
     *
     * @param layoutResId custom layout to use for the bottom sheet
     * @return a new instance of BottomSheetSectionDialog
     */
    public static BottomSheetDecorationDialog newInstance(@LayoutRes int layoutResId) {
        return newInstance(layoutResId, null);
    }

    /**
     * Use from Fragments.
     *
     * @param layoutResId custom layout to use for the bottom sheet
     * @param fragment    target fragment
     * @return a new instance of BottomSheetSectionDialog
     */
    public static BottomSheetDecorationDialog newInstance(@LayoutRes int layoutResId, @Nullable Fragment fragment) {
        BottomSheetDecorationDialog bottomSheetFragment = new BottomSheetDecorationDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT, layoutResId);
        bottomSheetFragment.setArguments(args);
        if (fragment != null) bottomSheetFragment.setTargetFragment(fragment, 0);
        return bottomSheetFragment;
    }

    private void setListener() {
        mListener = (OnDecorationSelectedListener) getTargetFragment();
        if (mListener == null && getActivity() instanceof OnParameterSelectedListener) {
            mListener = (OnDecorationSelectedListener) getActivity();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mBottomSheetDialog = new android.support.design.widget.BottomSheetDialog(getActivity(), R.style.AppTheme_BottomSheetDialog);
        mBottomSheetDialog.setContentView(getArguments().getInt(ARG_LAYOUT));

        setListener();
        configureEdges();
        configureDividers();

        return mBottomSheetDialog;
    }

    private void configureEdges() {
        ((Switch) mBottomSheetDialog.findViewById(R.id.switch_edge_left)).setOnCheckedChangeListener(this);
        ((Switch) mBottomSheetDialog.findViewById(R.id.switch_edge_top)).setOnCheckedChangeListener(this);
        ((Switch) mBottomSheetDialog.findViewById(R.id.switch_edge_right)).setOnCheckedChangeListener(this);
        ((Switch) mBottomSheetDialog.findViewById(R.id.switch_edge_bottom)).setOnCheckedChangeListener(this);
    }

    private void configureDividers() {
        ((Switch) mBottomSheetDialog.findViewById(R.id.switch_default_divider)).setOnCheckedChangeListener(this);
        ((Switch) mBottomSheetDialog.findViewById(R.id.switch_custom_divider)).setOnCheckedChangeListener(this);
        ((Switch) mBottomSheetDialog.findViewById(R.id.switch_draw_over)).setOnCheckedChangeListener(this);
    }

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        FlexibleItemDecoration itemDecoration = mListener.getItemDecoration();
        if (buttonView.getId() == R.id.switch_edge_left) {
            itemDecoration.withLeftEdge(isChecked);

        } else if (buttonView.getId() == R.id.switch_edge_top) {
            itemDecoration.withTopEdge(isChecked);

        } else if (buttonView.getId() == R.id.switch_edge_right) {
            itemDecoration.withRightEdge(isChecked);

        } else if (buttonView.getId() == R.id.switch_edge_bottom) {
            itemDecoration.withBottomEdge(isChecked);

        } else if (buttonView.getId() == R.id.switch_default_divider) {
            itemDecoration.removeDivider();
            if (isChecked) itemDecoration.withDefaultDivider();

        } else if (buttonView.getId() == R.id.switch_custom_divider) {
            itemDecoration.removeDivider();
            if (isChecked) itemDecoration.withDivider(R.drawable.divider_large);

        } else if (buttonView.getId() == R.id.switch_draw_over) {
            itemDecoration.withDrawOver(isChecked);
        }

        mListener.onDecorationSelected();
    }
}