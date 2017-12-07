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

import android.support.annotation.CallSuper;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;
import eu.davidea.flexibleadapter.utils.Log;

/**
 * Helper Class to coordinate the MULTI selection with FlexibleAdapter.
 *
 * @author Davide Steduto
 * @since 30/04/2016 Created
 * <br>23/08/2017 Option to disable swipe/drag capabilities
 */
public class ActionModeHelper implements ActionMode.Callback {

    @Mode
    private int defaultMode = Mode.IDLE;
    @MenuRes
    private int mCabMenu;
    private boolean disableSwipe, disableDrag,
            longPressDragDisabledByHelper, handleDragDisabledByHelper, swipeDisabledByHelper;
    private FlexibleAdapter mAdapter;
    private ActionMode.Callback mCallback;
    protected ActionMode mActionMode;

    /**
     * Default constructor with internal callback.
     *
     * @param adapter the FlexibleAdapter instance
     * @param cabMenu the Contextual Action Bar menu resourceId
     * @see #ActionModeHelper(FlexibleAdapter, int, ActionMode.Callback)
     * @since 5.0.0-b6
     */
    public ActionModeHelper(@NonNull FlexibleAdapter adapter, @MenuRes int cabMenu) {
        this.mAdapter = adapter;
        this.mCabMenu = cabMenu;
    }

    /**
     * Constructor with internal callback + custom callback.
     *
     * @param adapter  the FlexibleAdapter instance
     * @param cabMenu  the Contextual Action Bar menu resourceId
     * @param callback the custom {@link android.support.v7.view.ActionMode.Callback}
     * @see #ActionModeHelper(FlexibleAdapter, int)
     * @since 5.0.0-b6
     */
    public ActionModeHelper(@NonNull FlexibleAdapter adapter, @MenuRes int cabMenu,
                            @Nullable ActionMode.Callback callback) {
        this(adapter, cabMenu);
        this.mCallback = callback;
    }

    /**
     * Changes the default mode to apply when the ActionMode is destroyed and normal selection is
     * again active.
     * <p>Default value is {@link Mode#IDLE}.</p>
     *
     * @param defaultMode the new default mode when ActionMode is off, accepted values:
     *                    {@code IDLE, SINGLE}
     * @return this object, so it can be chained
     * @since 5.0.0-b6
     */
    public final ActionModeHelper withDefaultMode(@Mode int defaultMode) {
        if (defaultMode == Mode.IDLE || defaultMode == Mode.SINGLE)
            this.defaultMode = defaultMode;
        return this;
    }

    /**
     * Automatically disables LongPress drag and Handle drag capability when ActionMode is
     * activated and enable it again when ActionMode is destroyed.
     *
     * @param disableDrag true to disable the drag, false to maintain the drag during ActionMode
     * @return this object, so it can be chained
     * @since 5.0.0-rc3
     */
    public final ActionModeHelper disableDragOnActionMode(boolean disableDrag) {
        this.disableDrag = disableDrag;
        return this;
    }

    /**
     * Automatically disables Swipe capability when ActionMode is activated and enable it again
     * when ActionMode is destroyed.
     *
     * @param disableSwipe true to disable the swipe, false to maintain the swipe during ActionMode
     * @return this object, so it can be chained
     * @since 5.0.0-rc3
     */
    public final ActionModeHelper disableSwipeOnActionMode(boolean disableSwipe) {
        this.disableSwipe = disableSwipe;
        return this;
    }

    /**
     * @return the current instance of the ActionMode, {@code null} if ActionMode is off.
     * @since 5.0.0-b6
     */
    public ActionMode getActionMode() {
        return mActionMode;
    }

    /**
     * Gets the activated position only when mode is {@code SINGLE}.
     *
     * @return the activated position when {@code SINGLE}. -1 if no item is selected
     * @since 5.0.0-rc1
     */
    public int getActivatedPosition() {
        List<Integer> selectedPositions = mAdapter.getSelectedPositions();
        if (mAdapter.getMode() == Mode.SINGLE && selectedPositions.size() == 1) {
            return selectedPositions.get(0);
        }
        return RecyclerView.NO_POSITION;
    }

