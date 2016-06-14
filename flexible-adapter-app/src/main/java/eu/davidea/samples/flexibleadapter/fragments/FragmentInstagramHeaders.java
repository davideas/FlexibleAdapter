package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.common.DividerItemDecoration;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.models.ProgressItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FragmentInstagramHeaders extends AbstractFragment
		implements FlexibleAdapter.EndlessScrollListener {

	public static final String TAG = FragmentInstagramHeaders.class.getSimpleName();

	/**
	 * Custom implementation of FlexibleAdapter
	 */
	private FlexibleAdapter mAdapter;


	public static FragmentInstagramHeaders newInstance() {
		return new FragmentInstagramHeaders();
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragmentInstagramHeaders() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		//Create New Database and Initialize RecyclerView
		DatabaseService.getInstance().createInstagramHeadersDatabase();
		initializeRecyclerView(savedInstanceState);

		//Settings for FlipView
		FlipView.stopLayoutAnimation();
	}

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		mAdapter = new FlexibleAdapter<>(DatabaseService.getInstance().getDatabaseList(), getActivity());
		//Experimenting NEW features (v5.0.0)
		mAdapter.setAnimationOnScrolling(true);
		mAdapter.setAnimationOnReverseScrolling(true);
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		mRecyclerView.setItemAnimator(new DefaultItemAnimator() {
			@Override
			public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
				//NOTE: This allows to receive Payload objects on notifyItemChanged called by the Adapter!!!
				return true;
			}
		});
		mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), 0, 24));
		mAdapter.setDisplayHeadersAtStartUp(true)//Show Headers at startUp!
				.enableStickyHeaders()//Make headers sticky
				//Endless scroll with 1 item threshold
				.setEndlessScrollListener(this, new ProgressItem())
				.setEndlessScrollThreshold(1);//Default=1

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);
	}

	/**
	 * Loads more data.
	 */
	@Override
	public void onLoadMore() {
		Log.i(TAG, "onLoadMore invoked!");
		//Simulating asynchronous call
		new Handler().postDelayed(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				final List<AbstractFlexibleItem> newItems = new ArrayList<AbstractFlexibleItem>(3);

				//Simulating success/failure
				int totalItemsOfType = mAdapter.getItemCountOfTypes(R.layout.recycler_instagram_item);
				for (int i = 1; i <= 3; i++) {
					newItems.add(DatabaseService.newInstagramItem(totalItemsOfType + i));
				}

				//Callback the Adapter to notify the change
				//Items will be added to the end of the list
				mAdapter.onLoadMoreComplete(newItems);

				//Notify user
				String message = "Fetched " + newItems.size() + " new items";
				Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			}
		}, 2000);
	}

	@Override
	protected GridLayoutManager createNewGridLayoutManager() {
		return null;
	}
}