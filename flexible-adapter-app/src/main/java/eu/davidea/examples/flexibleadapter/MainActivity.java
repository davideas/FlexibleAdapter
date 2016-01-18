package eu.davidea.examples.flexibleadapter;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import eu.davidea.common.SimpleDividerItemDecoration;
import eu.davidea.examples.anim.SlideInRightAnimator;
import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SmoothScrollLinearLayoutManager;
import eu.davidea.utils.Utils;
import eu.davidea.viewholder.FlexibleViewHolder;

public class MainActivity extends AppCompatActivity implements
		ActionMode.Callback, EditItemDialog.OnEditItemListener,
		SearchView.OnQueryTextListener,
		FlexibleAdapter.OnUpdateListener,
		FlexibleAdapter.OnDeleteCompleteListener,
		FlexibleViewHolder.OnListItemClickListener {

	public static final String TAG = MainActivity.class.getSimpleName();

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The current activated item position.
	 */
	private static final int INVALID_POSITION = -1;
	private int mActivatedPosition = INVALID_POSITION;

	/**
	 * RecyclerView and related objects
	 */
	private RecyclerView mRecyclerView;
	private ExampleAdapter mAdapter;
	private ActionMode mActionMode;
	private Snackbar mSnackBar;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private final Handler mSwipeHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
		public boolean handleMessage(Message message) {
			switch (message.what) {
				case 0: //Stop
					mSwipeRefreshLayout.setRefreshing(false);
					mSwipeRefreshLayout.setEnabled(true);
					return true;
				case 1: //1 Start
					mSwipeRefreshLayout.setRefreshing(true);
					mSwipeRefreshLayout.setEnabled(false);
					return true;
				default:
					return false;
			}
		}
	});
	/**
	 * FAB
	 */
	private FloatingActionButton mFab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate");

		//Adapter & RecyclerView
		FlexibleAdapter.enableLogs(true);
		mAdapter = new ExampleAdapter(this, "example parameter for List1");
		mAdapter.setAnimateOnReverseScrolling(true);
		mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of views will not change as the data changes
		mRecyclerView.setItemAnimator(new SlideInRightAnimator());
		mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(
				ResourcesCompat.getDrawable(getResources(), R.drawable.divider, null)));

		//Add FastScroll to the RecyclerView

		mAdapter.setFastScroller((FastScroller) findViewById(R.id.fast_scroller), Utils.getColorAccent(this));
//		FastScroller fastScroller = (FastScroller) findViewById(R.id.fast_scroller);
//		fastScroller.setRecyclerView(mRecyclerView);
//		fastScroller.setViewsToUse(R.layout.fast_scroller, R.id.fast_scroller_bubble, R.id.fast_scroller_handle);

		//FAB
		mFab = (FloatingActionButton) findViewById(R.id.fab);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				destroyActionModeIfNeeded();

				for (int i = 0; i <= mAdapter.getItemCount() + 1; i++) {
					Item item = DatabaseService.newExampleItem(i);

					if (!DatabaseService.getInstance().getListById(null).contains(item)) {
						DatabaseService.getInstance().addItem(i, item);//This is the original list
						//TODO FOR YOU: Use userLearnedSelection from settings
						if (!DatabaseService.userLearnedSelection) {
							i++;//Fixing exampleAdapter for new position :-)
						}
						mAdapter.addItem(i, item);//Adapter's list is a copy, to animate the item you must call addItem on the new position

						Toast.makeText(MainActivity.this, "Added New " + item.getTitle(), Toast.LENGTH_SHORT).show();
						mRecyclerView.smoothScrollToPosition(i);
						break;
					}
				}
			}
		});

		//Update EmptyView (by default EmptyView is visible)