    /**
     * Implements the basic behavior of a CAB and multi select behavior.
     *
     * @param position the current item position
     * @return true if selection is changed, false if the click event should ignore the ActionMode
     * and continue
     * @since 5.0.0-b6
     */
    public boolean onClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            toggleSelection(position);
            return true;
        }
        return false;
    }

    /**
     * Implements the basic behavior of a CAB and multi select behavior onLongClick.
     *
     * @param activity the current Activity
     * @param position the position of the clicked item
     * @return the initialized ActionMode or null if nothing was done
     * @since 5.0.0-b6
     */
    @NonNull
    public ActionMode onLongClick(AppCompatActivity activity, int position) {
        // Activate ActionMode
        if (mActionMode == null) {
            mActionMode = activity.startSupportActionMode(this);
        }
        // We have to select this on our own as we will consume the event
        toggleSelection(position);
        return mActionMode;
    }

    /**
     * Toggle the selection state of an item.
     * <p>If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).</p>
     *
     * @param position position of the item to toggle the selection state
     * @since 5.0.0-b6
     */
    public void toggleSelection(int position) {
        if (position >= 0 && (
                (mAdapter.getMode() == Mode.SINGLE && !mAdapter.isSelected(position)) ||
                        mAdapter.getMode() == Mode.MULTI)) {
            mAdapter.toggleSelection(position);
        }
        // If SINGLE is active then ActionMode can be null
        if (mActionMode == null) return;

        int count = mAdapter.getSelectedItemCount();
        if (count == 0) {
            mActionMode.finish();
        } else {
            updateContextTitle(count);
        }
    }

    /**
     * Updates the title of the Context Menu.
     * <p>Override to customize the title and subtitle.</p>
     *
     * @param count the current number of selected items
     * @since 5.0.0-b6
     */
    public void updateContextTitle(int count) {
        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(count));
        }
    }

    /**
     * Helper method to restart the action mode after a restoration of deleted items and after
     * screen rotation. The ActionMode will be activated only if
     * {@link FlexibleAdapter#getSelectedItemCount()} has selections.
     * <p>To be called in the {@code onUndo} method after the restoration is done or at the end
     * of {@code onRestoreInstanceState}.</p>
     *
     * @param activity the current Activity
     * @since 5.0.0-b6
     */
    public void restoreSelection(AppCompatActivity activity) {
        if ((defaultMode == Mode.IDLE && mAdapter.getSelectedItemCount() > 0) ||
                (defaultMode == Mode.SINGLE && mAdapter.getSelectedItemCount() > 1)) {
            onLongClick(activity, -1);
        }
    }

    @CallSuper
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate the Context Menu
        actionMode.getMenuInflater().inflate(mCabMenu, menu);
        Log.d("ActionMode is active!");
        // Activate the ActionMode Multi
        mAdapter.setMode(Mode.MULTI);
        // Disable Swipe and Drag capabilities as per settings
        disableSwipeDragCapabilities();
        // Notify the provided callback
        return mCallback == null || mCallback.onCreateActionMode(actionMode, menu);
    }

    @CallSuper
    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return mCallback != null && mCallback.onPrepareActionMode(actionMode, menu);
    }

    @CallSuper
    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
        boolean consumed = false;
        if (mCallback != null) {
            consumed = mCallback.onActionItemClicked(actionMode, item);
        }
        if (!consumed) {
            // Finish the actionMode
            actionMode.finish();
        }
        return consumed;
    }

    /**
     * {@inheritDoc}
     * With FlexibleAdapter v5.0.0 the default mode is {@link Mode#IDLE}, if
     * you want single selection enabled change default mode with {@link #withDefaultMode(int)}.
     */
    @CallSuper
    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        Log.d("ActionMode is about to be destroyed!");
        // Change mode and deselect everything
        mAdapter.setMode(defaultMode);
        mAdapter.clearSelection();
        mActionMode = null;
        // Re-enable Swipe and Drag capabilities if they were disabled by this helper
        enableSwipeDragCapabilities();
        // Notify the provided callback
        if (mCallback != null) {
            mCallback.onDestroyActionMode(actionMode);
        }
    }

    /**
     * Utility method to be called from Activity in many occasions such as: <i>onBackPressed</i>,
     * <i>onRefresh</i> for SwipeRefreshLayout, after <i>deleting</i> all selected items.
     *
     * @return true if ActionMode was active (in case it is also terminated), false otherwise
     * @since 5.0.0-b6
     */
    public boolean destroyActionModeIfCan() {
        if (mActionMode != null) {
            mActionMode.finish();
            return true;
        }
        return false;
    }

    private void enableSwipeDragCapabilities() {
        if (longPressDragDisabledByHelper) {
            longPressDragDisabledByHelper = false;
            mAdapter.setLongPressDragEnabled(true);
        }
        if (handleDragDisabledByHelper) {
            handleDragDisabledByHelper = false;
            mAdapter.setHandleDragEnabled(true);
        }
        if (swipeDisabledByHelper) {
            swipeDisabledByHelper = false;
            mAdapter.setSwipeEnabled(true);
        }
    }

    private void disableSwipeDragCapabilities() {
        if (disableDrag && mAdapter.isLongPressDragEnabled()) {
            longPressDragDisabledByHelper = true;
            mAdapter.setLongPressDragEnabled(false);
        }
        if (disableDrag && mAdapter.isHandleDragEnabled()) {
            handleDragDisabledByHelper = true;
            mAdapter.setHandleDragEnabled(false);
        }
        if (disableSwipe && mAdapter.isSwipeEnabled()) {
            swipeDisabledByHelper = true;
            mAdapter.setSwipeEnabled(false);
        }
    }

}