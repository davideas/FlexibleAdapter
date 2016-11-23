package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.common.DividerItemDecoration;
import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.ExampleAdapter;
import eu.davidea.samples.flexibleadapter.MainActivity;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;
import eu.davidea.utils.Utils;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FragmentExpandableSections extends AbstractFragment {

	public static final String TAG = FragmentExpandableSections.class.getSimpleName();

	private ExampleAdapter mAdapter;

	public static FragmentExpandableSections newInstance(int columnCount) {
		FragmentExpandableSections fragment = new FragmentExpandableSections();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragmentExpandableSections() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		//Create New Database and Initialize RecyclerView
		DatabaseService.getInstance().createExpandableSectionsDatabase(100);//N. of sections
		initializeRecyclerView(savedInstanceState);

		//Settings for FlipView
		FlipView.stopLayoutAnimation();
	}

	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		//Initialize Adapter and RecyclerView
		//ExampleAdapter makes use of stableIds, I strongly suggest to implement 'item.hashCode()'
		mAdapter = new ExampleAdapter(DatabaseService.getInstance().getDatabaseList(), getActivity());
		//Experimenting NEW features (v5.0.0)
		mAdapter.expandItemsAtStartUp()
				.setAutoCollapseOnExpand(false)
				.setAutoScrollOnExpand(true)
				.setAnimateToLimit(Integer.MAX_VALUE)//Size limit = MAX_VALUE will always animate the changes
				.setNotifyMoveOfFilteredItems(false)//When true, filtering on big list is very slow!
				.setNotifyChangeOfUnfilteredItems(true)//We have highlighted text while filtering, so let's enable this feature to be consistent with the active filter
				.setRemoveOrphanHeaders(false)
				.setAnimationOnScrolling(DatabaseConfiguration.animateOnScrolling)
				.setAnimationOnReverseScrolling(true);
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		//NOTE: Use default item animator 'canReuseUpdatedViewHolder()' will return true if
		// a Payload is provided. FlexibleAdapter is actually sending Payloads onItemChange.
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		//Custom divider item decorator
		mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
				R.drawable.divider, 0));//Increase to add gap between sections (Works only with LinearLayout!)

		//Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
		mAdapter.setFastScroller((FastScroller) getView().findViewById(R.id.fast_scroller),
				Utils.getColorAccent(getActivity()), (MainActivity) getActivity());
		//Experimenting NEW features (v5.0.0)
		mAdapter.setLongPressDragEnabled(true)//Enable long press to drag items
				.setHandleDragEnabled(true);//Enable handle drag
				//.setDisplayHeadersAtStartUp(true);//Show Headers at startUp! (not necessary if Headers are also Expandable)

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setEnabled(true);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);

		//Add sample HeaderView items on the top (not belongs to the library)
		mAdapter.showLayoutInfo(savedInstanceState == null);
	}

	@Override
	public void showNewLayoutInfo(MenuItem item) {
		super.showNewLayoutInfo(item);
		mAdapter.showLayoutInfo(true);
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
					case R.layout.recycler_scrollable_layout_item:
					case R.layout.recycler_scrollable_uls_item:
					case R.layout.recycler_header_item:
					case R.layout.recycler_expandable_header_item:
					case R.layout.recycler_expandable_item:
						return mColumnCount;
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
		Log.v(TAG, "onCreateOptionsMenu called!");
		inflater.inflate(R.menu.menu_sections, menu);
		mListener.initSearchView(menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_list_type)
			mAdapter.setAnimationOnScrolling(true);
		return super.onOptionsItemSelected(item);
	}

}