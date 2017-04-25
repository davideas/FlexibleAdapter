package eu.davidea.samples.flexibleadapter.fragments;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.databinding.BindingFlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.animators.GarageDoorItemAnimator;
import eu.davidea.samples.flexibleadapter.items.HeaderItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FragmentDataBinding extends AbstractFragment {

	public static final String TAG = FragmentDataBinding.class.getSimpleName();

	private ObservableArrayList<AbstractFlexibleItem> items = new ObservableArrayList<>();

	private int fabClickedTimes = 0;
	/**
	 * Custom implementation of FlexibleAdapter
	 */
	private BindingFlexibleAdapter<AbstractFlexibleItem> mAdapter;


	public static FragmentDataBinding newInstance(int columnCount) {
		FragmentDataBinding fragment = new FragmentDataBinding();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragmentDataBinding() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		eu.davidea.samples.flexibleadapter.databinding.FragmentRecyclerViewDataBinding binding = DataBindingUtil
				.inflate(inflater, R.layout.fragment_recycler_view_data, container, false);
		binding.setItems(items);
		return binding.getRoot();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		//Create New Database and Initialize RecyclerView
		DatabaseService.getInstance().createHeadersSectionsDatabase(12, 4);
		initializeRecyclerView(savedInstanceState);

		//Restore FAB button and icon
		initializeFab();

		//Settings for FlipView
		FlipView.stopLayoutAnimation();
	}

	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		//Initialize Adapter and RecyclerView
		mAdapter = new BindingFlexibleAdapter<>(getActivity(), true);
		//Experimenting NEW features (v5.0.0)
		mAdapter.setNotifyChangeOfUnfilteredItems(true)//We have highlighted text while filtering, so let's enable this feature to be consistent with the active filter
				.setAnimationOnScrolling(DatabaseConfiguration.animateOnScrolling);
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		//NOTE: Use default item animator 'canReuseUpdatedViewHolder()' will return true if
		// a Payload is provided. FlexibleAdapter is actually sending Payloads onItemChange.
		mRecyclerView.setItemAnimator(new GarageDoorItemAnimator());

		//Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
		FastScroller fastScroller = (FastScroller) getView().findViewById(R.id.fast_scroller);
		mAdapter.setFastScroller(fastScroller);
		mAdapter.setLongPressDragEnabled(true)
				.setHandleDragEnabled(true)
				.setSwipeEnabled(true)
				.setDisplayHeadersAtStartUp(true)
				.setStickyHeaders(true);

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setEnabled(true);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);
	}

	@Override
	public void performFabAction() {
		if (fabClickedTimes == 0) {
			items.addAll(DatabaseService.getInstance().getDatabaseList());
		} else if (fabClickedTimes == 2) {
			HeaderItem headerItem = DatabaseService.newHeader(mAdapter.getItemCountOfTypes(R.layout.recycler_header_item) + 1);
			items.add(1, DatabaseService.newSimpleItem(fabClickedTimes * 111, headerItem));
		} else {
			items.add(0, DatabaseService.newSimpleItem(fabClickedTimes * 111, null));
		}
		++fabClickedTimes;
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
		menu.findItem(R.id.action_list_type).setVisible(false);
		menu.findItem(R.id.action_auto_collapse).setVisible(false);
		menu.findItem(R.id.action_expand_collapse_all).setVisible(false);
	}

}