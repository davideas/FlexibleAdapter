package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;

import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.samples.flexibleadapter.OverallAdapter;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.items.ScrollableUseCaseItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

/**
 * A fragment representing a list of Examples for FlexibleAdapter displayed with GridLayout.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FragmentOverall extends AbstractFragment {

	public static final String TAG = FragmentOverall.class.getSimpleName();

	/**
	 * Custom implementation of FlexibleAdapter
	 */
	private OverallAdapter mAdapter;


	public static FragmentOverall newInstance(int columnCount) {
		FragmentOverall fragment = new FragmentOverall();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragmentOverall() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Create overall items and Initialize RecyclerView
		if (savedInstanceState == null) {
			DatabaseService.getInstance().createOverallDatabase(getActivity().getResources());
		}
		initializeRecyclerView(savedInstanceState);
	}

	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		// Initialize Adapter and RecyclerView
		// OverallAdapter makes use of stableIds, I strongly suggest to implement 'item.hashCode()'

		// In this example the Adapter make uses of METHOD B and extends FlexibleAdapter: items
		// don't implement the AutoMap and don't implement create and binding methods: The Adapter
		// remains responsible to handling all view types.
		mAdapter = new OverallAdapter(getActivity());

		// Experimenting NEW features (v5.0.0)
		mAdapter.setOnlyEntryAnimation(true)
				.setAnimationInterpolator(new DecelerateInterpolator())
				.setAnimationInitialDelay(500L)
				.setAnimationDelay(70L);

		// Prepare the RecyclerView and attach the Adapter to it
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setItemViewCacheSize(0); //Setting ViewCache to 0 (default=2) will animate items better while scrolling down+up with LinearLayout
		mRecyclerView.setLayoutManager(createNewStaggeredGridLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change

		// After Adapter is attached to RecyclerView
		mAdapter.setLongPressDragEnabled(true);
		mRecyclerView.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getView() != null) { //Fix NPE when closing app before the execution of Runnable
					Snackbar.make(getView(), "Long press drag is enabled", Snackbar.LENGTH_SHORT).show();
				}
			}
		}, 4000L);

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setEnabled(true);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);

		// Add 2 Scrollable Headers
		mAdapter.showLayoutInfo(savedInstanceState == null);
		mAdapter.addScrollableHeader(new ScrollableUseCaseItem(
				getString(R.string.overall_use_case_title),
				getString(R.string.overall_use_case_description)));
	}

	@Override
	public void showNewLayoutInfo(MenuItem item) {
		super.showNewLayoutInfo(item);
		mAdapter.showLayoutInfo(true);
	}

	@Override
	protected LinearLayoutManager createNewLinearLayoutManager() {
		mAdapter.setAnimationEntryStep(true);
		return super.createNewLinearLayoutManager();
	}

	@Override
	protected GridLayoutManager createNewGridLayoutManager() {
		mAdapter.setAnimationEntryStep(false);
		GridLayoutManager gridLayoutManager = new SmoothScrollGridLayoutManager(getActivity(), mColumnCount);
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				// NOTE: If you use simple integers to identify the ViewType,
				// here, you should use them and not Layout integers
				switch (mAdapter.getItemViewType(position)) {
					case R.layout.recycler_scrollable_usecase_item:
					case R.layout.recycler_scrollable_layout_item:
						return mColumnCount;
					default:
						return 1;
				}
			}
		});
		return gridLayoutManager;
	}

	@Override
	protected StaggeredGridLayoutManager createNewStaggeredGridLayoutManager() {
		mAdapter.setAnimationEntryStep(true);
		return super.createNewStaggeredGridLayoutManager();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_overall, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem gridMenuItem = menu.findItem(R.id.action_list_type);
		if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
			gridMenuItem.setIcon(R.drawable.ic_view_agenda_white_24dp);
			gridMenuItem.setTitle(R.string.linear_layout);
		} else if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
			gridMenuItem.setIcon(R.drawable.ic_dashboard_white_24dp);
			gridMenuItem.setTitle(R.string.staggered_layout);
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

}