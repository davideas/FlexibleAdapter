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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;

/**
 * Helper to coordinates the MULTI selection with FlexibleAdapter.
 *
 * @author Davide Steduto
 * @since 30/04/2016
 */
public class ActionModeHelper implements ActionMode.Callback {

	public static final String TAG = ActionModeHelper.class.getSimpleName();

	@Mode
	private int defaultMode = SelectableAdapter.MODE_IDLE;
	@MenuRes
	private int mCabMenu;
	private FlexibleAdapter mAdapter;
	private ActionMode.Callback mCallback;
	protected ActionMode mActionMode;

	/**
	 * Default constructor with internal callback.
	 *
	 * @param adapter the FlexibleAdapter instance
	 * @param cabMenu the menu resourceId
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
	 * @param adapter the FlexibleAdapter instance
	 * @param cabMenu the menu resourceId
	 * @param callback the custom {@link android.support.v7.view.ActionMode.Callback}
	 * @see #ActionModeHelper(FlexibleAdapter, int)
	 * @since 5.0.0-b6
	 */
	public ActionModeHelper(@NonNull FlexibleAdapter adapter, @MenuRes int cabMenu,
							@NonNull ActionMode.Callback callback) {
		this(adapter, cabMenu);
		this.mCallback = callback;
	}

	/**
	 * Changes the default mode to apply when the ActionMode is destroyed and normal selection is
	 * again active.
	 * <p>Default value is {@link SelectableAdapter#MODE_IDLE}.</p>
	 *
	 * @param defaultMode the new default mode when ActionMode is off, accepted values:
	 *                    {@code MODE_IDLE, MODE_SINGLE}
	 * @return this object, so it can be chained
	 * @since 5.0.0-b6
	 */
	public final ActionModeHelper withDefaultMode(@Mode int defaultMode) {
		if (defaultMode == SelectableAdapter.MODE_IDLE || defaultMode == SelectableAdapter.MODE_SINGLE)
			this.defaultMode = defaultMode;
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
		//Activate ActionMode
		if (mActionMode == null) {
			mActionMode = activity.startSupportActionMode(this);
		}
		//we have to select this on our own as we will consume the event
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
		if (position >= 0 && (mAdapter.getMode() == SelectableAdapter.MODE_SINGLE ||
				mAdapter.getMode() == SelectableAdapter.MODE_MULTI)) {
			mAdapter.toggleSelection(position);
		}
		//If MODE_SINGLE is active then ActionMode can be null
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
	 * Helper method to restart the action mode after a restoration of deleted items. The
	 * ActionMode will be activated only if {@link FlexibleAdapter#getSelectedItemCount()}
	 * has selection.
	 * <p>To be called in the <i>onUndo</i> method after the restoration is done or in the
	 * <i>onRestoreInstanceState</i>.</p>
	 *
	 * @param activity the current Activity
	 * @since 5.0.0-b6
	 */
	public void restoreSelection(AppCompatActivity activity) {
		if ((defaultMode == SelectableAdapter.MODE_IDLE && mAdapter.getSelectedItemCount() > 0) ||
				(defaultMode == SelectableAdapter.MODE_SINGLE && mAdapter.getSelectedItemCount() > 1)) {
			onLongClick(activity, -1);
		}
	}

	@CallSuper
	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		if (SelectableAdapter.DEBUG) Log.v(TAG, "ActionMode is active!");
		//Inflate the Context Menu
		actionMode.getMenuInflater().inflate(mCabMenu, menu);
		//Activate the ActionMode Multi
		mAdapter.setMode(SelectableAdapter.MODE_MULTI);
		//Notify the provided callback
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
			//Finish the actionMode
			actionMode.finish();
		}
		return consumed;
	}

	/**
	 * {@inheritDoc}
	 * With FlexibleAdapter v5.0.0 the default mode is {@link SelectableAdapter#MODE_IDLE}, if
	 * you want single selection enabled change default mode with {@link #withDefaultMode(int)}.
	 */
	@CallSuper
	@Override
	public void onDestroyActionMode(ActionMode actionMode) {
		if (SelectableAdapter.DEBUG)
			Log.v(TAG, "ActionMode is about to be destroyed! New mode will be " + defaultMode);
		//Change mode and deselect everything
		mAdapter.setMode(defaultMode);
		mAdapter.clearSelection();
		mActionMode = null;
		//Notify the provided callback
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

}