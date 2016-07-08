package eu.davidea.samples.flexibleadapter;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;
import eu.davidea.flexibleadapter.helpers.UndoHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.samples.flexibleadapter.fragments.AbstractFragment;
import eu.davidea.samples.flexibleadapter.fragments.FragmentEndlessScrolling;
import eu.davidea.samples.flexibleadapter.fragments.FragmentExpandableMultiLevel;
import eu.davidea.samples.flexibleadapter.fragments.FragmentExpandableSections;
import eu.davidea.samples.flexibleadapter.fragments.FragmentHeadersSections;
import eu.davidea.samples.flexibleadapter.fragments.FragmentInstagramHeaders;
import eu.davidea.samples.flexibleadapter.fragments.FragmentOverall;
import eu.davidea.samples.flexibleadapter.fragments.FragmentSelectionModes;
import eu.davidea.samples.flexibleadapter.fragments.FragmentStaggeredLayout;
import eu.davidea.samples.flexibleadapter.fragments.MessageDialogFragment;
import eu.davidea.samples.flexibleadapter.fragments.OnFragmentInteractionListener;
import eu.davidea.samples.flexibleadapter.models.AbstractModelItem;
import eu.davidea.samples.flexibleadapter.models.ExpandableItem;
import eu.davidea.samples.flexibleadapter.models.HeaderItem;
import eu.davidea.samples.flexibleadapter.models.OverallItem;
import eu.davidea.samples.flexibleadapter.models.SimpleItem;
import eu.davidea.samples.flexibleadapter.models.StaggeredItem;
import eu.davidea.samples.flexibleadapter.models.SubItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;
import eu.davidea.utils.ScrollAwareFABBehavior;
import eu.davidea.utils.Utils;

