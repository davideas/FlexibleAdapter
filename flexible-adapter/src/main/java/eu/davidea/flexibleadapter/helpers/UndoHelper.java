/*
 * Copyright 2016 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.flexibleadapter.helpers;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * @author Davide Steduto
 * @since 30/04/2016
 */
public class UndoHelper extends Snackbar.Callback {

	/**
	 * Default undo-timeout of 5''.
	 */
	public static final int UNDO_TIMEOUT = 5000;
	/**
	 * Indicates that the Confirmation Listener (Undo and Delete) will perform a deletion.
	 */
	public static final int ACTION_REMOVE = 0;
	/**
	 * Indicates that the Confirmation Listener (Undo and Delete) will perform an update.
	 */
	public static final int ACTION_UPDATE = 1;

	/**
	 * @hide
	 */
	@IntDef({ACTION_REMOVE, ACTION_UPDATE})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Action {
	}

	@Action
	private int mAction;
	private FlexibleAdapter mAdapter;
	private List<Integer> mPositions = null;
	private Object mPayload = null;
	private OnUndoListener mUndoListener;

	/**
	 * Basic constructor.
	 *
	 * @param adapter      the instance of {@code FlexibleAdapter}
	 * @param undoListener the callback for the Undo and Delete confirmation
	 */
	public UndoHelper(FlexibleAdapter adapter, OnUndoListener undoListener) {
		this.mAdapter = adapter;
		this.mUndoListener = undoListener;
	}

	/**
	 * Sets the payload to inform other linked items about the change in action.
	 *
	 * @param payload any non-null user object to notify the parent (the payload will be
	 *                therefore passed to the bind method of the parent ViewHolder),
	 *                pass null to <u>not</u> notify the parent
	 * @return this
	 */
	public UndoHelper withPayload(Object payload) {
		this.mPayload = payload;
		return this;
	}

	/**
	 * As {@link #remove(List, int, View, CharSequence, CharSequence, int)} but with String
	 * resources instead of CharSequence.
	 */
	public Snackbar remove(List<Integer> positions, @Action int action, @NonNull View mainView,
						   @StringRes int messageStringResId, @StringRes int actionStringResId,
						   @IntRange(from = 0) int undoTime) {
		Context context = mainView.getContext();
		return remove(positions, action, mainView, context.getString(messageStringResId),
				context.getString(actionStringResId), undoTime);
	}

	/**
	 * Performs the delete action on the specified positions and display a SnackBar to Undo
	 * the operation. To customize the UPDATE event, please override the
	 * {@link #onShown(Snackbar)} method.
	 * <p>By default the DELETE action will be performed.</p>
	 *
	 * @param positions  the position to delete or update
	 * @param action     the action, one of {@link UndoHelper#ACTION_REMOVE}, {@link UndoHelper#ACTION_UPDATE}
	 * @param mainView   the view to find a parent from
	 * @param message    the text to show. Can be formatted text
	 * @param actionText the action text to display
	 * @param undoTime   How long to display the message. Either {@link Snackbar#LENGTH_SHORT} or
	 *                   {@link Snackbar#LENGTH_LONG} or any custom Integer.
	 * @return The SnackBar instance to be customized again
	 * @see #remove(List, int, View, int, int, int)
	 */
	public Snackbar remove(List<Integer> positions, @Action int action, @NonNull View mainView,
						   CharSequence message, CharSequence actionText,
						   @IntRange(from = 0) int undoTime) {
		this.mPositions = positions;
		this.mAction = action;
		Snackbar snackbar;
		if (!mAdapter.isPermanentDelete()) {
			snackbar = Snackbar.make(mainView, message, undoTime + 400)//More time due to the animation
					.setCallback(this)
					.setAction(actionText, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (mUndoListener != null)
								mUndoListener.onUndoConfirmed(mAction);
						}
					});
			snackbar.show();
			return snackbar;
		} else {
			snackbar = Snackbar.make(mainView, message, undoTime)
					.setCallback(this);
			snackbar.show();
			return snackbar;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDismissed(Snackbar snackbar, int event) {
		if (mAdapter.isPermanentDelete()) return;
		switch (event) {
			case DISMISS_EVENT_ACTION:
				//We ignore it, action is performed already
				break;
			case DISMISS_EVENT_SWIPE:
			case DISMISS_EVENT_MANUAL:
			case DISMISS_EVENT_TIMEOUT:
				if (mUndoListener != null)
					mUndoListener.onDeleteConfirmed(mAction);
				mAdapter.emptyBin();
				break;
		}
	}

	/**
	 * Override to customize the UPDATE action.
	 * <p>By default, it performs the DELETE action.</p>
	 * {@inheritDoc}
	 *
	 * @param snackbar the SnackBar produced
	 */
	@Override
	public void onShown(Snackbar snackbar) {
		//Remove selected items from Adapter list after message is shown
		mAdapter.removeItems(mPositions, mPayload);
		if (mAdapter.isPermanentDelete() && mUndoListener != null)
			mUndoListener.onDeleteConfirmed(mAction);
	}

	/**
	 * @since 30/04/2016
	 */
	public interface OnUndoListener {
		/**
		 * Called when Undo event is triggered. Perform custom action after restoration.
		 * <p>Usually for a delete restoration you should call
		 * {@link FlexibleAdapter#restoreDeletedItems()}.</p>
		 *
		 * @param action one of {@link UndoHelper#ACTION_REMOVE}, {@link UndoHelper#ACTION_UPDATE}
		 */
		void onUndoConfirmed(int action);

		/**
		 * Called when Undo timeout is over and action must be committed in the user Database.
		 * <p>Due to Java Generic, it's too complicated and not well manageable if we pass the
		 * List&lt;T&gt; object.<br/>
		 * To get deleted items, use {@link FlexibleAdapter#getDeletedItems()} from the
		 * implementation of this method.</p>
		 *
		 * @param action one of {@link UndoHelper#ACTION_REMOVE}, {@link UndoHelper#ACTION_UPDATE}
		 */
		void onDeleteConfirmed(int action);
	}

}