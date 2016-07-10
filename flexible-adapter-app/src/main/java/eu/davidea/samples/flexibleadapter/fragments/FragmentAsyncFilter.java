package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.MainActivity;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;
import eu.davidea.utils.Utils;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
@SuppressWarnings("ConstantConditions")
public class FragmentAsyncFilter extends AbstractFragment {

	public static final String TAG = FragmentAsyncFilter.class.getSimpleName();

	private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
	private int mSize;

	public static FragmentAsyncFilter newInstance(int size) {
		FragmentAsyncFilter fragment = new FragmentAsyncFilter();
		Bundle args = new Bundle();
		args.putInt(ARG_DYNAMIC_LIST, size);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragmentAsyncFilter() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mSize = getArguments().getInt(ARG_DYNAMIC_LIST);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		//Restore FAB icon
		FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

		if (mSize == -1) {
			DatabaseService.getInstance().clear();
			mAdapter = new FlexibleAdapter<>(null, getActivity());
			fab.setImageResource(R.drawable.fab_add);
		} else {
			//Create Database with custom size
			DatabaseService.getInstance().createEndlessDatabase(mSize);//N. of items
			mAdapter = new FlexibleAdapter<>(DatabaseService.getInstance().getDatabaseList(), getActivity());
			fab.setImageResource(R.drawable.ic_settings_white_24dp);
		}

		initializeRecyclerView();
	}

	private void initializeRecyclerView() {
		//Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		//Experimenting NEW features (v5.0.0)
		mAdapter.setAnimateToLimit(Integer.MAX_VALUE)//Size limit = MAX_VALUE will always animate the changes
				.setNotifyMoveOfFilteredItems(false)//When true, filtering on big list is very slow!
				.setNotifyChangeOfUnfilteredItems(true)//We have highlighted text while filtering, so let's enable this feature to be consistent with the active filter
				.setAnimationOnScrolling(true)
				.setAnimationOnReverseScrolling(true);
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		mRecyclerView.setItemAnimator(new DefaultItemAnimator() {
			@SuppressWarnings("NullableProblems")
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

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);

		//Settings for FlipView
		FlipView.stopLayoutAnimation();
	}

	@Override
	public void performFabAction() {
		if (mSize < 0) {
			DatabaseService.getInstance().createConfigurationDatabase();
			mAdapter = new FlexibleAdapter<>(DatabaseService.getInstance().getDatabaseList(), getActivity());
			FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
			fab.setImageResource(R.drawable.fab_add);
		} else {
			mAdapter.onDetachedFromRecyclerView(mRecyclerView);
			//Inflate the new Fragment with the new RecyclerView and a new Adapter
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.recycler_view_container, FragmentAsyncFilter.newInstance(-1)).commit();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		Log.v(TAG, "onCreateOptionsMenu called!");
		inflater.inflate(R.menu.menu_filter, menu);
		mListener.initSearchView(menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_search).setVisible(mSize >= 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}


}