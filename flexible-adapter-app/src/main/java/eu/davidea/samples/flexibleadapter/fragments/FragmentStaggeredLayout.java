package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Random;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.Payload;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration;
import eu.davidea.flexibleadapter.common.TopSnappedSmoothScroller;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.items.ScrollableUseCaseItem;
import eu.davidea.samples.flexibleadapter.items.StaggeredHeaderItem;
import eu.davidea.samples.flexibleadapter.items.StaggeredItem;
import eu.davidea.samples.flexibleadapter.items.StaggeredItemStatus;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
@SuppressWarnings("ConstantConditions")
public class FragmentStaggeredLayout extends AbstractFragment {

    public static final String TAG = FragmentStaggeredLayout.class.getSimpleName();

    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;

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

        // Create New Database and Initialize RecyclerView
        if (savedInstanceState == null) {
            DatabaseService.getInstance().createStaggeredDatabase(getActivity());
        }
        initializeRecyclerView(savedInstanceState);

        // Restore FAB button and icon
        initializeFab();
    }

    @SuppressWarnings({"ConstantConditions", "NullableProblems"})
    private void initializeRecyclerView(Bundle savedInstanceState) {
        // Initialize Adapter and RecyclerView
        // ExampleAdapter makes use of stableIds, I strongly suggest to implement 'item.hashCode()'
        FlexibleAdapter.useTag("StaggeredLayoutAdapter");
        mAdapter = new FlexibleAdapter<>(DatabaseService.getInstance().getDatabaseList(), getActivity());
        mRecyclerView = getView().findViewById(R.id.recycler_view);
        // Customize the speed of the smooth scroll.
        // NOTE: Every time you change this value you MUST recreate the LayoutManager instance
        // and to assign it again to the RecyclerView!
        TopSnappedSmoothScroller.MILLISECONDS_PER_INCH = 33f; //Make faster the smooth scroll
        mRecyclerView.setLayoutManager(createNewStaggeredGridLayoutManager());
        // This value is restored to 100f (default) right here, because it is used in the
        // constructor by Android. If we don't change it now, others LayoutManager will be
        // impacted too by the above modification!
        TopSnappedSmoothScroller.MILLISECONDS_PER_INCH = 100f;

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true); //Size of RV will not change
        // NOTE: Use default item animator 'canReuseUpdatedViewHolder()' will return true if
        // a Payload is provided. FlexibleAdapter is actually sending Payloads onItemChange.
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new FlexibleItemDecoration(getActivity())
                .addItemViewType(R.layout.recycler_staggered_item, 8)
                .withEdge(true));

        // Experimenting NEW features (v5.0.0)
        mAdapter.setDisplayHeadersAtStartUp(true) //Show Headers at startUp!
                .setNotifyMoveOfFilteredItems(true)
                .setPermanentDelete(true) //Default=true
                .setOnlyEntryAnimation(true);

        SwipeRefreshLayout swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(true);
        mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, Mode.IDLE);

        // Add 1 Scrollable Header
        mAdapter.addScrollableHeader(new ScrollableUseCaseItem(
                getString(R.string.staggered_use_case_title),
                getString(R.string.staggered_use_case_description)));
    }

    @Override
    public int getContextMenuResId() {
        return R.menu.menu_staggered_context;
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
            // This is necessary if we call updateDataSet() and not removeItems
            DatabaseService.getInstance().resetHeaders();
            // Change fab action (ADD NEW ITEM UNTIL 15)
            FloatingActionButton fab = getActivity().findViewById(R.id.fab);
            fab.setImageResource(R.drawable.fab_add);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void performFabAction() {
        // Simulate changing status
        StaggeredItemStatus status = StaggeredItemStatus.values()[new Random().nextInt(StaggeredItemStatus.values().length - 1)];
        StaggeredHeaderItem headerItem = DatabaseService.getInstance().getHeaderByStatus(status);
        int scrollTo;

        // CALCULATE POSITION FOR
        // - Useful in ALL situations of moving/adding an item.
        // - Position is calculated based on the custom Comparator implementation.
        // - Comparator object should sort the Section (in the eventuality the header is hidden)
        //   and the Item into the Section (see the Class ItemComparatorByGroup for an
        //   example of implementation).
        // - It respects the custom sort, also for a non-displayed section!
        // - When moving/adding, the relative header item will be automatically displayed too
        //   if not yet visible.
        if (mAdapter.getItemCountOfTypes(R.layout.recycler_staggered_item) >= 15) {
            //FAB Action: Move Item
            scrollTo = moveItem(status, headerItem);
        }

        // ADD ITEM TO SECTION
        // - Useful only to add new items of every type
        // - Comparator object should sort the Section (in the eventuality the header is hidden)
        //   and the Item into the Section (see the Class ItemComparatorByGroup for an
        //   example of implementation).
        // - The relative header will be automatically displayed too if not yet visible.
        // - if you already know the relative index of the new item, then call the correct
        //   method without the Comparator object.
        else {
            // FAB Action: Add Item
            scrollTo = addItem(status, headerItem);
        }

        // Show to the user the result of the addition/changes
        smoothScrollTo(scrollTo, headerItem);
        refreshItem(scrollTo);
        clearEmptySections();
    }

    private int addItem(StaggeredItemStatus status, StaggeredHeaderItem headerItem) {
        StaggeredItem staggeredItem = DatabaseService.newStaggeredItem(
                DatabaseService.getInstance().getMaxStaggeredId(), headerItem);
        staggeredItem.setStatus(status);//!!!

        // The section object is known
        mAdapter.addItemToSection(staggeredItem, staggeredItem.getHeader(),
                new DatabaseService.ItemComparatorByGroup());
        // Add Item to the Database as well for next refresh
        DatabaseService.getInstance().addItem(staggeredItem, new DatabaseService.ItemComparatorById());

        // Change fab action (MOVE ITEM)
        if (mAdapter.getItemCountOfTypes(R.layout.recycler_staggered_item) >= 15) {
            FloatingActionButton fab = getActivity().findViewById(R.id.fab);
            fab.setImageResource(R.drawable.ic_sort_white_24dp);
        }

        // Retrieve the final position due to a possible hidden header became now visible!
        int scrollTo = mAdapter.getGlobalPositionOf(staggeredItem);
        Log.d(TAG, "Creating New Item " + staggeredItem + " at position " + scrollTo);
        return scrollTo;
    }

    private int moveItem(StaggeredItemStatus status, StaggeredHeaderItem headerItem) {
        StaggeredItem staggeredItem = DatabaseService.getInstance().getRandomStaggeredItem();
        if (!staggeredItem.getHeader().equals(headerItem)) {
            // Before calculate the position, change header/section
            staggeredItem.setStatus(status);//!!!
            staggeredItem.setHeader(headerItem);

            int toPosition = mAdapter.calculatePositionFor(staggeredItem, new DatabaseService.ItemComparatorByGroup());
            // Move item to just calculated position under the correct section
            mAdapter.moveItem(mAdapter.getGlobalPositionOf(staggeredItem), toPosition, Payload.MOVE);
            DatabaseService.getInstance().sort(new DatabaseService.ItemComparatorById());
        }
        // Retrieve the final position due to a possible hidden header became now visible!
        int scrollTo = mAdapter.getGlobalPositionOf(staggeredItem);
        Log.d(TAG, "Moving Item " + staggeredItem + " to position" + scrollTo);
        return scrollTo;
    }

    private void smoothScrollTo(final int scrollTo, final StaggeredHeaderItem headerItem) {
        // Smooth scrolling should be delayed because the just added item could not be yet
        // animated/rendered by the LayoutManager
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int first = mAdapter.getFlexibleLayoutManager().findFirstCompletelyVisibleItemPosition();
                int last = mAdapter.getFlexibleLayoutManager().findLastCompletelyVisibleItemPosition();
                int headerPosition = mAdapter.getGlobalPositionOf(headerItem);
                if (scrollTo <= first) {
                    Log.d(TAG, "ScrollTo headerPosition=" + headerPosition);
                    mRecyclerView.smoothScrollToPosition(Math.max(0, headerPosition));
                } else if (scrollTo >= last) {
                    Log.d(TAG, "ScrollTo itemPosition=" + scrollTo);
                    mRecyclerView.smoothScrollToPosition(Math.min(scrollTo, mAdapter.getItemCount()));
                }
            }
        }, 200L);
    }

    private void refreshItem(final int position) {
        // We notify the item that it is changed, bind it again (change color)
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyItemChanged(position, "blink");
            }
        }, 400L);
    }

    private void clearEmptySections() {
        // We remove the header item that is without items (empty sections)
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