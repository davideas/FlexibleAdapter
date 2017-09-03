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
 * Helper to simplify the Undo operation with FlexibleAdapter.
 *
 * @author Davide Steduto
 * @since 30/04/2016
 */
@SuppressWarnings("WeakerAccess")
public class UndoHelper extends Snackbar.Callback implements FlexibleAdapter.OnDeleteCompleteListener {

    /**
     * Default undo timeout of 5''.
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
     * Annotation interface for Undo actions.
     */
    @IntDef({ACTION_REMOVE, ACTION_UPDATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action {
    }

    @Action
    private int mAction = ACTION_REMOVE;
    @ColorInt
    private int mActionTextColor = Color.TRANSPARENT;
    private boolean consecutive = false;
    private List<Integer> mPositions = null;
    private Object mPayload = null;
    private FlexibleAdapter mAdapter;
    private OnActionListener mActionListener;
    private OnUndoListener mUndoListener;
    private Snackbar mSnackbar;

    /**
     * Default constructor.
     * <p>By calling this constructor, {@link FlexibleAdapter#setPermanentDelete(boolean)}
     * is set {@code false} automatically.
     *
     * @param adapter      the instance of {@code FlexibleAdapter}
     * @param undoListener the callback for the Undo and Delete confirmation
     */
    public UndoHelper(FlexibleAdapter adapter, OnUndoListener undoListener) {
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
     * By default {@link UndoHelper#ACTION_REMOVE} is performed.
     *
     * @param action         the action, one of {@link UndoHelper#ACTION_REMOVE}, {@link UndoHelper#ACTION_UPDATE}
     * @param actionListener the listener for the custom action to perform before the deletion
     * @return this object, so it can be chained
     * @deprecated Action listener is deprecated, use {@link #withAction(int)}
     */
    @Deprecated
    public UndoHelper withAction(@Action int action, @NonNull OnActionListener actionListener) {
        this.mAction = action;
        this.mActionListener = actionListener;
        return this;
    }

    /**
     * By default {@link UndoHelper#ACTION_REMOVE} is performed.
     *
     * @param action the action, one of {@link UndoHelper#ACTION_REMOVE}, {@link UndoHelper#ACTION_UPDATE}
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
     * As {@link #remove(List, View, CharSequence, CharSequence, int)} but with String
     * resources instead of CharSequence.
     * @deprecated Renamed to {@link #start(List, View, int, int, int)}
     */
    @Deprecated
    public Snackbar remove(List<Integer> positions, @NonNull View mainView,
                           @StringRes int messageStringResId, @StringRes int actionStringResId,
                           @IntRange(from = -1) int duration) {
        return start(positions, mainView, messageStringResId, actionStringResId, duration);
    }

    /**
     * @deprecated Renamed to {@link #start(List, View, CharSequence, CharSequence, int)}
     */
    @Deprecated
    public Snackbar remove(List<Integer> positions, @NonNull View mainView,
                          CharSequence message, CharSequence actionText,
                          @IntRange(from = -1) int duration) {
        return start(positions, mainView, message, actionText, duration);
    }

    /**
     * As {@link #start(List, View, CharSequence, CharSequence, int)} but with String
     * resources instead of CharSequence.
     */
    public Snackbar start(List<Integer> positions, @NonNull View mainView,
                           @StringRes int messageStringResId, @StringRes int actionStringResId,
                           @IntRange(from = -1) int duration) {
        Context context = mainView.getContext();
        return remove(positions, mainView, context.getString(messageStringResId),
                context.getString(actionStringResId), duration);
    }

    /**
     * Performs the action on the specified positions and displays a SnackBar to Undo
     * the operation. To customize the UPDATE event, please set a custom listener with
     * {@link #withAction(int, OnActionListener)} method.
     * <p>By default the DELETE action will be performed.</p>
     *
     * @param positions  the position to delete or update
     * @param mainView   the view to find a parent from
     * @param message    the text to show. Can be formatted text
     * @param actionText the action text to display
     * @param duration   How long to display the message. Either {@link Snackbar#LENGTH_SHORT} or
     *                   {@link Snackbar#LENGTH_LONG} or any custom Integer.
     * @return The SnackBar instance
     * @see #remove(List, View, int, int, int)
     */
    @SuppressWarnings("WrongConstant")
    public Snackbar start(List<Integer> positions, @NonNull View mainView,
                           CharSequence message, CharSequence actionText,
                           @IntRange(from = -1) int duration) {
        Log.d("With %s", (mAction == ACTION_REMOVE ? "ACTION_REMOVE" : "ACTION_UPDATE"));
        this.mPositions = positions;
        if (!mAdapter.isPermanentDelete()) {
            mSnackbar = Snackbar.make(mainView, message, duration > 0 ? duration + 400 : duration)
                                .setAction(actionText, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (mUndoListener != null) {
                                            Log.v("onActionCanceled event=1");
                                            mUndoListener.onActionCanceled(mAction);
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
        mSnackbar.show();
        return mSnackbar;
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
        mAdapter.emptyBin();
        // Trigger manual dismiss event
        // Avoid circular calls!
        if (!mAdapter.isPermanentDelete() && mSnackbar.isShown()) {
            mSnackbar.dismiss(); //Note: dismiss is asynchronous!
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismissed(Snackbar snackbar, int event) {
        // Check if deletion has already been committed
        // Avoid circular calls!
        if (mAdapter == null || !mAdapter.isRestoreInTime()) return;
        switch (event) {
            case DISMISS_EVENT_SWIPE:
            case DISMISS_EVENT_MANUAL:
            case DISMISS_EVENT_TIMEOUT:
                onDeleteConfirmed(event);
                break;
            case DISMISS_EVENT_CONSECUTIVE:
                if (consecutive) onDeleteConfirmed(event);
                break;
            case DISMISS_EVENT_ACTION:
            default:
                break;
        }
        onDestroy(); //Clear memory
        Log.v("Snackbar dismissed with event=%s", event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShown(Snackbar snackbar) {
        // Remove selected items from Adapter list after SnackBar is shown
        mAdapter.removeItems(mPositions, mPayload);
        // We can already notify the callback only in case of permanent deletion
        if (mAdapter.isPermanentDelete() && mUndoListener != null)
            mUndoListener.onActionConfirmed(mAction, DISMISS_EVENT_MANUAL);
    }

    private void onDestroy() {
        if (mAdapter != null) {
            mAdapter.removeListener(FlexibleAdapter.OnDeleteCompleteListener.class);
        }
        mAdapter = null;
        mSnackbar = null;
        mPositions = null;
        mPayload = null;
        mActionListener = null;
        mUndoListener = null;
    }

    /**
     * Basic implementation of {@link OnActionListener} interface.
     * <p>Override the methods as your convenience.</p>
     *
     * @deprecated Use normal flow.
     */
    @Deprecated
    public static class SimpleActionListener implements OnActionListener {
        @Override
        public boolean onPreAction() {
            return false;
        }

        @Override
        public void onPostAction() {
        }
    }

    /**
     * @deprecated Use normal flow: PreAction can be simply moved <b>before</b> the undo
     * instantiation, as well as PostAction can be performed <b>after</b> the undo
     * instantiation. Execution flow is preserved, so no need of this callback.
     */
    @Deprecated
    public interface OnActionListener {
        /**
         * Performs the custom action before item deletion.
         *
         * @return true if action has been consumed and should stop the deletion, false to
         * continue with the deletion
         * @deprecated Move the code outside and <b>before</b> removal invocation
         */
        @Deprecated
        boolean onPreAction();

        /**
         * Performs custom action After items deletion. Useful to finish the action mode
         * and perform secondary custom actions.
         *
         * @deprecated Move the code outside and <b>after</b> removal invocation
         */
        @Deprecated
        void onPostAction();
    }

    /**
     * @since 30/04/2016 - Creation
     * <br>03/09/2017 - Refactoring
     */
    public interface OnUndoListener {
        /**
         * Called when Undo event is triggered. Perform custom action after restoration.
         * <p>Usually for a delete restoration you should call
         * {@link FlexibleAdapter#restoreDeletedItems()}.</p>
         *
         * @param action one of {@link UndoHelper#ACTION_REMOVE}, {@link UndoHelper#ACTION_UPDATE}
         */
        void onActionCanceled(@Action int action);

        /**
         * Called when Undo timeout is over and action must be committed in the user Database.
         * <p>Due to Java Generic, it's too complicated and not well manageable if we pass the
         * {@code List<T>} object.<br>
         * So, to get deleted items, use {@link FlexibleAdapter#getDeletedItems()} from the
         * implementation of this method.</p>
         *
         * @param action one of {@link UndoHelper#ACTION_REMOVE}, {@link UndoHelper#ACTION_UPDATE}
         * @param event  one of {@link Snackbar.Callback#DISMISS_EVENT_SWIPE},
         *               {@link Snackbar.Callback#DISMISS_EVENT_MANUAL},
         *               {@link Snackbar.Callback#DISMISS_EVENT_TIMEOUT},
         *               {@link Snackbar.Callback#DISMISS_EVENT_CONSECUTIVE}
         */
        void onActionConfirmed(@Action int action, int event);
    }

}