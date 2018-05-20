/*
 * Copyright 2016-2017 Davide Steduto
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
import android.graphics.Color;
import android.support.annotation.ColorInt;
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
import eu.davidea.flexibleadapter.utils.Log;

/**
 * Helper Class to simplify the Undo operation with FlexibleAdapter.
 *
 * @author Davide Steduto
 * @since 30/04/2016 Created in main package
 * <br>07/12/2017 Better use of OnActionListener
 * <br>17/12/2017 Moved into UI package
 */
@SuppressWarnings("WeakerAccess")
public class UndoHelper extends Snackbar.Callback implements FlexibleAdapter.OnDeleteCompleteListener {

    /**
     * Default undo timeout of 5''.
     */
    public static final int UNDO_TIMEOUT = 5000;

    /**
     * Annotation interface for Undo actions.
     */
    @IntDef({Action.REMOVE, Action.UPDATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action {
        /**
         * Indicates that the Action Listener for confirmation will perform a deletion.
         */
        int REMOVE = 0;
        /**
         * Indicates that the Action Listener for cancellation will perform an update (user responsibility)
         * without removing items.
         */
        int UPDATE = 1;
    }

    @Action
    private int mAction = Action.REMOVE;
    @ColorInt
    private int mActionTextColor = Color.TRANSPARENT;
    private boolean consecutive = false;
    private List<Integer> mPositions = null;
    private Object mPayload = null;
    private FlexibleAdapter<?> mAdapter;
    private OnActionListener mUndoListener;
    private Snackbar mSnackbar;

    /**
     * Default constructor.
     * <p>By calling this constructor, {@link FlexibleAdapter#setPermanentDelete(boolean)}
     * is set {@code false} automatically.
     *
     * @param adapter      the instance of {@code FlexibleAdapter}
     * @param undoListener the callback for the Undo and Delete confirmation
     */
    public UndoHelper(FlexibleAdapter adapter, OnActionListener undoListener) {
        this.mAdapter = adapter;
        this.mAdapter.addListener(this);
        this.mUndoListener = undoListener;
    }

    /**
     * Sets the payload to inform other linked items about the change in action.
     *
     * @param payload any non-null user object to notify the parent (the payload will be
     *                therefore passed to the bind method of the parent ViewHolder),
     *                pass null to <u>not</u> notify the parent
     * @return this object, so it can be chained
     */
    public UndoHelper withPayload(Object payload) {
        if (payload != null) Log.d("With payload");
        this.mPayload = payload;
        return this;
    }

    /**
     * By default {@link UndoHelper.Action#REMOVE} is performed.
     *
     * @param action the action, one of {@link UndoHelper.Action#REMOVE}, {@link UndoHelper.Action#UPDATE}
     * @return this object, so it can be chained
     */
    public UndoHelper withAction(@Action int action) {
        this.mAction = action;
        return this;
    }

    /**
     * Sets the text color of the action.
     *
     * @param color the color for the action button
     * @return this object, so it can be chained
     */
    public UndoHelper withActionTextColor(@ColorInt int color) {
        Log.d("With customActionTextColor");
        this.mActionTextColor = color;
        return this;
    }

    /**
     * Allows to commit previous action before next consecutive Undo request.
     * <p>Default value is {@code false} (accumulate items).</p>
     *
     * @param consecutive true to commit deletion at each Undo request to undo last action,
     *                    false to accumulate the deleted items and Undo all in one shot.
     * @return this object, so it can be chained
     */
    public UndoHelper withConsecutive(boolean consecutive) {
        Log.d("With consecutive=%s", consecutive);
        this.consecutive = consecutive;
        return this;
    }

    /**
     * As {@link #start(List, View, CharSequence, CharSequence, int)} but with String
     * resources instead of CharSequence.
     */
    public Snackbar start(List<Integer> positions, @NonNull View mainView,
                           @StringRes int messageStringResId, @StringRes int actionStringResId,
                           @IntRange(from = -1) int duration) {
        Context context = mainView.getContext();
        return start(positions, mainView, context.getString(messageStringResId),
                context.getString(actionStringResId), duration);
    }

    /**
     * Performs the action on the specified positions and displays a SnackBar to Undo
     * the operation. To customize the UPDATE event, please set a custom listener with
     * {@link #withAction(int)} method.
     * <p>By default the DELETE action will be performed.</p>
     *
     * @param positions  the position to delete or update
     * @param mainView   the view to find a parent from
     * @param message    the text to show. Can be formatted text
     * @param actionText the action text to display
     * @param duration   How long to display the message. Either {@link Snackbar#LENGTH_SHORT} or
     *                   {@link Snackbar#LENGTH_LONG} or any custom Integer.
     * @return The SnackBar instance
     * @see #start(List, View, int, int, int)
     */
    @SuppressWarnings("WrongConstant")
    public Snackbar start(List<Integer> positions, @NonNull View mainView,
                           CharSequence message, CharSequence actionText,
                           @IntRange(from = -1) int duration) {
        Log.d("With %s", (mAction == Action.REMOVE ? "ACTION_REMOVE" : "ACTION_UPDATE"));
        this.mPositions = positions;
        if (!mAdapter.isPermanentDelete()) {
            mSnackbar = Snackbar.make(mainView, message, duration > 0 ? duration + 400 : duration)
                                .setAction(actionText, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (mUndoListener != null) {
                                            Log.v("onActionCanceled event=1");
                                            mUndoListener.onActionCanceled(mAction, mAdapter.getUndoPositions());
                                            mAdapter.emptyBin();
                                        }
                                    }
                                });
            if (mActionTextColor != Color.TRANSPARENT) {
                mSnackbar.setActionTextColor(mActionTextColor);
            }
        } else {
            mSnackbar = Snackbar.make(mainView, message, duration);
        }
        mSnackbar.addCallback(this);
        mSnackbar.show(); // Note: show is asynchronous!
        // Early perform action and eventually clear previous action
        performAction();
        return mSnackbar;
    }

