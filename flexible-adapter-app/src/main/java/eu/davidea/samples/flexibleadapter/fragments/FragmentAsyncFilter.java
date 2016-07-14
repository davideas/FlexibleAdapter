package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
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
import eu.davidea.flexibleadapter.common.DividerItemDecoration;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.MainActivity;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;
import eu.davidea.samples.flexibleadapter.services.DatabaseType;
import eu.davidea.utils.Utils;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
@SuppressWarnings("ConstantConditions")
public class FragmentAsyncFilter extends AbstractFragment {

	public static final String TAG = FragmentAsyncFilter.class.getSimpleName();

	private FloatingActionButton mFab;
	private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
	private DividerItemDecoration mDivider;
	private boolean configure;
	private MenuItem mSearchView;

	public static FragmentAsyncFilter newInstance(boolean configure) {
		FragmentAsyncFilter fragment = new FragmentAsyncFilter();
		Bundle args = new Bundle();
		args.putBoolean(ARG_CONFIGURE, configure);
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
			configure = getArguments().getBoolean(ARG_CONFIGURE);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		//Restore FAB icon
		mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

		if (configure) {
			//Create configuration list
			DatabaseService.getInstance().createConfigurationDatabase(getResources());
			mAdapter = new FlexibleAdapter<>(DatabaseService.getInstance().getDatabaseList(), getActivity());

		} else {
			//Create Database with custom size
			DatabaseService.getInstance().createEndlessDatabase(DatabaseConfiguration.size);//N. of items (1000 items it's already a medium size)
			mAdapter = new FlexibleAdapter<>(DatabaseService.getInstance().getDatabaseList(), getActivity());
		}

		initializeRecyclerView();
		if (configure) configure = false;
	}

	private void initializeRecyclerView() {
		//Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		//Experimenting NEW features (v5.0.0)
		mAdapter.setAnimateToLimit(DatabaseConfiguration.animateToLimit)//Size limit = MAX_VALUE will always animate the changes
				.setNotifyMoveOfFilteredItems(DatabaseConfiguration.notifyMove)//When true, filtering on big list is very slow!
				.setNotifyChangeOfUnfilteredItems(DatabaseConfiguration.notifyChange)//We have highlighted text while filtering, so let's enable this feature to be consistent with the active filter
				.setOnlyEntryAnimation(true);
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
		if (mRecyclerView.getAdapter() == null) {
			mRecyclerView.setAdapter(mAdapter);
		} else {
			mRecyclerView.swapAdapter(mAdapter, false);
		}
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		mRecyclerView.setItemAnimator(new DefaultItemAnimator() {
			@SuppressWarnings("NullableProblems")
			@Override
			public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
				//NOTE: This allows to receive Payload objects when notifyItemChanged is called by the Adapter!!!
				return true;
			}
		});
		if (mDivider == null) {
			mDivider = new DividerItemDecoration(getActivity(), R.drawable.divider_large).withOffset(true);
		}

		//Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setEnabled(!configure);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);

		if (configure) {
			mFab.setImageResource(R.drawable.ic_check_white_24dp);
			mRecyclerView.addItemDecoration(mDivider);
		} else {
			mFab.setImageResource(R.drawable.ic_settings_white_24dp);
			mRecyclerView.removeItemDecoration(mDivider);
			mAdapter.setFastScroller((FastScroller) getActivity().findViewById(R.id.fast_scroller),
					Utils.getColorAccent(getActivity()), (MainActivity) getActivity());
		}

		//Settings for FlipView
		FlipView.stopLayoutAnimation();
		showFab(1000L);
	}

	@Override
	public void performFabAction() {
		hideFab();//Give time to hide FAB before changing everything
		mRecyclerView.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (configure) {
					onActivityCreated(null);
					mSearchView.setVisible(false);
					configure = false;
				} else {
					Snackbar.make(getView(), "Created list with " + DatabaseConfiguration.size + " items", Snackbar.LENGTH_LONG).show();
					onActivityCreated(null);
					mSearchView.setVisible(mAdapter.getItemCount() > 0);
					configure = true;
				}
			}
		}, 100L);
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
		mSearchView = menu.findItem(R.id.action_search);
		mSearchView.setVisible(mAdapter.getItemCount() > 0 &&
				DatabaseService.getInstance().getDatabaseType() != DatabaseType.CONFIGURATION);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void hideFab() {
		ViewCompat.animate(mFab)
				.scaleX(0f).scaleY(0f)
				.alpha(0f).setDuration(100)
				.start();
	}

	private void showFab(long delay) {
		mRecyclerView.postDelayed(new Runnable() {
			@Override
			public void run() {
				ViewCompat.animate(mFab)
						.scaleX(1f).scaleY(1f)
						.alpha(1f).setDuration(100)
						.start();
			}
		}, delay);
	}

}