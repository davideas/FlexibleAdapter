package eu.davidea.examples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import eu.davidea.examples.flexibleadapter.OverallAdapter;
import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.examples.flexibleadapter.services.DatabaseService;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;

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
		//Create overall items and Initialize RecyclerView
		DatabaseService.getInstance().createOverallItemsDatabase(getActivity().getResources());
		initializeRecyclerView(savedInstanceState);
	}

	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		mAdapter = new OverallAdapter(getActivity());
		//Experimenting NEW features (v5.0.0)
		mAdapter.setAnimationOnScrolling(true);
		mAdapter.setAnimationOnReverseScrolling(true);
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewGridLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		//mRecyclerView.setItemAnimator(new SlideInRightAnimator());

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		mListener.onAdapterChange(swipeRefreshLayout, mRecyclerView);
	}

	private GridLayoutManager createNewGridLayoutManager() {
		GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), mColumnCount);
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				//NOTE: If you use simple integer to identify the ViewType,
				//here, you should use them and not Layout integers
				switch (mAdapter.getItemViewType(position)) {
					//TODO: Header View span = 2
					default:
						return 1;
				}
			}
		});
		return gridLayoutManager;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_overall, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

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
		int id = item.getItemId();
		if (id == R.id.action_list_type) {
			if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
				mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(getActivity()));
				item.setIcon(R.drawable.ic_view_grid_white_24dp);
				item.setTitle(R.string.grid_layout);
			} else {
				mRecyclerView.setLayoutManager(createNewGridLayoutManager());
				item.setIcon(R.drawable.ic_view_agenda_white_24dp);
				item.setTitle(R.string.linear_layout);
			}
		}
		return super.onOptionsItemSelected(item);
	}

}