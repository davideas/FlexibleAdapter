package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
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
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.ExampleAdapter;
import eu.davidea.samples.flexibleadapter.MainActivity;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.models.ProgressItem;
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
		//Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		//Create New Database and Initialize RecyclerView
		DatabaseService.getInstance().createEndlessDatabase(50);
		initializeRecyclerView(savedInstanceState);

		//Settings for FlipView
		FlipView.stopLayoutAnimation();
	}

	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		mAdapter = new ExampleAdapter(getActivity());
		//Experimenting NEW features (v5.0.0)
		mAdapter.setAutoScrollOnExpand(true)
				.setHandleDragEnabled(true)
				.setAnimationOnScrolling(true)
				.setAnimationOnReverseScrolling(true);
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		mRecyclerView.setItemAnimator(new DefaultItemAnimator() {
			@Override
			public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
				//NOTE: This allows to receive Payload objects when notifyItemChanged is called by the Adapter!!!
				return true;
			}
		});
		//mRecyclerView.setItemAnimator(new SlideInRightAnimator());
		//mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.divider));

		//Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
		mAdapter.setFastScroller((FastScroller) getActivity().findViewById(R.id.fast_scroller),
				Utils.getColorAccent(getActivity()), (MainActivity) getActivity());
		//Experimenting NEW features (v5.0.0)
		mAdapter.setLongPressDragEnabled(true);//Enable long press to drag items
		mAdapter.setSwipeEnabled(true);//Enable swipe items
		mAdapter.setDisplayHeadersAtStartUp(true);//Show Headers at startUp!

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);

		//EndlessScrollListener - OnLoadMore (v5.0.0)
		mAdapter.setEndlessScrollListener(this, new ProgressItem());
		mAdapter.setEndlessScrollThreshold(1);//Default=1

		//Add sample HeaderView items on the top (not belongs to the library)
		mAdapter.addUserLearnedSelection(savedInstanceState == null);
		mAdapter.showLayoutInfo(savedInstanceState == null);
	}

	@Override
	public void showNewLayoutInfo(MenuItem item) {
		super.showNewLayoutInfo(item);
		mAdapter.showLayoutInfo(true);
	}

	/**
	 * Loads more data.
	 */
	@Override
	public void onLoadMore() {
		//We don't want load more items when searching into the current Collection!
		//Alternatively, for a special filter, if we want load more items when filter is active, the
		// new items that arrive from remote, should be already filtered, before adding them to the Adapter!
		if (mAdapter.hasSearchText()) {
			mAdapter.onLoadMoreComplete(null);
			return;
		}
		Log.i(TAG, "onLoadMore invoked!");
		//Simulating asynchronous call
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				final List<AbstractFlexibleItem> newItems = new ArrayList<AbstractFlexibleItem>();

				//Simulating success/failure
				int count = new Random().nextInt(5);
				int totalItemsOfType = mAdapter.getItemCountOfTypes(R.layout.recycler_expandable_item);
				for (int i = 1; i <= count; i++) {
					if (i % 2 != 0) {
						newItems.add(DatabaseService.newSimpleItem(totalItemsOfType + i, null));
					} else {
						newItems.add(DatabaseService.newExpandableItem(totalItemsOfType + i, null));
					}
				}

				//Callback the Adapter to notify the change:
				//- New items will be added to the end of the list
				//- When list is null or empty, ProgressItem will be hidden
				mAdapter.onLoadMoreComplete(newItems);
				DatabaseService.getInstance().addAll(newItems);

				//Expand all Expandable items: Not Expandable items are automatically skipped/ignored!
				for (AbstractFlexibleItem item : newItems) {
					//Simple expansion is performed:
					// - Automatic scroll is performed
					//mAdapter.expand(item);

					//Initialization is performed:
					// - Expanded status is ignored(WARNING: possible subItem duplication)
					// - Automatic scroll is skipped
					mAdapter.expand(item, true);
				}

				//Notify user
				String message = (newItems.size() > 0 ?
						"Simulated: " + newItems.size() + " new items arrived :-)" :
						"Simulated: No more items to load :-(");
				Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			}
		}, 3000);
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
				//NOTE: If you use simple integer to identify the ViewType,
				//here, you should use them and not Layout integers
				switch (mAdapter.getItemViewType(position)) {
					case R.layout.recycler_layout_item:
					case R.layout.recycler_uls_item:
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