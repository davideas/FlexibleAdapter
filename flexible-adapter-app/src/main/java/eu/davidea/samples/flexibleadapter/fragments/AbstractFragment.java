package eu.davidea.samples.flexibleadapter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;

/**
 * @author Davide Steduto
 * @since 15/04/2016
 */
public abstract class AbstractFragment extends Fragment {

	public static final String TAG = AbstractFragment.class.getSimpleName();
	protected static final String ARG_COLUMN_COUNT = "column_count";

	protected OnFragmentInteractionListener mListener;
	protected int mColumnCount = 2;
	protected RecyclerView mRecyclerView;

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

	protected abstract GridLayoutManager createNewGridLayoutManager();

	public void addItem() {
		//default implementation does nothing
	}

}