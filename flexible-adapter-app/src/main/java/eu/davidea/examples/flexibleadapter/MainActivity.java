package eu.davidea.examples.flexibleadapter;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
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

import eu.davidea.flexibleadapter.common.DividerItemDecoration;
import eu.davidea.examples.models.AbstractExampleItem;
import eu.davidea.examples.models.ExpandableItem;
import eu.davidea.examples.models.HeaderItem;
import eu.davidea.examples.models.SimpleItem;
import eu.davidea.examples.models.SubItem;
import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flipview.FlipView;
import eu.davidea.utils.Utils;

public class MainActivity extends AppCompatActivity implements
		ActionMode.Callback, EditItemDialog.OnEditItemListener, SearchView.OnQueryTextListener,
		FlexibleAdapter.OnUpdateListener, FlexibleAdapter.OnDeleteCompleteListener,
		FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnItemLongClickListener,
		FlexibleAdapter.OnItemMoveListener, FlexibleAdapter.OnItemSwipeListener {

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
	private SearchView mSearchView;
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

		//Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		//Adapter & RecyclerView
		FlexibleAdapter.enableLogs(true);
		mAdapter = new ExampleAdapter(this);
		//Experimenting NEW features
		mAdapter.setAnimationOnScrolling(true);
		mAdapter.setAnimationOnReverseScrolling(true);
		mAdapter.setAutoCollapseOnExpand(false);
		mAdapter.setAutoScrollOnExpand(true);
		mAdapter.setRemoveOrphanHeaders(false);
		mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		mRecyclerView.setItemAnimator(new DefaultItemAnimator() {
			@Override
			public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
				//NOTE: This allows to receive Payload objects on notifyItemChanged called by the Adapter!!!
				return true;
			}
		});
		//mRecyclerView.setItemAnimator(new SlideInRightAnimator());
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.divider));

		//Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
		mAdapter.setFastScroller((FastScroller) findViewById(R.id.fast_scroller), Utils.getColorAccent(this));
		//Experimenting NEW features
		mAdapter.setLongPressDragEnabled(true);//Enable long press to drag items
		mAdapter.setSwipeEnabled(true);//Enable swipe items
		mAdapter.setDisplayHeadersAtStartUp(true);//Show Headers at startUp!
		//Add sample item on the top (not part of library)
		mAdapter.addUserLearnedSelection(savedInstanceState == null);

		//FAB
		mFab = (FloatingActionButton) findViewById(R.id.fab);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				destroyActionModeIfCan();

				for (int position = 0; position <= mAdapter.getItemCountOfTypes(R.layout.recycler_expandable_row) + 1; position++) {
					//Every 3 positions I want to create an expandable
					AbstractExampleItem item = (position % 3 == 0 ?
							DatabaseService.newExpandableItem(position, null) :
							DatabaseService.newSimpleItem(position, null));
					//Add only if we don't have it
					if (!DatabaseService.getInstance().getListById().contains(item)) {
						DatabaseService.getInstance().addItem(position, item);//This is the original list
						//For my example, the adapter position must be adjusted according to
						//all child and headers currently visible
						int adapterPos = position + mAdapter.getItemCountOfTypes(
								R.layout.recycler_uls_row,
								R.layout.recycler_expandable_row,
								R.layout.recycler_child_row,
								R.layout.recycler_header_row);
						//Adapter's list is a copy, to animate the item you must call addItem on the new position
						mAdapter.addItem(adapterPos, item);
						Toast.makeText(MainActivity.this, "Added New " + item.getTitle(), Toast.LENGTH_SHORT).show();
						mRecyclerView.smoothScrollToPosition(position);
						break;
					}
				}
			}
		});

		//With FlexibleAdapter v5.0.0 we don't need to call this function anymore
		//It is automatically called if Activity implements FlexibleAdapter.OnUpdateListener
		//updateEmptyView();

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

		//Settings for FlipView
		FlipView.stopLayoutAnimation();
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
				mAdapter.updateDataSet(DatabaseService.getInstance().getListById());
				mSwipeRefreshLayout.setEnabled(false);
				mSwipeHandler.sendEmptyMessageDelayed(0, 1000L);
				destroyActionModeIfCan();
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
		mSearchView = (SearchView) MenuItemCompat
				.getActionView(menu.findItem(R.id.action_search));
		mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
		mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_FULLSCREEN);
		mSearchView.setQueryHint(getString(R.string.action_search));
		mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setOnSearchClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.findItem(R.id.action_list_type).setVisible(false);
				menu.findItem(R.id.action_reverse).setVisible(false);
				menu.findItem(R.id.action_about).setVisible(false);
			}
		});
		mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
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
		//Has searchText?
		if (!mAdapter.hasSearchText()) {
			Log.d(TAG, "onPrepareOptionsMenu Clearing SearchView!");
			mSearchView.setIconified(true);// This also clears the text in SearchView widget
		} else {
			mSearchView.setQuery(mAdapter.getSearchText(), false);
			mSearchView.setIconified(false);
		}

		MenuItem headersMenuItem = menu.findItem(R.id.action_show_hide_headers);
		headersMenuItem.setTitle(mAdapter.areHeadersShown() ? R.string.hide_headers : R.string.show_headers);
		MenuItem headersSticky = menu.findItem(R.id.action_sticky_headers);
		if (mAdapter.areHeadersShown()) {
			headersSticky.setVisible(true);
			headersSticky.setTitle(mAdapter.areHeadersSticky() ? R.string.scroll_headers : R.string.sticky_headers);
		} else {
			headersSticky.setVisible(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (!mAdapter.hasSearchText()
				|| !mAdapter.getSearchText().equalsIgnoreCase(newText)) {
			Log.d(TAG, "onQueryTextChange newText: " + newText);
			mAdapter.setSearchText(newText);
			//Fill and Filter mItems with your custom list and automatically animate the changes
			//Watch out! The original list must be a copy
			mAdapter.filterItems(DatabaseService.getInstance().getListById(), 450L);
		}

		if (mAdapter.hasSearchText()) {
			//mFab.setVisibility(View.GONE);
			ViewCompat.animate(mFab)
					.scaleX(0f).scaleY(0f)
					.alpha(0f).setDuration(100)
					.start();
		} else {

			//mFab.setVisibility(View.VISIBLE);
			ViewCompat.animate(mFab)
					.scaleX(1f).scaleY(1f)
					.alpha(1f).setDuration(100)
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
				GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
				gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
					@Override
					public int getSpanSize(int position) {
						//NOTE: If you use simple integer to identify the ViewType,
						//here, you should use them and not Layout integers
						switch (mAdapter.getItemViewType(position)) {
							case R.layout.recycler_uls_row:
							case R.layout.recycler_header_row:
							case R.layout.recycler_expandable_row:
								return 2;
							default:
								return 1;
						}
					}
				});
				mRecyclerView.setLayoutManager(gridLayoutManager);
				item.setIcon(R.drawable.ic_view_agenda_white_24dp);
				item.setTitle(R.string.linear_layout);
			}
		} else if (id == R.id.action_reverse) {
			if (mAdapter.isAnimationOnReverseScrolling()) {
				mAdapter.setAnimationOnReverseScrolling(false);
				item.setIcon(R.drawable.ic_sort_white_24dp);
				item.setTitle(R.string.reverse_scrolling);
			} else {
				mAdapter.setAnimationOnReverseScrolling(true);
				item.setIcon(R.drawable.ic_sort_descending_white_24dp);
				item.setTitle(R.string.forward_scrolling);
			}
		} else if (id == R.id.action_auto_collapse) {
			if (item.getTitle().equals(getString(R.string.auto_collapse))) {
				mAdapter.setAutoCollapseOnExpand(true);
				item.setTitle(R.string.keep_expanded);
			} else {
				mAdapter.setAutoCollapseOnExpand(false);
				item.setTitle(R.string.auto_collapse);
			}
		} else if (id == R.id.action_expand_collapse_all) {
			if (item.getTitle().equals(getString(R.string.expand_all))) {
				mAdapter.expandAll();
				item.setTitle(R.string.collapse_all);
			} else {
				mAdapter.collapseAll();
				item.setTitle(R.string.expand_all);
			}
		} else if (id == R.id.action_show_hide_headers) {
			if (mAdapter.areHeadersShown()) {
				mAdapter.hideAllHeaders();
				item.setTitle(R.string.show_headers);
			} else {
				mAdapter.showAllHeaders();
				item.setTitle(R.string.hide_headers);
			}
		} else if (id == R.id.action_sticky_headers) {
			if (mAdapter.areHeadersSticky()) {
				mAdapter.disableStickyHeaders();
				item.setTitle(R.string.sticky_headers);
			} else {
				mAdapter.enableStickyHeaders(3);
				item.setTitle(R.string.scroll_headers);
			}
		}

		//TODO: Show difference between MODE_IDLE, MODE_SINGLE
		//TODO: Add toggle for mAdapter.toggleFastScroller();
		//TODO: Add dialog configuration settings

		return super.onOptionsItemSelected(item);
	}

	//TODO: Include setActivatedPosition in the library?
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
	public boolean onItemClick(int position) {
		if (mActionMode != null && position != INVALID_POSITION) {
			toggleSelection(position);
			return true;
		} else {
			//Notify the active callbacks (ie. the activity, if the fragment is attached to one)
			// that an item has been selected.
			if (mAdapter.getItemCount() > 0) {
				if (position != mActivatedPosition) setActivatedPosition(position);
				AbstractFlexibleItem abstractItem = mAdapter.getItem(position);
				assert abstractItem != null;
				if (!(abstractItem instanceof ExpandableItem) && !(abstractItem instanceof IHeader)) {
					//TODO FOR YOU: call your custom Action, for example mCallback.onItemSelected(item.getId());
					String title = extractTitleFrom(abstractItem);
					EditItemDialog.newInstance(title, position).show(getFragmentManager(), EditItemDialog.TAG);
				}
			}
			return false;
		}
	}

	@Override
	public void onItemLongClick(int position) {
		if (mActionMode == null) {
			Log.d(TAG, "onItemLongClick actionMode activated!");
			mActionMode = startSupportActionMode(this);
		}
		toggleSelection(position);
	}