    private void performAction() {
        // Clear previous action if exists
        if (consecutive && mAdapter.isRestoreInTime()) {
            onDeleteConfirmed(DISMISS_EVENT_CONSECUTIVE);
        }
        // Remove selected items from Adapter list before SnackBar is shown
        // and if action is REMOVE.
        switch (mAction) {
            case Action.REMOVE:
                mAdapter.removeItems(mPositions, mPayload);
                break;
            case Action.UPDATE:
                mAdapter.saveUndoPositions(mPositions);
                break;
        }
        // We can already notify the callback only in case of permanent deletion
        if (mAdapter.isPermanentDelete() && mUndoListener != null)
            mUndoListener.onActionConfirmed(mAction, DISMISS_EVENT_MANUAL);
    }

    /**
     * {@inheritDoc}
     * <p><b>Note:</b> This method is also invoked by {@link FlexibleAdapter#filterItems(List)}
     * before applying the filter.</p>
     */
    @Override
    public void onDeleteConfirmed(int event) {
        if (mUndoListener != null) {
            Log.v("onActionConfirmed event=%s", event);
            mUndoListener.onActionConfirmed(mAction, event);
        }
        mAdapter.confirmDeletion();
        // Trigger manual dismiss event
        // Avoid circular calls!
        if (mSnackbar.isShown() && (mAction == Action.REMOVE && !mAdapter.isRestoreInTime())) {
            mSnackbar.dismiss(); // Note: dismiss is asynchronous!
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismissed(Snackbar snackbar, int event) {
        // Check if deletion has already been committed
        // Avoid circular calls!
        if (mAdapter == null || (mAction == Action.REMOVE && !mAdapter.isRestoreInTime())) {
            return;
        }
        switch (event) {
            case DISMISS_EVENT_SWIPE:
            case DISMISS_EVENT_MANUAL:
            case DISMISS_EVENT_TIMEOUT:
                onDeleteConfirmed(event);
                break;
            case DISMISS_EVENT_CONSECUTIVE:
            case DISMISS_EVENT_ACTION:
            default:
                break;
        }
        onDestroy(); // Clear memory
        Log.v("Snackbar dismissed with event=%s", event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShown(Snackbar snackbar) {
        // Too slow this callback for consecutive action: we need to call performAction() earlier!
    }

    private void onDestroy() {
        if (mAdapter != null) {
            mAdapter.removeListener(this);
        }
        mAdapter = null;
        mSnackbar = null;
        mPositions = null;
        mPayload = null;
        mUndoListener = null;
    }

    /**
     * @since 30/04/2016 Creation in main package
     * <br>03/09/2017 Refactoring methods
     * <br>06/12/2017 Refactoring class name and methods
     */
    public interface OnActionListener {
        /**
         * Called when Undo event is triggered. Perform custom action after restoration.
         * <p>Usually for a delete restoration you should call
         * {@link FlexibleAdapter#restoreDeletedItems()}.</p>
         *
         * @param action one of {@link UndoHelper.Action#REMOVE}, {@link UndoHelper.Action#UPDATE}
         * @param positions positions affected
         */
        void onActionCanceled(@Action int action, List<Integer> positions);

        /**
         * Called when Undo timeout is over and action must be committed in the user Database.
         * <p>Due to Java Generic, it's too complicated and not well manageable if we pass the
         * {@code List<T>} object.<br>
         * So, to get deleted items, use {@link FlexibleAdapter#getDeletedItems()} from the
         * implementation of this method.</p>
         *
         * @param action one of {@link UndoHelper.Action#REMOVE}, {@link UndoHelper.Action#UPDATE}
         * @param event  one of {@link Snackbar.Callback#DISMISS_EVENT_SWIPE},
         *               {@link Snackbar.Callback#DISMISS_EVENT_MANUAL},
         *               {@link Snackbar.Callback#DISMISS_EVENT_TIMEOUT},
         *               {@link Snackbar.Callback#DISMISS_EVENT_CONSECUTIVE}
         */
        void onActionConfirmed(@Action int action, int event);
    }

}