//		updateEmptyView();

		//SwipeToRefresh
		initializeSwipeToRefresh();

		//Restore previous state
		if (savedInstanceState != null) {
			//Selection
			mAdapter.onRestoreInstanceState(savedInstanceState);
			if (mAdapter.getSelectedItemCount() > 0) {
				mActionMode = startSupportActionMode(this);
				setContextTitle(mAdapter.getSelectedItemCount());
			}
			//Previously serialized activated item position
			if (savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
				setSelection(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	private void initializeSwipeToRefresh() {
		//Swipe down to force synchronize
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
		mSwipeRefreshLayout.setDistanceToTriggerSync(390);
		mSwipeRefreshLayout.setEnabled(true);
		mSwipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_purple, android.R.color.holo_blue_light,
				android.R.color.holo_green_light, android.R.color.holo_orange_light);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mAdapter.updateDataSet();
				mSwipeRefreshLayout.setEnabled(false);
				mSwipeHandler.sendEmptyMessageDelayed(0, 1000L);
				destroyActionModeIfNeeded();
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.v(TAG, "onSaveInstanceState start!");

		mAdapter.onSaveInstanceState(outState);

		if (mActivatedPosition != AdapterView.INVALID_POSITION) {
			//Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
			Log.d(TAG, STATE_ACTIVATED_POSITION + "=" + mActivatedPosition);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v(TAG, "onCreateOptionsMenu called!");
		getMenuInflater().inflate(R.menu.menu_main, menu);
		initSearchView(menu);
		return true;
	}

	private void initSearchView(final Menu menu) {
		//Associate searchable configuration with the SearchView
		Log.d(TAG, "onCreateOptionsMenu setup SearchView!");
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) MenuItemCompat
				.getActionView(menu.findItem(R.id.action_search));
		searchView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_FULLSCREEN);
		searchView.setQueryHint(getString(R.string.action_search));
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnQueryTextListener(this);
		searchView.setOnSearchClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.findItem(R.id.action_list_type).setVisible(false);
				menu.findItem(R.id.action_reverse).setVisible(false);
				menu.findItem(R.id.action_about).setVisible(false);
			}
		});
		searchView.setOnCloseListener(new SearchView.OnCloseListener() {
			@Override
			public boolean onClose() {
				menu.findItem(R.id.action_list_type).setVisible(true);
				menu.findItem(R.id.action_reverse).setVisible(true);
				menu.findItem(R.id.action_about).setVisible(true);
				return false;
			}
		});
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v(TAG, "onPrepareOptionsMenu called!");
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		//Has searchText?
		if (!mAdapter.hasSearchText()) {
			Log.d(TAG, "onPrepareOptionsMenu Clearing SearchView!");
			searchView.setIconified(true);// This also clears the text in SearchView widget
		} else {
			searchView.setQuery(mAdapter.getSearchText(), false);
			searchView.setIconified(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (!mAdapter.hasSearchText()
				|| !mAdapter.getSearchText().equalsIgnoreCase(newText)) {
			Log.d(TAG, "onQueryTextChange newText: " + newText);
			mAdapter.setSearchText(newText);
			//Filter the items and notify the change!
			mAdapter.updateDataSet();
		}

		if (mAdapter.hasSearchText()) {
			//mFab.setVisibility(View.GONE);
			ViewCompat.animate(mFab)
					.scaleX(0f)
					.scaleY(0f)
					.alpha(0f)
					.setDuration(100)
					.start();
		} else {
			//mFab.setVisibility(View.VISIBLE);
			ViewCompat.animate(mFab)
					.scaleX(1f)
					.scaleY(1f)
					.alpha(1f)
					.setDuration(100)
					.start();
		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		Log.v(TAG, "onQueryTextSubmit called!");
		return onQueryTextChange(query);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_about) {
			MessageDialog.newInstance(
					R.drawable.ic_info_grey600_24dp,
					getString(R.string.about_title),
					getString(R.string.about_body,
							Utils.getVersionName(this),
							Utils.getVersionCode(this)))
					.show(getFragmentManager(), MessageDialog.TAG);
			return true;
		} else if (id == R.id.action_list_type) {
			if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
				mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(this));
				item.setIcon(R.drawable.ic_view_grid_white_24dp);
				item.setTitle(R.string.grid_layout);
			} else {
				mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
				item.setIcon(R.drawable.ic_view_agenda_white_24dp);
				item.setTitle(R.string.linear_layout);
			}
		} else if (id == R.id.action_reverse) {
			if (mAdapter.isAnimateOnReverseScrolling()) {
				mAdapter.setAnimateOnReverseScrolling(false);
				item.setIcon(R.drawable.ic_sort_white_24dp);
				item.setTitle(R.string.reverse_scrolling);
			} else {
				mAdapter.setAnimateOnReverseScrolling(true);
				item.setIcon(R.drawable.ic_sort_descending_white_24dp);
				item.setTitle(R.string.forward_scrolling);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Handling RecyclerView when empty.
	 * <br/><br/>
	 * <b>Note:</b> The order how the 3 Views (RecyclerView, EmptyView, FastScroller)
	 * are placed in the Layout is important!
	 */
	@Override
	public void onUpdateEmptyView(int size) {
		FastScroller fastScroller = (FastScroller) findViewById(R.id.fast_scroller);
		TextView emptyView = (TextView) findViewById(R.id.empty);
		emptyView.setText(getString(R.string.no_items));
		if (!DatabaseService.userLearnedSelection && size == 1) size = 0;
		if (size > 0) {
			fastScroller.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
		} else {
			fastScroller.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
		}
	}

	//TODO: Include setActiveSelection in the library
	public void setSelection(final int position) {
		if (mAdapter.getMode() == FlexibleAdapter.MODE_SINGLE) {
			Log.v(TAG, "setSelection called!");

			setActivatedPosition(position);

			mRecyclerView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mRecyclerView.smoothScrollToPosition(position);
				}
			}, 1000L);
		}
	}

	private void setActivatedPosition(int position) {
		Log.d(TAG, "ItemList New mActivatedPosition=" + position);
		mActivatedPosition = position;
	}

	@Override
	public void onTitleModified(int position, String newTitle) {
		Item item = mAdapter.getItem(position);
		item.setTitle(newTitle);
		mAdapter.updateItem(position, item);
	}

	@Override
	public boolean onListItemClick(int position) {
		if (mActionMode != null && position != INVALID_POSITION) {
			toggleSelection(position);
			return true;
		} else {
			//Notify the active callbacks interface (the activity, if the
			//fragment is attached to one) that an item has been selected.
			if (mAdapter.getItemCount() > 0) {
				if (position != mActivatedPosition) setActivatedPosition(position);
				Item item = mAdapter.getItem(position);
				if (!item.hasSubItems()) {
					//TODO FOR YOU: call your custom Callback, for example mCallback.onItemSelected(item.getId());
					EditItemDialog.newInstance(item, position).show(getFragmentManager(), EditItemDialog.TAG);
				}
			}
			return false;
		}
	}

	@Override
	public void onListItemLongClick(int position) {
		Log.d(TAG, "onListItemLongClick on position " + position);
		if (mActionMode == null) {
			Log.d(TAG, "onListItemLongClick actionMode activated!");
			mActionMode = startSupportActionMode(this);
		}
		Toast.makeText(this, "ImageClick or LongClick on " + mAdapter.getItem(position).getTitle(), Toast.LENGTH_SHORT).show();
		toggleSelection(position);
	}

	/**
	 * Toggle the selection state of an item.<br/><br/>
	 * If the item was the last one in the selection and is unselected, the selection is stopped.
	 * Note that the selection must already be started (actionMode must not be null).
	 *
	 * @param position Position of the item to toggle the selection state
	 */
	private void toggleSelection(int position) {
		mAdapter.toggleSelection(position, false);

		int count = mAdapter.getSelectedItemCount();

		if (count == 0) {
			Log.d(TAG, "toggleSelection finish the actionMode");
			mActionMode.finish();
		} else {
			Log.d(TAG, "toggleSelection update title after selection count=" + count);
			setContextTitle(count);
			mActionMode.invalidate();
		}
	}

	private void setContextTitle(int count) {
		mActionMode.setTitle(String.valueOf(count) + " " + (count == 1 ?
				getString(R.string.action_selected_one) :
				getString(R.string.action_selected_many)));
	}

	@Override
	public void onDeleteConfirmed() {
		for (Item item : mAdapter.getDeletedItems()) {
			//Remove items from your Database. Example:
			Log.d(TAG, "Confirm removed " + item.getTitle());
			DatabaseService.getInstance().removeItem(item);
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		//Inflate the correct Menu
		int menuId = R.menu.menu_item_list_context;
		mode.getMenuInflater().inflate(menuId, menu);
		//Activate the ActionMode Multi
		mAdapter.setMode(ExampleAdapter.MODE_MULTI);
		if (Utils.hasMarshmallow()) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccentDark_light, this.getTheme()));
		} else {
			//noinspection deprecation
			getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccentDark_light));
		}
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_select_all:
				mAdapter.selectAll();
				setContextTitle(mAdapter.getSelectedItemCount());
				return true;
			case R.id.action_delete:
				//Build message before delete, for the Snackbar
				StringBuilder message = new StringBuilder();
				for (Integer pos : mAdapter.getSelectedItems()) {

					message.append(mAdapter.getItem(pos).getTitle());
					message.append(", ");
				}
				message.append(" ").append(getString(R.string.action_deleted));

				//SnackBar for Undo
				//noinspection ResourceType
				mSnackBar = Snackbar.make(findViewById(R.id.main_view), message, 7000)
						.setAction(R.string.undo, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mAdapter.restoreDeletedItems();
								mSwipeHandler.sendEmptyMessage(0);
							}
						});
				mSnackBar.show();

				//Remove selected items from Adapter list after message is shown
				mAdapter.removeItems(mAdapter.getSelectedItems(), true);
				mAdapter.startUndoTimer(7000L + 200L, this);//+200: Using SnackBar, user can still click on the action button while bar is dismissing for a fraction of time

				mSwipeHandler.sendEmptyMessage(1);
				mSwipeHandler.sendEmptyMessageDelayed(0, 7000L);
				mActionMode.finish();
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		Log.v(TAG, "onDestroyActionMode called!");
		mAdapter.setMode(ExampleAdapter.MODE_SINGLE);
		mAdapter.clearSelection();
		mActionMode = null;
		if (Utils.hasMarshmallow()) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark_light, this.getTheme()));
		} else {
			//noinspection deprecation
			getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark_light));
		}
	}

	/**
	 * Utility method called from MainActivity on BackPressed
	 *
	 * @return true if ActionMode was active (in case it is also terminated), false otherwise
	 */
	public boolean destroyActionModeIfNeeded() {
		if (mActionMode != null) {
			mActionMode.finish();
			return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		//If ActionMode is active, back key closes it
		if (destroyActionModeIfNeeded()) return;

		//Close the App
		DatabaseService.onDestroy();
		super.onBackPressed();
	}

}