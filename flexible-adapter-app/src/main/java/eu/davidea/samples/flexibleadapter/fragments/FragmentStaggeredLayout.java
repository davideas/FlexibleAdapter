package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Random;

import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.samples.flexibleadapter.ExampleAdapter;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.models.StaggeredHeaderItem;
import eu.davidea.samples.flexibleadapter.models.StaggeredItem;
import eu.davidea.samples.flexibleadapter.models.StaggeredItemStatus;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
@SuppressWarnings("ConstantConditions")
public class FragmentStaggeredLayout extends AbstractFragment {

	public static final String TAG = FragmentStaggeredLayout.class.getSimpleName();

	private ExampleAdapter mAdapter;

	public static FragmentStaggeredLayout newInstance(int columnCount) {
		FragmentStaggeredLayout fragment = new FragmentStaggeredLayout();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragmentStaggeredLayout() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		//Create New Database and Initialize RecyclerView
		DatabaseService.getInstance().createStaggeredDatabase(getActivity());
		initializeRecyclerView(savedInstanceState);
	}

	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		//Restore FAB icon
		FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
		fab.setImageResource(R.drawable.fab_add);

		//Initialize Adapter and RecyclerView
		mAdapter = new ExampleAdapter(getActivity());
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewStaggeredGridLayoutManager());
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

		//Experimenting NEW features (v5.0.0)
		mAdapter.setDisplayHeadersAtStartUp(true)//Show Headers at startUp!
				.setPermanentDelete(true)
				.setAnimationOnScrolling(true)
				.setAnimationOnReverseScrolling(true);

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);

		//Add sample HeaderView items on the top (not belongs to the library)
		mAdapter.showLayoutInfo(savedInstanceState == null);
	}

	@Override
	public int getContextMenuResId() {
		return R.menu.menu_staggered_context;
	}

	@Override
	public void showNewLayoutInfo(MenuItem item) {
		super.showNewLayoutInfo(item);
		mAdapter.showLayoutInfo(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		Log.v(TAG, "onCreateOptionsMenu called!");
		inflater.inflate(R.menu.menu_staggered, menu);
		mListener.initSearchView(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_reset) {
			DatabaseService.getInstance().resetItems();
			mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList(), true);
		} else if (id == R.id.action_delete) {
			DatabaseService.getInstance().removeAll();
			mAdapter.updateDataSet(null, true);
			//This is necessary if we call updateDataSet() and not removeItems
			DatabaseService.getInstance().resetHeaders();
			//Change fab action (ADD NEW ITEM UNTIL 15)
			FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
			fab.setImageResource(R.drawable.fab_add);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void performFabAction() {
		//Simulate changing status
		StaggeredItemStatus status = StaggeredItemStatus.values()[new Random().nextInt(StaggeredItemStatus.values().length - 1)];
		StaggeredHeaderItem headerItem = DatabaseService.getInstance().getHeaderByStatus(status);
		int scrollTo;

		//CALCULATE POSITION FOR
		//- Useful in ALL situations of moving/adding an item.
		//- Position is calculated based on the custom Comparator implementation.
		//- Comparator object should sort the Section (in the eventuality the header is hidden)
		//  and the Item into the Section (see the Class ItemComparatorByGroup for an
		//  example of implementation).
		//- It respects the custom sort, also for a non-displayed section!
		//- When moving/adding, the relative header item will be automatically displayed too
		//  if not yet visible.
		if (mAdapter.getItemCountOfTypes(R.layout.recycler_staggered_item) >= 15) {
			//FAB Action: Move Item
			scrollTo = moveItem(status, headerItem);
		}

		//ADD ITEM TO SECTION
		//- Useful only to add new items of every type
		//- Comparator object should sort the Section (in the eventuality the header is hidden)
		//  and the Item into the Section (see the Class ItemComparatorByGroup for an
		//  example of implementation).
		//- The relative header will be automatically displayed too if not yet visible.
		//- if you already know the relative index of the new item, then call the correct
		//  method without the Comparator object.
		else {
			//FAB Action: Add Item
			scrollTo = addItem(status, headerItem);
		}

		//Show to the user the result of the addition/changes
		smoothScrollTo(scrollTo, headerItem);
		refreshItem(scrollTo);
		clearEmptySections();
	}

	private int addItem(StaggeredItemStatus status, StaggeredHeaderItem headerItem) {
		StaggeredItem staggeredItem = DatabaseService.newStaggeredItem(
				DatabaseService.getInstance().getMaxStaggeredId(), headerItem);
		staggeredItem.setStatus(status);//!!!

		//The section object is known
		mAdapter.addItemToSection(staggeredItem, staggeredItem.getHeader(),
				new DatabaseService.ItemComparatorByGroup());
		//Add Item to the Database as well for next refresh
		DatabaseService.getInstance().addItem(staggeredItem, new DatabaseService.ItemComparatorById());

		//Change fab action (MOVE ITEM)
		if (mAdapter.getItemCountOfTypes(R.layout.recycler_staggered_item) >= 15) {
			FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
			fab.setImageResource(R.drawable.ic_sort_white_24dp);
		}

		//Retrieve the final position due to a possible hidden header became now visible!
		int scrollTo = mAdapter.getGlobalPositionOf(staggeredItem);
		Log.d(TAG, "Creating New Item " + staggeredItem + " at position " + scrollTo);
		return scrollTo;
	}

	private int moveItem(StaggeredItemStatus status, StaggeredHeaderItem headerItem) {
		StaggeredItem staggeredItem = DatabaseService.getInstance().getRandomStaggeredItem();
		if (!staggeredItem.getHeader().equals(headerItem)) {
			//Before calculate the position, change header/section
			staggeredItem.setStatus(status);//!!!
			staggeredItem.setHeader(headerItem);

			int toPosition = mAdapter.calculatePositionFor(staggeredItem, new DatabaseService.ItemComparatorByGroup());
			//Move item to just calculated position under the correct section
			mAdapter.moveItem(mAdapter.getGlobalPositionOf(staggeredItem), toPosition, null);
		}
		//Retrieve the final position due to a possible hidden header became now visible!
		int scrollTo = mAdapter.getGlobalPositionOf(staggeredItem);
		Log.d(TAG, "Moving Item to position" + scrollTo);
		return scrollTo;
	}

	private void smoothScrollTo(final int scrollTo, final StaggeredHeaderItem headerItem) {
		//Smooth scrolling should be delayed because the just added item could not be yet
		// animated/rendered by the LayoutManager
		mRecyclerView.postDelayed(new Runnable() {
			@Override
			public void run() {
				StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
				int[] first = layoutManager.findFirstCompletelyVisibleItemPositions(null);
				int[] last = layoutManager.findLastCompletelyVisibleItemPositions(null);
				int headerPosition = mAdapter.getGlobalPositionOf(headerItem);
				if (scrollTo <= first[0]) {
					Log.d(TAG, "ScrollTo headerPosition=" + headerPosition);
					mRecyclerView.smoothScrollToPosition(Math.max(0, headerPosition));
				} else if (scrollTo >= last[0]) {
					Log.d(TAG, "ScrollTo itemPosition=" + scrollTo);
					mRecyclerView.smoothScrollToPosition(Math.min(scrollTo, mAdapter.getItemCount()));
				}
			}
		}, 200L);
	}

	private void refreshItem(final int position) {
		//We notify the item that it is changed, bind it again (change color)
		mRecyclerView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mAdapter.notifyItemChanged(position, "blink");
			}
		}, 400L);
	}

	private void clearEmptySections() {
		//We remove the header item that is without items (empty sections)
		for (final IHeader header : mAdapter.getHeaderItems()) {
			Log.d(TAG, "Header=" + header.toString() + " Items=" + mAdapter.getSectionItems(header).size());
			if (mAdapter.getSectionItems(header).size() == 0) {
				//noinspection Range
				mAdapter.removeItem(mAdapter.getGlobalPositionOf(header));
			} else {
				mAdapter.notifyItemChanged(mAdapter.getGlobalPositionOf(header));
			}
		}
	}

}