package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.Payload;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.ExampleAdapter;
import eu.davidea.samples.flexibleadapter.MainActivity;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.animators.FadeInDownAnimator;
import eu.davidea.samples.flexibleadapter.items.ProgressItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;
import eu.davidea.utils.Utils;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
@SuppressWarnings("ConstantConditions")
public class FragmentEndlessScrolling extends AbstractFragment
		implements FlexibleAdapter.EndlessScrollListener {

	public static final String TAG = FragmentEndlessScrolling.class.getSimpleName();

	private ExampleAdapter mAdapter;

	public static FragmentEndlessScrolling newInstance(int columnCount) {
		FragmentEndlessScrolling fragment = new FragmentEndlessScrolling();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragmentEndlessScrolling() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		// Create New Database and Initialize RecyclerView
		DatabaseService.getInstance().createEndlessDatabase(0); //N. of items
		initializeRecyclerView(savedInstanceState);

		// Settings for FlipView
		FlipView.stopLayoutAnimation();
	}

	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		// Initialize Adapter and RecyclerView
		// ExampleAdapter makes use of stableIds, I strongly suggest to implement 'item.hashCode()'
		mAdapter = new ExampleAdapter(DatabaseService.getInstance().getDatabaseList(), getActivity());
		// Experimenting NEW features (v5.0.0)
		mAdapter.setAutoScrollOnExpand(true)
				//.setAnimateToLimit(Integer.MAX_VALUE) //Use the default value
				.setNotifyMoveOfFilteredItems(true) //When true, filtering on big list is very slow, not in this case!
				.setNotifyChangeOfUnfilteredItems(true) //We have highlighted text while filtering, so let's enable this feature to be consistent with the active filter
				.setAnimationOnScrolling(DatabaseConfiguration.animateOnScrolling)
				.setAnimationOnReverseScrolling(true);
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		// NOTE: Use the custom FadeInDownAnimator for ALL notifications for ALL items,
		// but ScrollableFooterItem implements AnimatedViewHolder with a unique animation: SlideInUp!
		mRecyclerView.setItemAnimator(new FadeInDownAnimator());

		// Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
		mAdapter.setFastScroller((FastScroller) getView().findViewById(R.id.fast_scroller),
				Utils.getColorAccent(getActivity()), (MainActivity) getActivity());
		// Experimenting NEW features (v5.0.0)
		mAdapter.setLongPressDragEnabled(true) //Enable long press to drag items
				.setHandleDragEnabled(true) //Enable drag using handle view
				.setSwipeEnabled(true) //Enable swipe items
				.setDisplayHeadersAtStartUp(true); //Show Headers at startUp!

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setEnabled(true);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);

		// EndlessScrollListener - OnLoadMore (v5.0.0)
		mAdapter.setEndlessScrollListener(this, new ProgressItem())
//				.setEndlessPageSize(3)
				.setEndlessTargetCount(15);
