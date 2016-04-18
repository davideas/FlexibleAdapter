package eu.davidea.examples.flexibleadapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import eu.davidea.examples.flexibleadapter.fragments.FragmentAnimators;
import eu.davidea.examples.flexibleadapter.fragments.FragmentExpandableMultiLevel;
import eu.davidea.examples.flexibleadapter.fragments.FragmentExpandableSections;
import eu.davidea.examples.flexibleadapter.fragments.FragmentHeadersSections;
import eu.davidea.examples.flexibleadapter.fragments.FragmentOverall;
import eu.davidea.examples.flexibleadapter.fragments.OnFragmentInteractionListener;
import eu.davidea.examples.flexibleadapter.models.AbstractModelItem;
import eu.davidea.examples.flexibleadapter.models.ExpandableItem;
import eu.davidea.examples.flexibleadapter.models.ExpandableLevel1Item;
import eu.davidea.examples.flexibleadapter.models.HeaderItem;
import eu.davidea.examples.flexibleadapter.models.OverallItem;
import eu.davidea.examples.flexibleadapter.models.SimpleItem;
import eu.davidea.examples.flexibleadapter.models.SubItem;
import eu.davidea.examples.flexibleadapter.services.DatabaseService;
import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.utils.Utils;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity implements
		ActionMode.Callback, EditItemDialog.OnEditItemListener, SearchView.OnQueryTextListener,
		FlexibleAdapter.OnUpdateListener, FlexibleAdapter.OnDeleteCompleteListener,
		FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnItemLongClickListener,
		FlexibleAdapter.OnItemMoveListener, FlexibleAdapter.OnItemSwipeListener,
		FastScroller.OnScrollStateChangeListener,
		NavigationView.OnNavigationItemSelectedListener,
		OnFragmentInteractionListener {

	public static final String TAG = MainActivity.class.getSimpleName();

	/**
	 * Bundle key representing the Active Fragment
	 */
	private static final String STATE_ACTIVE_FRAGMENT = "active_fragment";

	/**
	 * FAB
	 */
	private FloatingActionButton mFab;

	/**
	 * RecyclerView and related objects
	 */
	private RecyclerView mRecyclerView;
	private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
	private ActionMode mActionMode;
	private Snackbar mSnackBar;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private Toolbar mToolbar;
	private DrawerLayout mDrawer;
	private NavigationView mNavigationView;
	private Fragment mFragment;
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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate");
		FlexibleAdapter.enableLogs(true);

		//Initialize Toolbar, Drawer & FAB
		initializeToolbar();
		initializeDrawer();
		initializeFab();
		//Initialize Fragment containing Adapter & RecyclerView
		initializeFragment(savedInstanceState);

		//With FlexibleAdapter v5.0.0 we don't need to call this function anymore
		//It is automatically called if Activity implements FlexibleAdapter.OnUpdateListener
		//updateEmptyView();

		//Restore previous state
		if (savedInstanceState != null && mAdapter != null) {
			//Selection
			mAdapter.onRestoreInstanceState(savedInstanceState);
			if (mAdapter.getSelectedItemCount() > 0) {
				mActionMode = startSupportActionMode(this);
				setContextTitle(mAdapter.getSelectedItemCount());
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.v(TAG, "onSaveInstanceState start!");
		mAdapter.onSaveInstanceState(outState);
		getFragmentManager().putFragment(outState, STATE_ACTIVE_FRAGMENT, mFragment);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onAdapterChange(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView) {
		mRecyclerView = recyclerView;
		mAdapter = (FlexibleAdapter) recyclerView.getAdapter();
		mSwipeRefreshLayout = swipeRefreshLayout;
		initializeSwipeToRefresh();
	}

	private void initializeFragment(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mFragment = getFragmentManager().getFragment(savedInstanceState, STATE_ACTIVE_FRAGMENT);
		}
		if (mFragment == null) {
			mFragment = FragmentOverall.newInstance(2);
			mToolbar.setSubtitle(getString(R.string.overall));
		}
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.recycler_view_container,
				mFragment).commit();
	}

	private void initializeSwipeToRefresh() {
		//Swipe down to force synchronize
		//mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
		mSwipeRefreshLayout.setDistanceToTriggerSync(390);
		mSwipeRefreshLayout.setEnabled(true);
		mSwipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_purple, android.R.color.holo_blue_light,
				android.R.color.holo_green_light, android.R.color.holo_orange_light);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList());
				mSwipeRefreshLayout.setEnabled(false);
				mSwipeHandler.sendEmptyMessageDelayed(0, 1000L);
				destroyActionModeIfCan();
			}
		});
	}

	private void initializeToolbar() {
		Log.d(TAG, "initializeToolbar as actionBar");
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		//Toolbar will now take on default Action Bar characteristics
		setSupportActionBar(mToolbar);
	}

	@SuppressWarnings("ConstantConditions")
	private void initializeDrawer() {
		mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mDrawer.addDrawerListener(toggle);
		toggle.syncState();

		mNavigationView = (NavigationView) findViewById(R.id.nav_view);
		mNavigationView.setNavigationItemSelectedListener(this);
		//TODO: select the correct item after the rotation

		//Version
		TextView appVersion = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.app_version);
		appVersion.setText(getString(R.string.about_version,
				Utils.getVersionName(this),
				Utils.getVersionCode(this)));
	}

	private void initializeFab() {
		mFab = (FloatingActionButton) findViewById(R.id.fab);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				destroyActionModeIfCan();

				for (int position = 0; position <= mAdapter.getItemCountOfTypes(R.layout.recycler_expandable_row) + 1; position++) {
					//Every 3 positions I want to create an expandable
					AbstractModelItem item = (position % 3 == 0 ?
							DatabaseService.newExpandableItem(position, null) :
							DatabaseService.newSimpleItem(position, null));
					//Add only if we don't have it
					if (!DatabaseService.getInstance().getDatabaseList().contains(item)) {
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
						mRecyclerView.smoothScrollToPosition(adapterPos);
						break;
					}
				}
			}
		});
	}

	@Override
	public void onFastScrollerStateChange(boolean scrolling) {
		if (scrolling) {
			mFab.hide();
		} else {
			mFab.postDelayed(new Runnable() {
				@Override
				public void run() {
					mFab.show();
				}
			}, 200L);
		}
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		//Handle navigation view item clicks
		int id = item.getItemId();
		if (id == R.id.nav_overall) {
			mFragment = FragmentOverall.newInstance(2);
		} else if (id == R.id.nav_animators) {
			mFragment = FragmentAnimators.newInstance(2);
		} else if (id == R.id.nav_instagram_headers) {

		} else if (id == R.id.nav_headers_and_sections) {
			mFragment = FragmentHeadersSections.newInstance(2);
		} else if (id == R.id.nav_selection_modes) {

		} else if (id == R.id.nav_expandable) {

		} else if (id == R.id.nav_multi_level_expandable) {
			mFragment = FragmentExpandableMultiLevel.newInstance(2);
		} else if (id == R.id.nav_expandable_sections) {
			mFragment = FragmentExpandableSections.newInstance(3);
		} else if (id == R.id.nav_about) {
			MessageDialog.newInstance(
					R.drawable.ic_info_grey600_24dp,
					getString(R.string.about_title),
					getString(R.string.about_body,
							Utils.getVersionName(this),
							Utils.getVersionCode(this)))
					.show(getFragmentManager(), MessageDialog.TAG);
			return true;
		} else if (id == R.id.nav_github) {

		}
		// Insert the fragment by replacing any existing fragment
		if (mFragment != null) {
			//Highlight the selected item has been done by NavigationView
			item.setChecked(true);
			//THIS IS VERY IMPORTANT. Because you are going to inflate a new RecyclerView, its
			//Adapter will be null, therefore the following method cannot be called automatically!
			//If your StickyHeaderContainer is in the main view, you must call this method to clean
			//the previous sticky view. Alternatively you can move the <include> of StickyHeaderLayout
			//in the Fragment view.
			mAdapter.onDetachedFromRecyclerView(mRecyclerView);
			//Inflate the new Fragment with the new RecyclerView and a new Adapter
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.recycler_view_container, mFragment).commit();
			//Close drawer
			mDrawer.closeDrawer(GravityCompat.START);
			mToolbar.setSubtitle(item.getTitle());
			return true;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v(TAG, "onPrepareOptionsMenu called!");

		if (mSearchView != null) {
			//Has searchText?
			if (!mAdapter.hasSearchText()) {
				Log.d(TAG, "onPrepareOptionsMenu Clearing SearchView!");
				mSearchView.setIconified(true);// This also clears the text in SearchView widget
			} else {
				mSearchView.setQuery(mAdapter.getSearchText(), false);
				mSearchView.setIconified(false);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void initSearchView(final Menu menu) {
		//Associate searchable configuration with the SearchView
		Log.d(TAG, "onCreateOptionsMenu setup SearchView!");
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		if (searchItem != null) {
			MenuItemCompat.setOnActionExpandListener(
					searchItem, new MenuItemCompat.OnActionExpandListener() {
						@Override
						public boolean onMenuItemActionExpand(MenuItem item) {
							MenuItem listTypeItem = menu.findItem(R.id.action_list_type);
							if (listTypeItem != null)
								listTypeItem.setVisible(false);

							ViewCompat.animate(mFab)
									.scaleX(0f).scaleY(0f)
									.alpha(0f).setDuration(100)
									.start();

							return true;
						}

						@Override
						public boolean onMenuItemActionCollapse(MenuItem item) {
							MenuItem listTypeItem = menu.findItem(R.id.action_list_type);
							if (listTypeItem != null)
								listTypeItem.setVisible(true);

							mFab.postDelayed(new Runnable() {
								@Override
								public void run() {
									ViewCompat.animate(mFab)
											.scaleX(1f).scaleY(1f)
											.alpha(1f).setDuration(100)
											.start();
								}
							}, 400L);

							return true;
						}
					});
			mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
			mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
			mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_FULLSCREEN);
			mSearchView.setQueryHint(getString(R.string.action_search));
			mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			mSearchView.setOnQueryTextListener(this);
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (!mAdapter.hasSearchText() || mAdapter.hasNewSearchText(newText)) {
			Log.d(TAG, "onQueryTextChange newText: " + newText);
			mAdapter.setSearchText(newText);
			//Fill and Filter mItems with your custom list and automatically animate the changes
			//Watch out! The original list must be a copy
			mAdapter.filterItems(DatabaseService.getInstance().getDatabaseList(), 450L);
		}
		//Disable SwipeRefresh if search is active!!
		mSwipeRefreshLayout.setEnabled(!mAdapter.hasSearchText());
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
		if (id == R.id.action_reverse) {
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
				int total = mAdapter.expandAll();
				Toast.makeText(this, "Expanded " + total + " items", Toast.LENGTH_SHORT).show();
				item.setTitle(R.string.collapse_all);
			} else {
				int total = mAdapter.collapseAll();
				Toast.makeText(this, "Collapsed " + total + " items", Toast.LENGTH_SHORT).show();
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
				mAdapter.enableStickyHeaders();
				item.setTitle(R.string.scroll_headers);
			}
		}

		//TODO: Show difference between MODE_IDLE, MODE_SINGLE
		//TODO: Add toggle for mAdapter.toggleFastScroller();
		//TODO: Add dialog configuration settings

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onItemClick(int position) {
		IFlexible flexibleItem = mAdapter.getItem(position);
		if (flexibleItem instanceof OverallItem) {
			OverallItem overallItem = (OverallItem) flexibleItem;
			MenuItem menuItem = mNavigationView.getMenu().findItem(overallItem.getId());
			onNavigationItemSelected(menuItem);
			return false;
		}

		if (mActionMode != null && position != RecyclerView.NO_POSITION) {
			toggleSelection(position);
			return true;
		} else {
			//Notify the active callbacks (ie. the activity, if the fragment is attached to one)
			// that an item has been selected.
			if (mAdapter.getItemCount() > 0) {
				if (!(flexibleItem instanceof ExpandableItem) && !(flexibleItem instanceof IHeader) &&
						!(flexibleItem instanceof ExpandableLevel1Item)) {
					//TODO FOR YOU: call your custom Action
					String title = extractTitleFrom(flexibleItem);
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
		IFlexible fromItem = mAdapter.getItem(fromPosition);
		IFlexible toItem = mAdapter.getItem(toPosition);
		//Don't swap if a Header is involved!!!
//		if (fromItem instanceof ISectionable || toItem instanceof ISectionable) {
//			return;
//		}
		//FIXME: this doesn't work with all types of items (of course)..... we need to implement some custom logic
//		DatabaseService.getInstance().swapItem(
//				DatabaseService.getInstance().getDatabaseList().indexOf(fromItem),
//				DatabaseService.getInstance().getDatabaseList().indexOf(toItem));
	}

	@Override
	public void onItemSwipe(int position, int direction) {
		IFlexible abstractItem = mAdapter.getItem(position);
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
		if (abstractItem instanceof AbstractModelItem) {
			AbstractModelItem exampleItem = (AbstractModelItem) abstractItem;
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
						DatabaseService.getInstance().removeSubItem(mAdapter.getExpandableOfDeletedChild(subItem), subItem);
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

	@Override
	public void onBackPressed() {
		//If Drawer is open, back key closes it
		if (mDrawer.isDrawerOpen(GravityCompat.START)) {
			mDrawer.closeDrawer(GravityCompat.START);
			return;
		}
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

	/**
	 * Utility method called from MainActivity on BackPressed
	 *
	 * @return true if ActionMode was active (in case it is also terminated), false otherwise
	 */
	private boolean destroyActionModeIfCan() {
		if (mActionMode != null) {
			mActionMode.finish();
			return true;
		}
		return false;
	}

	private void logOrphanHeaders() {
		//If removeOrphanHeader is set false, once hidden the Orphan Headers are not shown
		// anymore, but you can recover them using getOrphanHeaders()
		for (IHeader header : mAdapter.getOrphanHeaders()) {
			Log.w(TAG, "Logging orphan header " + header);
		}
	}

	private String extractTitleFrom(IFlexible flexibleItem) {
		if (flexibleItem instanceof AbstractModelItem) {
			AbstractModelItem exampleItem = (AbstractModelItem) flexibleItem;
			String title = exampleItem.getTitle();
			if (exampleItem instanceof ExpandableItem) {
				ExpandableItem expandableItem = (ExpandableItem) flexibleItem;
				if (expandableItem.getSubItems() != null) {
					title += "(+" + expandableItem.getSubItems().size() + ")";
				}
			}
			return title;
		} else if (flexibleItem instanceof HeaderItem) {
			HeaderItem headerItem = (HeaderItem) flexibleItem;
			return headerItem.getTitle();
		}
		//We already covered all situations with instanceof
		return "";
	}

}