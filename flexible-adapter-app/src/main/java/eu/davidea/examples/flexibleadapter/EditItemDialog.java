package eu.davidea.examples.flexibleadapter;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import eu.davidea.common.SimpleTextWatcher;
import eu.davidea.examples.models.Item;
import eu.davidea.utils.Utils;


public class EditItemDialog extends DialogFragment {

	public static final String TAG = EditItemDialog.class.getSimpleName();
	public static final String ARG_ITEM = "item";
	public static final String ARG_ITEM_POSITION = "position";

	public interface OnEditItemListener {
		void onTitleModified(int position, String newTitle);
	}

	private Item mItem;
	private int mPosition;

	public EditItemDialog() {
	}

	public static EditItemDialog newInstance(Item item, int position) {
		return newInstance(item, position, null);
	}

	public static EditItemDialog newInstance(Item item, int position, Fragment fragment) {
		EditItemDialog dialog = new EditItemDialog();
		Bundle args = new Bundle();
		args.putSerializable(ARG_ITEM, item);
		args.putInt(ARG_ITEM_POSITION, position);
		dialog.setArguments(args);
		dialog.setArguments(args);
		dialog.setTargetFragment(fragment, 0);
		return dialog;
	}

	private OnEditItemListener getListener() {
		OnEditItemListener listener = (OnEditItemListener) getTargetFragment();
		if (listener == null) {
			listener = (OnEditItemListener) getActivity();
		}
		return listener;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(ARG_ITEM, mItem);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		getDialog().getWindow().setWindowAnimations(R.style.animation_slide_from_right);
	}

	@SuppressLint({"InflateParams", "HandlerLeak"})
	@Override
	public AlertDialog onCreateDialog(Bundle savedInstanceState) {
		//Pick up bundle parameters
		Bundle bundle;
		if (savedInstanceState == null) {
			bundle = getArguments();
		} else {
			bundle = savedInstanceState;
		}

		mItem = (Item) bundle.getSerializable(ARG_ITEM);
		mPosition = bundle.getInt(ARG_ITEM_POSITION);

		//Inflate custom view
		View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_item, null);

		final EditText editText = (EditText) dialogView.findViewById(R.id.text_edit_title);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//, R.style.AppTheme_AlertDialog);
		builder.setTitle(R.string.dialog_edit_title)
				.setView(dialogView)
				.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.hideSoftInputFrom(getActivity(), editText);
						dialog.dismiss();
					}
				})
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getListener().onTitleModified(mPosition,
								editText.getText().toString().trim());
						Utils.hideSoftInputFrom(getActivity(), editText);
						dialog.dismiss();
					}
				});

		final AlertDialog editDialog = builder.create();

		editDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				updateOkButtonState(editDialog, null);
			}
		});

		if (mItem != null) {
			editText.setText(mItem.getTitle());
			editText.selectAll();
		}
		editText.requestFocus();
		editText.addTextChangedListener(new SimpleTextWatcher() {
			private final static long DELAY = 400L;
			private final static int TRIGGER = 1;

			private Handler mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (msg.what == TRIGGER) {
						updateOkButtonState(editDialog, editText);
					}
				}
			};

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				updateOkButtonState(editDialog, null);
			}

			@Override
			public void afterTextChanged(Editable s) {
				mHandler.removeMessages(TRIGGER);
				mHandler.sendEmptyMessageDelayed(TRIGGER, DELAY);
			}
		});

		editDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		return editDialog;
	}

	private void updateOkButtonState(AlertDialog dialog, EditText editText) {
		Button buttonOK = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		if (editText == null || (editText.getText().toString().trim()).length() == 0) {
			buttonOK.setEnabled(false);
			return;
		}
		if (mItem != null && !mItem.getTitle().equalsIgnoreCase(editText.getText().toString().trim())) {
			buttonOK.setEnabled(true);
		} else {
			editText.setError(getActivity().getString(R.string.err_no_edit));
			buttonOK.setEnabled(false);
		}

		return;
	}

}