//	@Override
//	public boolean shouldMoveItem(int fromPosition, int toPosition) {
//		return true;
//	}

	@Override
	public void onItemMove(int fromPosition, int toPosition) {
		AbstractFlexibleItem fromItem = mAdapter.getItem(fromPosition);
		AbstractFlexibleItem toItem = mAdapter.getItem(toPosition);
		//Don't swap if a Header is involved!!!
		if (fromItem instanceof ISectionable || toItem instanceof ISectionable) {
			return;
		}
		//FIXME: this doesn't work with all types of items (of course)..... we need to implement some custom logic
//		DatabaseService.getInstance().swapItem(
//				DatabaseService.getInstance().getListById().indexOf(fromItem),
//				DatabaseService.getInstance().getListById().indexOf(toItem));
	}

	@Override
	public void onItemSwipe(int position, int direction) {
		AbstractFlexibleItem abstractItem = mAdapter.getItem(position);
		assert abstractItem != null;
		//Experimenting NEW feature
		if (abstractItem.isSelectable())
			mAdapter.setRestoreSelectionOnUndo(false);

		//TODO: Create Undo Helper with SnackBar?
		StringBuilder message = new StringBuilder();
		message.append(extractTitleFrom(abstractItem))
				.append(" ").append(getString(R.string.action_deleted));
		//noinspection ResourceType
		mSnackBar = Snackbar.make(findViewById(R.id.main_view), message, 7000)
				.setAction(R.string.undo, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mAdapter.restoreDeletedItems();
					}
				});
		mSnackBar.show();
		mAdapter.removeItem(position, true);
		logOrphanHeaders();
		mAdapter.startUndoTimer(5000L + 200L, this);
		//Handle ActionMode title
		if (mAdapter.getSelectedItemCount() == 0)
			destroyActionModeIfCan();
		else
			setContextTitle(mAdapter.getSelectedItemCount());
	}

	@Override
	public void onTitleModified(int position, String newTitle) {
		AbstractFlexibleItem abstractItem = mAdapter.getItem(position);
		assert abstractItem != null;
		if (abstractItem instanceof AbstractExampleItem) {
			AbstractExampleItem exampleItem = (AbstractExampleItem) abstractItem;
			exampleItem.setTitle(newTitle);
		} else if (abstractItem instanceof HeaderItem) {
			HeaderItem headerItem = (HeaderItem) abstractItem;
			headerItem.setTitle(newTitle);
		}
		mAdapter.updateItem(position, abstractItem, null);
	}

	/**
	 * Handling RecyclerView when empty.
	 * <br/><br/>
	 * <b>Note:</b> The order how the 3 Views (RecyclerView, EmptyView, FastScroller)
	 * are placed in the Layout is important!
	 */
	@Override
	public void onUpdateEmptyView(int size) {
		Log.d(TAG, "onUpdateEmptyView size=" + size);
		FastScroller fastScroller = (FastScroller) findViewById(R.id.fast_scroller);
		TextView emptyView = (TextView) findViewById(R.id.empty);
		emptyView.setText(getString(R.string.no_items));
		if (size > 0) {
			fastScroller.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
		} else {
			fastScroller.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Toggle the selection state of an item.
	 * <p>If the item was the last one in the selection and is unselected, the selection is stopped.
	 * Note that the selection must already be started (actionMode must not be null).</p>
	 *
	 * @param position Position of the item to toggle the selection state
	 */
	private void toggleSelection(int position) {
		mAdapter.toggleSelection(position);
		if (mActionMode == null) return;

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
		if (mActionMode != null) {
			mActionMode.setTitle(String.valueOf(count) + " " + (count == 1 ?
					getString(R.string.action_selected_one) :
					getString(R.string.action_selected_many)));
		}
	}

	@Override
	public void onDeleteConfirmed() {
		mSwipeHandler.sendEmptyMessage(0);
		for (AbstractFlexibleItem adapterItem : mAdapter.getDeletedItems()) {
			//Removing items from Database. Example:
			try {
				//NEW! You can take advantage of AutoMap and differentiate logic by viewType using "switch" statement
				switch (adapterItem.getLayoutRes()) {
					case R.layout.recycler_child_row:
						SubItem subItem = (SubItem) adapterItem;
						ExpandableItem expandable = (ExpandableItem) mAdapter.getExpandableOfDeletedChild(subItem);
						DatabaseService.getInstance().removeSubItem(expandable, subItem);
						Log.d(TAG, "Confirm removed " + subItem.getTitle());
						break;
					case R.layout.recycler_expandable_row:
						DatabaseService.getInstance().removeItem(adapterItem);
						Log.d(TAG, "Confirm removed " + adapterItem);
						break;
				}

			} catch (IllegalStateException e) {
				//AutoMap is disabled, fallback to if-else with "instanceof" statement
				if (adapterItem instanceof SubItem) {
					//SubItem
					SubItem subItem = (SubItem) adapterItem;
					IExpandable expandable = mAdapter.getExpandableOf(subItem);
					DatabaseService.getInstance().removeSubItem((ExpandableItem) expandable, subItem);
					Log.d(TAG, "Confirm removed " + subItem.getTitle());
				} else if (adapterItem instanceof SimpleItem) {
					//SimpleItem or ExpandableItem(extends SimpleItem)
					DatabaseService.getInstance().removeItem(adapterItem);
					Log.d(TAG, "Confirm removed " + adapterItem);
				}
			}
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
		} else if (Utils.hasLollipop()) {
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
				//Build message before delete, for the SnackBar
				StringBuilder message = new StringBuilder();
				message.append(getString(R.string.action_deleted)).append(" ");
				for (Integer pos : mAdapter.getSelectedPositions()) {
					message.append(extractTitleFrom(mAdapter.getItem(pos)));
					if (mAdapter.getSelectedItemCount() > 1)
						message.append(", ");
				}

				//SnackBar for Undo
				//noinspection ResourceType
				int undoTime = 20000;
				//noinspection ResourceType
				mSnackBar = Snackbar.make(findViewById(R.id.main_view), message, undoTime)
						.setAction(R.string.undo, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mAdapter.restoreDeletedItems();
								mSwipeHandler.sendEmptyMessage(0);
								if (mAdapter.isRestoreWithSelection() && mAdapter.getSelectedItemCount() > 0) {
									mActionMode = startSupportActionMode(MainActivity.this);
									setContextTitle(mAdapter.getSelectedItemCount());
								}
							}
						});
				mSnackBar.show();

				//Remove selected items from Adapter list after message is shown
				//MY Payload is a Boolean(true), you can pass what ever you want!
				mAdapter.removeItems(mAdapter.getSelectedPositions(), true);
				logOrphanHeaders();
				//+200: Using SnackBar, user can still click on the action button while bar is dismissing for a fraction of time
				mAdapter.startUndoTimer(undoTime + 200L, this);

				mSwipeHandler.sendEmptyMessage(1);
				mSwipeHandler.sendEmptyMessageDelayed(0, undoTime);

				//Experimenting NEW feature
				mAdapter.setRestoreSelectionOnUndo(true);
				mActionMode.finish();
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		Log.v(TAG, "onDestroyActionMode called!");
		//With FlexibleAdapter v5.0.0 you should use MODE_IDLE if you don't want
		//single selection still visible.
		mAdapter.setMode(FlexibleAdapter.MODE_IDLE);
		mAdapter.clearSelection();
		mActionMode = null;
		if (Utils.hasMarshmallow()) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark_light, this.getTheme()));
		} else if (Utils.hasLollipop()) {
			//noinspection deprecation
			getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark_light));
		}
	}

	/**
	 * Utility method called from MainActivity on BackPressed
	 *
	 * @return true if ActionMode was active (in case it is also terminated), false otherwise
	 */
	public boolean destroyActionModeIfCan() {
		if (mActionMode != null) {
			mActionMode.finish();
			return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		//If ActionMode is active, back key closes it
		if (destroyActionModeIfCan()) return;
		//If SearchView is visible, back key cancels search and iconify it
		if (mSearchView != null && !mSearchView.isIconified()) {
			mSearchView.setIconified(true);
			return;
		}
		//Close the App
		DatabaseService.onDestroy();
		super.onBackPressed();
	}

	private void logOrphanHeaders() {
		//If removeOrphanHeader is set false, once hidden the Orphan Headers are not shown
		// anymore, but you can recover them using getOrphanHeaders()
		for (IHeader header : mAdapter.getOrphanHeaders()) {
			Log.w(TAG, "Logging orphan header " + header);
		}
	}

	private String extractTitleFrom(AbstractFlexibleItem abstractItem) {
		if (abstractItem instanceof AbstractExampleItem) {
			AbstractExampleItem exampleItem = (AbstractExampleItem) abstractItem;
			String title = exampleItem.getTitle();
			if (exampleItem instanceof ExpandableItem) {
				ExpandableItem expandableItem = (ExpandableItem) abstractItem;
				if (expandableItem.getSubItems() != null) {
					title += "(+" + expandableItem.getSubItems().size() + ")";
				}
			}
			return title;
		} else if (abstractItem instanceof HeaderItem) {
			HeaderItem headerItem = (HeaderItem) abstractItem;
			return headerItem.getTitle();
		}
		//We already covered all situations with instanceof
		return "";
	}

}