package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration;
import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.ExampleAdapter;
import eu.davidea.samples.flexibleadapter.MainActivity;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.dialogs.OnParameterSelectedListener;
import eu.davidea.samples.flexibleadapter.items.HeaderItem;
import eu.davidea.samples.flexibleadapter.items.ScrollableUseCaseItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FragmentHeadersSections extends AbstractFragment
        implements OnParameterSelectedListener {

    public static final String TAG = FragmentHeadersSections.class.getSimpleName();

    /**
     * Custom implementation of FlexibleAdapter
     */
    private ExampleAdapter mAdapter;


    public static FragmentHeadersSections newInstance(int columnCount) {
        FragmentHeadersSections fragment = new FragmentHeadersSections();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FragmentHeadersSections() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Settings for FlipView
        FlipView.resetLayoutAnimationDelay(true, 1000L);

        // Create New Database and Initialize RecyclerView
        if (savedInstanceState == null) {
            DatabaseService.getInstance().createHeadersSectionsDatabase(400, 100);
        }
        initializeRecyclerView(savedInstanceState);

        // Restore FAB button and icon
        initializeFab();

        // Settings for FlipView
        FlipView.stopLayoutAnimation();
    }

    @SuppressWarnings({"ConstantConditions", "NullableProblems"})
    private void initializeRecyclerView(Bundle savedInstanceState) {
        // Initialize Adapter and RecyclerView
        // ExampleAdapter makes use of stableIds, I strongly suggest to implement 'item.hashCode()'
        FlexibleAdapter.useTag("HeadersSectionsAdapter");
        mAdapter = new ExampleAdapter(DatabaseService.getInstance().getDatabaseList(), getActivity());
        // Experimenting NEW features (v5.0.0)
        mAdapter.setNotifyMoveOfFilteredItems(true)
                .setAnimationOnScrolling(DatabaseConfiguration.animateOnScrolling);
        mRecyclerView = getView().findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
        mRecyclerView.setHasFixedSize(true); //Size of RV will not change
        // NOTE: Use default item animator 'canReuseUpdatedViewHolder()' will return true if
        // a Payload is provided. FlexibleAdapter is actually sending Payloads onItemChange.
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new FlexibleItemDecoration(getActivity())
//				.addItemViewType(R.layout.recycler_header_item, 8, 8, 8, 8)
.addItemViewType(R.layout.recycler_simple_item, 0, 8, 0, 8)
.withSectionGapOffset(24)
.withEdge(true));

        // Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
        FastScroller fastScroller = getView().findViewById(R.id.fast_scroller);
        fastScroller.addOnScrollStateChangeListener((MainActivity) getActivity());
        mAdapter.setFastScroller(fastScroller);

        mAdapter.setLongPressDragEnabled(true)
                .setHandleDragEnabled(true)
                .setSwipeEnabled(true)
                .setStickyHeaderElevation(5)
                .setUnlinkAllItemsOnRemoveHeaders(true)
                // Show Headers at startUp, 1st call, correctly executed, no warning log message!
                .setDisplayHeadersAtStartUp(true)
                .setStickyHeaders(true);
        // Simulate developer 2nd call mistake, now it's safe, not executed, no warning log message!
        //.setDisplayHeadersAtStartUp(true)
        // Simulate developer 3rd call mistake, still safe, not executed, warning log message displayed!
        //.showAllHeaders();

        SwipeRefreshLayout swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(true);
        mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, Mode.IDLE);

        // Add 2 Scrollable Headers and 1 Footer
        mAdapter.addUserLearnedSelection(savedInstanceState == null);
        mAdapter.addScrollableHeaderWithDelay(new ScrollableUseCaseItem(
                getString(R.string.headers_sections_use_case_title),
                getString(R.string.headers_sections_use_case_description)), 900L, false);
        mAdapter.addScrollableFooter();
    }

    int count = 1;

    @Override
    public void performFabAction() {
        int position = mAdapter.getStickyPosition();
        if (position > 0) {
            HeaderItem header = (HeaderItem) mAdapter.getItem(position);
            header.setTitle("New sticky title " + count++);
            mAdapter.updateItem(header);
        }
//		BottomSheetSectionDialog bottomSheetDialogFragment = BottomSheetSectionDialog.newInstance(R.layout.bottom_sheet_headers_sections, this);
//		bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), BottomSheetSectionDialog.TAG);
    }

    @Override
    public void showNewLayoutInfo(MenuItem item) {
        super.showNewLayoutInfo(item);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.showLayoutInfo(false);
    }

    @Override
    public void onParameterSelected(int itemType, int referencePosition, int childPosition) {
        if (referencePosition < 0) return;
        int scrollTo, id;
        IHeader referenceHeader = getReferenceList().get(referencePosition);
        Log.d(TAG, "Adding New Item: ItemType=" + itemType +
                " referencePosition=" + referencePosition +
                " childPosition=" + childPosition);
        switch (itemType) {
            case 1: //Expandable
                id = mAdapter.getItemCountOfTypes(R.layout.recycler_expandable_item) + 1;
                ISectionable sectionableExpandable = DatabaseService.newExpandableItem(id, referenceHeader);
                mAdapter.addItemToSection(sectionableExpandable, referenceHeader, childPosition);
                scrollTo = mAdapter.getGlobalPositionOf(referenceHeader);
                break;
//			case 2: //Expandable Header
//				id = mAdapter.getItemCountOfTypes(R.layout.recycler_expandable_header_item) + 1;
//				ExpandableHeaderItem expandableHeader = DatabaseService.newExpandableSectionItem(id);
//				expandableHeader.setExpanded(false);
//				mAdapter.addSection(expandableHeader, referenceHeader);
//				scrollTo = mAdapter.getGlobalPositionOf(expandableHeader);
//				break;
//			case 3: //Header
//				id = mAdapter.getItemCountOfTypes(R.layout.recycler_header_item) + 1;
//				IHeader header = DatabaseService.newHeader(id);
//				mAdapter.addSection(header, referenceHeader);
//				scrollTo = mAdapter.getGlobalPositionOf(header);
//				break;
            default: //case 0 = Simple Item
                id = mAdapter.getItemCountOfTypes(R.layout.recycler_expandable_item) + 1;
                ISectionable sectionable = DatabaseService.newSimpleItem(id, referenceHeader);
                mAdapter.addItemToSection(sectionable, referenceHeader, childPosition);
                scrollTo = mAdapter.getGlobalPositionOf(referenceHeader);
        }

        // With Sticky Headers enabled, this seems necessary to give
        // time at the RV to be in correct state before scrolling
        final int scrollToFinal = scrollTo;
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(scrollToFinal);
            }
        });
    }

    @Override
    public List<IHeader> getReferenceList() {
        return mAdapter.getHeaderItems();
    }

    @Override
    protected GridLayoutManager createNewGridLayoutManager() {
        GridLayoutManager gridLayoutManager = new SmoothScrollGridLayoutManager(getActivity(), mColumnCount);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getItem(position).getSpanSize(mColumnCount, position);
            }
        });
        return gridLayoutManager;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.v(TAG, "onCreateOptionsMenu called!");
        inflater.inflate(R.menu.menu_sections, menu);
        mListener.initSearchView(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_auto_collapse).setVisible(false);
        menu.findItem(R.id.action_expand_collapse_all).setVisible(false);
    }

}