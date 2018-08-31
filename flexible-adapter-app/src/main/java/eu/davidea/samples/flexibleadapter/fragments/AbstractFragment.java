package eu.davidea.samples.flexibleadapter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.common.SmoothScrollStaggeredLayoutManager;
import eu.davidea.samples.flexibleadapter.R;

/**
 * @author Davide Steduto
 * @since 15/04/2016
 */
public abstract class AbstractFragment extends Fragment {

    public static final String TAG = AbstractFragment.class.getSimpleName();
    protected static final String ARG_COLUMN_COUNT = "column_count";
    protected static final String ARG_CONFIGURE = "dynamic_list";

    protected OnFragmentInteractionListener mListener;
    protected int mColumnCount = 2;
    protected RecyclerView mRecyclerView;
    protected FloatingActionButton mFab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        //Contribution for specific action buttons in the Toolbar
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false);
    }

    /**
     * Display FAB button and restore default icon
     */
    protected void initializeFab() {
        mFab = getActivity().findViewById(R.id.fab);
        mFab.setImageResource(R.drawable.fab_add);
        ViewCompat.animate(mFab)
                  .scaleX(1f).scaleY(1f)
                  .alpha(1f).setDuration(100)
                  .setStartDelay(300L)
                  .start();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_list_type) {
            if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
                item.setIcon(R.drawable.ic_view_grid_white_24dp);
                item.setTitle(R.string.grid_layout);//next click
                showNewLayoutInfo(item);
            } else if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
                mRecyclerView.setLayoutManager(createNewStaggeredGridLayoutManager());
                item.setIcon(R.drawable.ic_view_agenda_white_24dp);
                item.setTitle(R.string.linear_layout);//next click
                showNewLayoutInfo(item);
            } else {
                mRecyclerView.setLayoutManager(createNewGridLayoutManager());
                item.setIcon(R.drawable.ic_dashboard_white_24dp);
                item.setTitle(R.string.staggered_layout);//next click
                showNewLayoutInfo(item);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected LinearLayoutManager createNewLinearLayoutManager() {
        return new SmoothScrollLinearLayoutManager(getActivity());
    }

    protected GridLayoutManager createNewGridLayoutManager() {
        return new SmoothScrollGridLayoutManager(getActivity(), mColumnCount);
    }

    protected StaggeredGridLayoutManager createNewStaggeredGridLayoutManager() {
        return new SmoothScrollStaggeredLayoutManager(getActivity(), mColumnCount);
    }

    public void performFabAction() {
        //default implementation does nothing
    }

    public int getContextMenuResId() {
        //default Menu Context is returned
        return R.menu.menu_context;
    }

    @CallSuper
    public void showNewLayoutInfo(final MenuItem item) {
        item.setEnabled(false);
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setEnabled(true);
            }
        }, 1000L);
    }

}