//				.setEndlessScrollThreshold(1); //Default=1

		// Add 2 Scrollable Headers and 1 Footer items
		mAdapter.addUserLearnedSelection(savedInstanceState == null);
		mAdapter.showLayoutInfo();
		mAdapter.addScrollableFooter();
		mAdapter.addScrollableHeaderWithDelay(DatabaseService.newExpandableSectionItem(111), 5000L, true);
		mAdapter.addScrollableFooterWithDelay(DatabaseService.newExpandableSectionItem(999), 5000L, false);
	}

	@Override
	public void showNewLayoutInfo(MenuItem item) {
		super.showNewLayoutInfo(item);
		mAdapter.showLayoutInfo();
	}


	/**
	 * No more data to load.
	 * <p>This method is called if any limit is reached (<b>targetCount</b> or <b>pageSize</b>
	 * must be set) AND if new data is <u>temporary</u> unavailable (ex. no connection or no
	 * new updates remotely). If no new data, a {@link FlexibleAdapter#notifyItemChanged(int, Object)}
	 * with a payload {@link Payload#NO_MORE_LOAD} is triggered on the <i>progressItem</i>.</p>
	 *
	 * @param newItemsSize the last size of the new items loaded
	 * @see FlexibleAdapter#setEndlessTargetCount(int)
	 * @see FlexibleAdapter#setEndlessPageSize(int)
	 * @since 5.0.0-rc1
	 */
	@Override
	public void noMoreLoad(int newItemsSize) {
		Log.d(TAG, "newItemsSize=" + newItemsSize);
		Log.d(TAG, "Total pages loaded=" + mAdapter.getEndlessCurrentPage());
		Log.d(TAG, "Total items loaded=" + mAdapter.getItemCount(false));
	}

	/**
	 * Loads more data.
	 * <p>Use {@code lastPosition} and {@code currentPage} to know what to load next.</p>
	 * {@code lastPosition} is the count of the main items without Scrollable Headers.
	 *
	 * @param lastPosition the position of the last main item in the adapter
	 * @param currentPage  the current page
	 * @since 5.0.0-b6
	 * <br/>5.0.0-rc1 added {@code lastPosition} and {@code currentPage} as parameters
	 */
	@Override
	public void onLoadMore(int lastPosition, int currentPage) {
		// We don't want load more items when searching into the current Collection!
		// Alternatively, for a special filter, if we want load more items when filter is active, the
		// new items that arrive from remote, should be already filtered, before adding them to the Adapter!
		if (mAdapter.hasSearchText()) {
			mAdapter.onLoadMoreComplete(null);
			return;
		}
		// Simulating asynchronous call
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				final List<AbstractFlexibleItem> newItems = new ArrayList<>();

				// Simulating success/failure with Random
				int count = new Random().nextInt(7);
				int totalItemsOfType = mAdapter.getItemCountOfTypes(R.layout.recycler_expandable_item);
				for (int i = 1; i <= count; i++) {
					newItems.add(DatabaseService.newSimpleItem(totalItemsOfType + i, null));
				}

				// Callback the Adapter to notify the change:
				// - New items will be added to the end of the main list
				// - When list is null or empty and limits are reached, Endless scroll will be disabled
				mAdapter.onLoadMoreComplete(newItems, 5000L);
				DatabaseService.getInstance().addAll(newItems);
				// It's better to read the page after adding new items!
				Log.d(TAG, "EndlessCurrentPage=" + mAdapter.getEndlessCurrentPage());
				Log.d(TAG, "EndlessPageSize=" + mAdapter.getEndlessPageSize());
				Log.d(TAG, "EndlessTargetCount=" + mAdapter.getEndlessTargetCount());

				// Expand all Expandable items: Not Expandable items are automatically skipped/ignored!
				for (AbstractFlexibleItem item : newItems) {
					// Simple expansion is performed:
					// - Automatic scroll is performed
					//mAdapter.expand(item);

					// Initialization is performed:
					// - Expanded status is ignored(WARNING: possible subItem duplication)
					// - Automatic scroll is skipped
					mAdapter.expand(item, true);
				}

				// Notify user
				if (getActivity() != null && newItems.size() > 0) {
					Toast.makeText(getActivity(),
							"Simulated: " + newItems.size() + " new items arrived :-)",
							Toast.LENGTH_SHORT).show();
				}
			}
		}, 2000);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		Log.v(TAG, "onCreateOptionsMenu called!");
		inflater.inflate(R.menu.menu_endless, menu);
		mListener.initSearchView(menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		Log.v(TAG, "onPrepareOptionsMenu called!");

		MenuItem gridMenuItem = menu.findItem(R.id.action_list_type);
		if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
			gridMenuItem.setIcon(R.drawable.ic_view_agenda_white_24dp);
			gridMenuItem.setTitle(R.string.linear_layout);
		} else {
			gridMenuItem.setIcon(R.drawable.ic_view_grid_white_24dp);
			gridMenuItem.setTitle(R.string.grid_layout);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_list_type)
			mAdapter.setAnimationOnScrolling(true);
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected GridLayoutManager createNewGridLayoutManager() {
		GridLayoutManager gridLayoutManager = new SmoothScrollGridLayoutManager(getActivity(), mColumnCount);
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				// NOTE: If you use simple integers to identify the ViewType,
				// here, you should use them and not Layout integers
				switch (mAdapter.getItemViewType(position)) {
					case R.layout.recycler_scrollable_header_item:
					case R.layout.recycler_scrollable_footer_item:
					case R.layout.recycler_scrollable_layout_item:
					case R.layout.recycler_scrollable_uls_item:
					case R.layout.progress_item:
						return mColumnCount;
					default:
						return 1;
				}
			}
		});
		return gridLayoutManager;
	}

}