@SuppressWarnings({"ConstantConditions", "unchecked"})
public class MainActivity extends AppCompatActivity implements
		ActionMode.Callback, EditItemDialog.OnEditItemListener, SearchView.OnQueryTextListener,
		FlexibleAdapter.OnUpdateListener, UndoHelper.OnUndoListener,
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
	private ActionModeHelper mActionModeHelper;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private Toolbar mToolbar;
	private DrawerLayout mDrawer;
	private NavigationView mNavigationView;
	private AbstractFragment mFragment;
	private SearchView mSearchView;
	private final Handler mRefreshHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
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
		if (Utils.hasLollipop()) requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		super.onCreate(savedInstanceState);

		if (Utils.hasLollipop()) {
			getWindow().setEnterTransition(new Fade());
		}

		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate");
		FlexibleAdapter.enableLogs(true);

		//Initialize Toolbar, Drawer, FAB & BottomSheet
		initializeToolbar();
		initializeDrawer();
		initializeFab();
		//Initialize Fragment containing Adapter & RecyclerView
		initializeFragment(savedInstanceState);

		//With FlexibleAdapter v5.0.0 we don't need to call this function anymore
		//It is automatically called if Activity implements FlexibleAdapter.OnUpdateListener
		//updateEmptyView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.v(TAG, "onSaveInstanceState!");
		mAdapter.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, STATE_ACTIVE_FRAGMENT, mFragment);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		//Restore previous state
		if (savedInstanceState != null && mAdapter != null) {
			//Selection
			mAdapter.onRestoreInstanceState(savedInstanceState);
			mActionModeHelper.restoreSelection(this);
		}
	}

	@Override
	public void onFragmentChange(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView, int mode) {
		mRecyclerView = recyclerView;
		mAdapter = (FlexibleAdapter) recyclerView.getAdapter();
		mSwipeRefreshLayout = swipeRefreshLayout;
		initializeSwipeToRefresh();
		initializeActionModeHelper(mode);
	}

	private void initializeActionModeHelper(int mode) {
		mActionModeHelper = new ActionModeHelper(mAdapter, mFragment.getContextMenuResId(), this) {
			@Override
			public void updateContextTitle(int count) {
				if (mActionMode != null) {//You can use the internal ActionMode instance
					mActionMode.setTitle(count == 1 ?
							getString(R.string.action_selected_one, count) :
							getString(R.string.action_selected_many, count));
				}
			}
		}.withDefaultMode(mode);
	}

	private void initializeFragment(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mFragment = (AbstractFragment) getSupportFragmentManager().getFragment(savedInstanceState, STATE_ACTIVE_FRAGMENT);
		}
		if (mFragment == null) {
			mFragment = FragmentOverall.newInstance(2);
			mToolbar.setSubtitle(getString(R.string.overall));
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
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
				//Passing true as parameter we always animate the changes between the old and the new data set
				mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList(), true);
				mSwipeRefreshLayout.setEnabled(false);
				mRefreshHandler.sendEmptyMessageDelayed(0, 1000L);
				mActionModeHelper.destroyActionModeIfCan();
			}
		});
	}

	private void initializeToolbar() {
		Log.d(TAG, "initializeToolbar as actionBar");
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		//Toolbar will now take on default Action Bar characteristics
		setSupportActionBar(mToolbar);
	}

	private void initializeDrawer() {
		mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mDrawer.addDrawerListener(toggle);
		toggle.syncState();

		mNavigationView = (NavigationView) findViewById(R.id.nav_view);
		mNavigationView.setNavigationItemSelectedListener(this);

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
				mActionModeHelper.destroyActionModeIfCan();
				mFragment.performFabAction();
			}
		});
		//No Fab on 1st fragment
		hideFab();
	}

	@Override
	public void onFastScrollerStateChange(boolean scrolling) {
		if (scrolling) {
			hideFab();
		} else {
			showFab();
		}
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		hideFab();
		CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
		ScrollAwareFABBehavior fabBehavior = ((ScrollAwareFABBehavior) layoutParams.getBehavior());
		fabBehavior.setEnabled(false);

		//Handle navigation view item clicks
		int id = item.getItemId();
		if (id == R.id.nav_overall) {
			mFragment = FragmentOverall.newInstance(2);
		} else if (id == R.id.nav_endless_scrolling) {
			mFragment = FragmentEndlessScrolling.newInstance(2);
		} else if (id == R.id.nav_instagram_headers) {
			mFragment = FragmentInstagramHeaders.newInstance();
		} else if (id == R.id.nav_headers_and_sections) {
			mFragment = FragmentHeadersSections.newInstance(2);
			showFab();
			fabBehavior.setEnabled(true);
		} else if (id == R.id.nav_selection_modes) {
			mFragment = FragmentSelectionModes.newInstance(2);
		} else if (id == R.id.nav_expandable) {

		} else if (id == R.id.nav_multi_level_expandable) {
			mFragment = FragmentExpandableMultiLevel.newInstance(2);
		} else if (id == R.id.nav_expandable_sections) {
			mFragment = FragmentExpandableSections.newInstance(3);
		} else if (id == R.id.nav_staggered) {
			mFragment = FragmentStaggeredLayout.newInstance(2);
			showFab();
			fabBehavior.setEnabled(true);
		} else if (id == R.id.nav_about) {
			MessageDialogFragment.newInstance(
					R.drawable.ic_info_grey600_24dp,
					getString(R.string.about_title),
					getString(R.string.about_body,
							Utils.getVersionName(this),
							Utils.getVersionCode(this)))
					.show(getFragmentManager(), MessageDialogFragment.TAG);
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
			FragmentManager fragmentManager = getSupportFragmentManager();
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
				//Necessary after the restoreInstanceState
				menu.findItem(R.id.action_search).expandActionView();//must be called first
				//This restores the text, must be after the expandActionView()
				mSearchView.setQuery(mAdapter.getSearchText(), false);//submit = false!!!
				mSearchView.clearFocus();//Optionally the keyboard can be closed
				//mSearchView.setIconified(false);//This is not necessary
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
							hideFab();
							return true;
						}

						@Override
						public boolean onMenuItemActionCollapse(MenuItem item) {
							MenuItem listTypeItem = menu.findItem(R.id.action_list_type);
							if (listTypeItem != null)
								listTypeItem.setVisible(true);
							showFab();
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

	private void hideFab() {
		ViewCompat.animate(mFab)
				.scaleX(0f).scaleY(0f)
				.alpha(0f).setDuration(100)
				.start();
	}

	private void showFab() {
		if (mFragment instanceof FragmentHeadersSections || mFragment instanceof FragmentStaggeredLayout)
			ViewCompat.animate(mFab)
					.scaleX(1f).scaleY(1f)
					.alpha(1f).setDuration(100)
					.setStartDelay(300L)
					.start();
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (mAdapter.hasNewSearchText(newText)) {
			Log.d(TAG, "onQueryTextChange newText: " + newText);
			mAdapter.setSearchText(newText);
			//Fill and Filter mItems with your custom list and automatically animate the changes
			//Watch out! The original list must be a copy
			mAdapter.filterItems(DatabaseService.getInstance().getDatabaseList(), 0L);
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
				Snackbar.make(findViewById(R.id.main_view), "Reverse Scrolling Animation is disabled", Snackbar.LENGTH_SHORT).show();
			} else {
				mAdapter.setAnimationOnReverseScrolling(true);
				item.setIcon(R.drawable.ic_sort_descending_white_24dp);
				item.setTitle(R.string.forward_scrolling);
				Snackbar.make(findViewById(R.id.main_view), "Reverse Scrolling Animation is enabled", Snackbar.LENGTH_SHORT).show();
			}
		} else if (id == R.id.action_auto_collapse) {
			if (item.getTitle().equals(getString(R.string.auto_collapse))) {
				mAdapter.setAutoCollapseOnExpand(true);
				item.setTitle(R.string.keep_expanded);
				Snackbar.make(findViewById(R.id.main_view), "Auto-Collapse is enabled", Snackbar.LENGTH_SHORT).show();
			} else {
				mAdapter.setAutoCollapseOnExpand(false);
				item.setTitle(R.string.auto_collapse);
				Snackbar.make(findViewById(R.id.main_view), "Auto-Collapse is disabled", Snackbar.LENGTH_SHORT).show();
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
				Snackbar.make(findViewById(R.id.main_view), "Sticky headers disabled", Snackbar.LENGTH_SHORT).show();
			} else {
				mAdapter.enableStickyHeaders();
				item.setTitle(R.string.scroll_headers);
				Snackbar.make(findViewById(R.id.main_view), "Sticky headers enabled", Snackbar.LENGTH_SHORT).show();
			}
		} else if (id == R.id.action_selection_mode) {
			if (mAdapter.getMode() == SelectableAdapter.MODE_IDLE) {
				mAdapter.setMode(SelectableAdapter.MODE_SINGLE);
				mActionModeHelper.withDefaultMode(SelectableAdapter.MODE_SINGLE);
				item.setIcon(R.drawable.ic_select_off_white_24dp);
				item.setTitle(R.string.mode_idle);
				Snackbar.make(findViewById(R.id.main_view), "Selection MODE_SINGLE is enabled", Snackbar.LENGTH_SHORT).show();
			} else {
				mAdapter.setMode(SelectableAdapter.MODE_IDLE);
				mActionModeHelper.withDefaultMode(SelectableAdapter.MODE_IDLE);
				item.setIcon(R.drawable.ic_select_white_24dp);
				item.setTitle(R.string.mode_single);
				Snackbar.make(findViewById(R.id.main_view), "Selection MODE_IDLE is enabled", Snackbar.LENGTH_SHORT).show();
			}
		} else if (id == R.id.action_reset || id == R.id.action_delete) {
			showFab();
		}
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

		//Action on elements are allowed if Mode is IDLE, otherwise selection has priority
		if (mAdapter.getMode() != SelectableAdapter.MODE_IDLE && mActionModeHelper != null) {
			return mActionModeHelper.onClick(position);
		} else {
			//Notify the active callbacks or implement a custom action onClick
			if (!(flexibleItem instanceof ExpandableItem) && flexibleItem instanceof SimpleItem
					|| flexibleItem instanceof SubItem) {
				//TODO FOR YOU: call your custom Action
				String title = extractTitleFrom(flexibleItem);
				EditItemDialog.newInstance(title, position).show(getFragmentManager(), EditItemDialog.TAG);
			}
			return false;
		}
	}

	@Override
	public void onItemLongClick(int position) {
		mActionModeHelper.onLongClick(this, position);
	}

//	/**
//	 * Not yet analyzed
//	 */
//	@Override
//	public boolean shouldMoveItem(int fromPosition, int toPosition) {
//		return true;
//	}

	@Override
	public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
		mSwipeRefreshLayout.setEnabled(actionState == ItemTouchHelper.ACTION_STATE_IDLE);
	}

	@Override
	public void onItemMove(int fromPosition, int toPosition) {
//		IFlexible fromItem = mAdapter.getItem(fromPosition);
//		IFlexible toItem = mAdapter.getItem(toPosition);
		//Don't swap if a Header is involved!!!
//		if (fromItem instanceof ISectionable || toItem instanceof ISectionable) {
//			return;
//		}
		//FIXME: this doesn't work with all types of items (of course)..... we need to implement some custom logic
//		DatabaseService.getInstance().swapItems(
//				DatabaseService.getInstance().getDatabaseList().indexOf(fromItem),
//				DatabaseService.getInstance().getDatabaseList().indexOf(toItem));
	}

	@Override
	public void onItemSwipe(final int position, int direction) {
		Log.i(TAG, "onItemSwipe position=" + position +
				" direction=" + (direction == ItemTouchHelper.LEFT ? "LEFT" : "RIGHT"));

		//Option 1 FULL_SWIPE: Direct action no Undo Action
		//Do something based on direction when item has been swiped:
		//   A) update item, set "read" if an email etc.
		//   B) remove the item from the adapter;

		//Option 2 FULL_SWIPE: Delayed action with Undo Action
		//Show action button and start a new Handler:
		//   A) on time out do something based on direction (open dialog with options);

		//Create list for single position (only in onItemSwipe)
		List<Integer> positions = new ArrayList<Integer>(1);
		positions.add(position);
		//Build the message
		IFlexible abstractItem = mAdapter.getItem(position);
		StringBuilder message = new StringBuilder();
		message.append(extractTitleFrom(abstractItem)).append(" ");
		//Experimenting NEW feature
		if (abstractItem.isSelectable())
			mAdapter.setRestoreSelectionOnUndo(false);

		//Perform different actions
		//Here, option 2A) is implemented
		if (direction == ItemTouchHelper.LEFT) {
			message.append(getString(R.string.action_archived));
			new UndoHelper(mAdapter, this)
					.withPayload(true)//You can pass any custom object (in this case Boolean is enough)
					.withAction(UndoHelper.ACTION_UPDATE, new UndoHelper.SimpleActionListener() {
						@Override
						public boolean onPreAction() {
							//Return true to avoid default immediate deletion.
							//Ask to the user what to do, open a custom dialog. On option chosen,
							//remove the item from Adapter list as usual.
							return true;
						}
					})
					.remove(positions, findViewById(R.id.main_view), message,
							getString(R.string.undo), UndoHelper.UNDO_TIMEOUT);

		//Here, option 1B) is implemented
		} else if (direction == ItemTouchHelper.RIGHT) {
			message.append(getString(R.string.action_deleted));
			new UndoHelper(mAdapter, this)
					.withPayload(true)//You can pass any custom object (in this case Boolean is enough)
					.withAction(UndoHelper.ACTION_REMOVE, new UndoHelper.SimpleActionListener() {
						@Override
						public void onPostAction() {
							logOrphanHeaders();
							//Handle ActionMode title
							if (mAdapter.getSelectedItemCount() == 0)
								mActionModeHelper.destroyActionModeIfCan();
							else
								mActionModeHelper.updateContextTitle(mAdapter.getSelectedItemCount());
						}
					})
					.remove(positions, findViewById(R.id.main_view), message,
							getString(R.string.undo), UndoHelper.UNDO_TIMEOUT);
		}
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
	 * <b>Note:</b> The order, how the 3 Views (RecyclerView, EmptyView, FastScroller)
	 * are placed in the Layout, is important!
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

	@Override
	public void onUndoConfirmed(int action) {
		if (action == UndoHelper.ACTION_UPDATE) {
			//FIXME: Adjust click animation on swiped item
//			final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForLayoutPosition(mSwipedPosition);
//			if (holder instanceof ItemTouchHelperCallback.ViewHolderCallback) {
//				final View view = ((ItemTouchHelperCallback.ViewHolderCallback) holder).getFrontView();
//				Animator animator = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0);
//				animator.addListener(new SimpleAnimatorListener() {
//					@Override
//					public void onAnimationCancel(Animator animation) {
//						view.setTranslationX(0);
//					}
//				});
//				animator.start();
//			}
		} else if (action == UndoHelper.ACTION_REMOVE) {
			//Custom action is restore deleted items
			mAdapter.restoreDeletedItems();
			//Enable SwipeRefresh
			mRefreshHandler.sendEmptyMessage(0);
			//Check also selection restoration
			if (mAdapter.isRestoreWithSelection()) {
				mActionModeHelper.restoreSelection(this);
			}
		}
	}

	@Override
	public void onDeleteConfirmed(int action) {
		//Enable SwipeRefresh
		mRefreshHandler.sendEmptyMessage(0);
		//Removing items from Database. Example:
		for (AbstractFlexibleItem adapterItem : mAdapter.getDeletedItems()) {
			try {
				//NEW! You can take advantage of AutoMap and differentiate logic by viewType using "switch" statement
				switch (adapterItem.getLayoutRes()) {
					case R.layout.recycler_sub_item:
						SubItem subItem = (SubItem) adapterItem;
						DatabaseService.getInstance().removeSubItem(mAdapter.getExpandableOfDeletedChild(subItem), subItem);
						Log.d(TAG, "Confirm removed " + subItem.getTitle());
						break;
					case R.layout.recycler_expandable_item:
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
					DatabaseService.getInstance().removeSubItem(expandable, subItem);
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
				mActionModeHelper.updateContextTitle(mAdapter.getSelectedItemCount());
				//We consume the event
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

				//Experimenting NEW feature
				mAdapter.setRestoreSelectionOnUndo(true);

				//New Undo Helper
				new UndoHelper(mAdapter, this)
						.withPayload(true)
						.withAction(UndoHelper.ACTION_REMOVE, new UndoHelper.OnActionListener() {
							@Override
							public boolean onPreAction() {
								//Don't consume the event
								//OR use UndoHelper.SimpleActionListener and Override only onPostAction()
								return false;
							}

							@Override
							public void onPostAction() {
								//Disable SwipeRefresh
								mRefreshHandler.sendEmptyMessage(1);
								mRefreshHandler.sendEmptyMessageDelayed(0, 20000);
								//Finish the action mode
								mActionModeHelper.destroyActionModeIfCan();
								logOrphanHeaders();
							}
						})
						.remove(mAdapter.getSelectedPositions(),
								findViewById(R.id.main_view), message,
								getString(R.string.undo), 20000);

				//We consume the event
				return true;

			case R.id.action_merge:
				if (mAdapter.getSelectedItemCount() > 1) {
					//Selected positions are sorted by default, we take the first item of the set
					int mainPosition = mAdapter.getSelectedPositions().get(0);
					mAdapter.removeSelection(mainPosition);
					StaggeredItem mainItem = (StaggeredItem) mAdapter.getItem(mainPosition);
					for (Integer position : mAdapter.getSelectedPositions()) {
						//Merge item - Save the modification in the memory for next refresh
						DatabaseService.getInstance().mergeItem(mainItem, (StaggeredItem) mAdapter.getItem(position));
					}
					//Remove merged item from the list
					mAdapter.removeAllSelectedItems();
					//Keep selection on mainItem & Skip default notification by calling addSelection
					mAdapter.addSelection(mainPosition);
					//Custom notification to bind again (ripple only)
					mAdapter.notifyItemChanged(mainPosition, "blink");
					//New title for context
					mActionModeHelper.updateContextTitle(mAdapter.getSelectedItemCount());
				}
				//We consume always the event, never finish the ActionMode
				return true;

			case R.id.action_split:
				if (mAdapter.getSelectedItemCount() == 1) {
					StaggeredItem mainItem = (StaggeredItem) mAdapter.getItem(mAdapter.getSelectedPositions().get(0));
					if (mainItem.getMergedItems() != null) {
						List<StaggeredItem> itemsToSplit = new ArrayList<>(mainItem.getMergedItems());
						for (StaggeredItem itemToSplit : itemsToSplit) {
							//Split item - Save the modification in the memory for next refresh
							DatabaseService.getInstance().splitItem(mainItem, itemToSplit);
							//We know the section object, so we can insert directly the item at the right position
							//The calculated position is then returned
							int position = mAdapter.addItemToSection(itemToSplit, mainItem.getHeader(), new DatabaseService.ItemComparatorById());
							mAdapter.toggleSelection(position);//Execute default notification
							mAdapter.notifyItemChanged(position, "blink");
						}
						//Custom notification to bind again (ripple only)
						mAdapter.notifyItemChanged(mAdapter.getGlobalPositionOf(mainItem), "blink");
						//New title for context
						mActionModeHelper.updateContextTitle(mAdapter.getSelectedItemCount());
					}
				}
				//We consume always the event, never finish the ActionMode
				return true;

			default:
				//If an item is not implemented we don't consume the event, so we finish the ActionMode
				return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
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
		if (mActionModeHelper.destroyActionModeIfCan()) return;
		//If SearchView is visible, back key cancels search and iconify it
		if (mSearchView != null && !mSearchView.isIconified()) {
			mSearchView.setIconified(true);
			return;
		}
		//Return to Overall View
		if (DatabaseService.getInstance().getDatabaseType() != 0) {
			MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.nav_overall);
			onNavigationItemSelected(menuItem);
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