package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.MainActivity;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;
import eu.davidea.samples.flexibleadapter.services.DatabaseType;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
@SuppressWarnings("ConstantConditions")
public class FragmentAsyncFilter extends AbstractFragment {

    public static final String TAG = FragmentAsyncFilter.class.getSimpleName();

    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    private FlexibleItemDecoration mDivider;
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

        if (savedInstanceState != null) {
            configure = savedInstanceState.getBoolean(ARG_CONFIGURE);
        } else if (getArguments() != null) {
            configure = getArguments().getBoolean(ARG_CONFIGURE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Restore FAB button and icon
        initializeFab();

        FlexibleAdapter.useTag("AsyncFilterAdapter");
        // IMPORTANT! Upgrading to Support Library v26, stableIds must remain = false.
        // StableIds cause the entry animation not animate properly: alpha property of views
        // remain 1 (item not changed) while the entry animation requires the view to be invisible
        // when Swapping / Changing the Adapter.
        if (configure) {
            // Create configuration list
            DatabaseService.getInstance().createConfigurationDatabase(getResources());
            mAdapter = new FlexibleAdapter<>(DatabaseService.getInstance().getDatabaseList(),
                    getActivity(), false);
        } else {
            // Create Database with custom size
            // N. of items (1000 items it's already a medium size)
            DatabaseService.getInstance().createEndlessDatabase(DatabaseConfiguration.size);
            mAdapter = new FlexibleAdapter<>(DatabaseService.getInstance().getDatabaseList(),
                    getActivity(), false);
        }

        initializeRecyclerView();
        configure = !configure;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Revert the change so next time the "if-else" is executed correctly
        configure = !configure;
        outState.putBoolean(ARG_CONFIGURE, configure);
        super.onSaveInstanceState(outState);
    }

    private void initializeRecyclerView() {
        // Settings for FlipView
        FlipView.resetLayoutAnimationDelay(true, 1000L);

        // Size limit = MAX_VALUE will always animate the changes
        mAdapter.setAnimateToLimit(DatabaseConfiguration.animateToLimit)
                // When true, filtering on big list is very slow!
                .setNotifyMoveOfFilteredItems(DatabaseConfiguration.notifyMove)
                .setNotifyChangeOfUnfilteredItems(DatabaseConfiguration.notifyChange)
                .setAnimationInitialDelay(100L)
                .setAnimationOnScrolling(true)
                .setAnimationOnReverseScrolling(true)
                .setOnlyEntryAnimation(true);
        if (mRecyclerView == null) {
            mRecyclerView = getView().findViewById(R.id.recycler_view);
            mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
            // Adapter changes won't affect the size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);
        }
        // ViewHolders are different so we do NOT swap Adapters
        mRecyclerView.setAdapter(mAdapter);
        // Custom divider item decorator with Offset
        if (mDivider == null) {
            mDivider = new FlexibleItemDecoration(getActivity())
                    .withDivider(R.drawable.divider_large);
        }

        // Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!
        SwipeRefreshLayout swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(!configure);
        mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, Mode.IDLE);

        if (configure) {
            mFab.setImageResource(R.drawable.ic_check_white_24dp);
            mRecyclerView.addItemDecoration(mDivider);
        } else {
            mFab.setImageResource(R.drawable.ic_settings_white_24dp);
            mRecyclerView.removeItemDecoration(mDivider);
            FastScroller fastScroller = getView().findViewById(R.id.fast_scroller);
            fastScroller.addOnScrollStateChangeListener((MainActivity) getActivity());
            mAdapter.setFastScroller(fastScroller);
        }

        // Settings for FlipView
        FlipView.stopLayoutAnimation();
        showFab(1200L);
    }

    @Override
    public void performFabAction() {
        hideFab(); //Give time to hide FAB before changing everything
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
        }, 200L);
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

    private void hideFab() {
        ViewCompat.animate(mFab)
                  .scaleX(0f).scaleY(0f)
                  .alpha(0f).setDuration(50)
                  .start();
    }

    private void showFab(long delay) {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewCompat.animate(mFab)
                          .scaleX(1f).scaleY(1f)
                          .alpha(1f).setDuration(200)
                          .start();
            }
        }, delay);
    }

}