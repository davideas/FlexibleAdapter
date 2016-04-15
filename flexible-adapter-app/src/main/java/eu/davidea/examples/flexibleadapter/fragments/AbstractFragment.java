package eu.davidea.examples.flexibleadapter.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.davidea.examples.flexibleadapter.R;

/**
 * @author Davide Steduto
 * @since 15/04/2016
 */
public abstract class AbstractFragment extends Fragment {

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

}