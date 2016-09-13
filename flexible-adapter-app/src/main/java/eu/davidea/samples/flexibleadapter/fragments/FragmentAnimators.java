package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.common.FlexibleItemAnimator;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.ExampleAdapter;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.animators.FadeInAnimator;
import eu.davidea.samples.flexibleadapter.animators.FadeInDownAnimator;
import eu.davidea.samples.flexibleadapter.animators.FadeInLeftAnimator;
import eu.davidea.samples.flexibleadapter.animators.FadeInRightAnimator;
import eu.davidea.samples.flexibleadapter.animators.FadeInUpAnimator;
import eu.davidea.samples.flexibleadapter.animators.FlipInBottomXAnimator;
import eu.davidea.samples.flexibleadapter.animators.FlipInTopXAnimator;
import eu.davidea.samples.flexibleadapter.animators.LandingAnimator;
import eu.davidea.samples.flexibleadapter.animators.OvershootInLeftAnimator;
import eu.davidea.samples.flexibleadapter.animators.OvershootInRightAnimator;
import eu.davidea.samples.flexibleadapter.animators.ScaleInAnimator;
import eu.davidea.samples.flexibleadapter.animators.SlideInDownAnimator;
import eu.davidea.samples.flexibleadapter.animators.SlideInLeftAnimator;
import eu.davidea.samples.flexibleadapter.animators.SlideInRightAnimator;
import eu.davidea.samples.flexibleadapter.animators.SlideInUpAnimator;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

/**
 * Testing different types of item animators including scrolling animations.
 */
public class FragmentAnimators extends AbstractFragment {

	public static final String TAG = FragmentAnimators.class.getSimpleName();

	private ExampleAdapter mAdapter;

	/* Spinner selected item */
	private static int selectedItem1 = -1;
	private static int selectedItem2 = -1;

	public static FragmentAnimators newInstance() {
		return new FragmentAnimators();
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragmentAnimators() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		//Create New Database and Initialize RecyclerView
		DatabaseService.getInstance().createAnimatorsDatabase(10);//N. of sections
		initializeRecyclerView(savedInstanceState);

		//Restore FAB button and icon
		initializeFab();

		//Settings for FlipView
		FlipView.stopLayoutAnimation();
	}

	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		mAdapter = new ExampleAdapter(DatabaseService.getInstance().getDatabaseList(), getActivity());
		//Experimenting NEW features (v5.0.0)
		mAdapter.expandItemsAtStartUp()
				.setAutoCollapseOnExpand(false)
				.setAutoScrollOnExpand(true)
				.setAnimationOnScrolling(DatabaseConfiguration.animateOnScrolling)
				.setAnimationOnReverseScrolling(true);
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change

		//NOTE: Custom item animators inherit 'canReuseUpdatedViewHolder()' from Default Item
		// Animator. It will return true if a Payload is provided. FlexibleAdapter is actually
		// sending Payloads onItemChange notifications.
		mRecyclerView.setItemAnimator(new FlexibleItemAnimator());
		initializeSpinnerItemAnimators();
		initializeSpinnerScrollAnimators();

		//Experimenting NEW features (v5.0.0)
		mAdapter.setSwipeEnabled(true)
				.getItemTouchHelperCallback()
				.setSwipeFlags(ItemTouchHelper.RIGHT);//Enable swipe

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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		Log.v(TAG, "onCreateOptionsMenu called!");
		inflater.inflate(R.menu.menu_animators, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_step_delay) {
			item.setChecked(!item.isChecked());
			mAdapter.setUseStepDelay(item.isChecked());
		} else if (id == R.id.action_sub_item_specific) {
			item.setChecked(!item.isChecked());
			DatabaseConfiguration.subItemSpecificAnimation = item.isChecked();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		getActivity().findViewById(R.id.layout_for_spinners).setVisibility(View.GONE);
	}

	private void initializeSpinnerItemAnimators() {
		// Creating adapter for spinner1
		ArrayAdapter<AnimatorType> spinnerAdapter = new ArrayAdapter<AnimatorType>(
				getActivity(), android.R.layout.simple_spinner_item, AnimatorType.values()) {
			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View view = super.getDropDownView(position, convertView, parent);
				view.setBackgroundResource(R.drawable.selector_item_light);
				view.setActivated(position == selectedItem1);
				return view;
			}
		};

		// Drop down layout style - list view with radio button
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Make visible spinner configuration
		getActivity().findViewById(R.id.layout_for_spinners).setVisibility(View.VISIBLE);

		Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinner_item_animators);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mRecyclerView.setItemAnimator(AnimatorType.values()[position].getAnimator());
				mRecyclerView.getItemAnimator().setAddDuration(500);
				mRecyclerView.getItemAnimator().setRemoveDuration(500);
				selectedItem1 = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	private void initializeSpinnerScrollAnimators() {
		// Creating adapter for spinner2
		ArrayAdapter<ScrollAnimatorType> spinnerAdapter = new ArrayAdapter<ScrollAnimatorType>(
				getActivity(), android.R.layout.simple_spinner_item, ScrollAnimatorType.values()) {
			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View view = super.getDropDownView(position, convertView, parent);
				view.setBackgroundResource(R.drawable.selector_item_light);
				view.setActivated(position == selectedItem2);
				return view;
			}
		};

		// Drop down layout style - list view with radio button
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Make visible spinner configuration
		getActivity().findViewById(R.id.layout_for_spinners).setVisibility(View.VISIBLE);

		Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinner_scrolling_animation);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				DatabaseConfiguration.scrollAnimatorType = ScrollAnimatorType.values()[position];
				selectedItem2 = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	public enum AnimatorType {
		FadeIn(new FadeInAnimator(new OvershootInterpolator(1f))),
		FadeInDown(new FadeInDownAnimator(new OvershootInterpolator(1f))),
		FadeInUp(new FadeInUpAnimator(new OvershootInterpolator(1f))),
		FadeInLeft(new FadeInLeftAnimator(new OvershootInterpolator(1f))),
		FadeInRight(new FadeInRightAnimator(new OvershootInterpolator(1f))),
		Landing(new LandingAnimator(new OvershootInterpolator(1f))),
		ScaleIn(new ScaleInAnimator(new OvershootInterpolator(1f))),
		FlipInTopX(new FlipInTopXAnimator(new OvershootInterpolator(1f))),
		FlipInBottomX(new FlipInBottomXAnimator(new OvershootInterpolator(1f))),
		SlideInLeft(new SlideInLeftAnimator(new OvershootInterpolator(1f))),
		SlideInRight(new SlideInRightAnimator(new OvershootInterpolator(1f))),
		SlideInDown(new SlideInDownAnimator(new OvershootInterpolator(1f))),
		SlideInUp(new SlideInUpAnimator(new OvershootInterpolator(1f))),
		OvershootInRight(new OvershootInRightAnimator(1f)),
		OvershootInLeft(new OvershootInLeftAnimator(1f));

		private FlexibleItemAnimator mAnimator;

		AnimatorType(FlexibleItemAnimator animator) {
			mAnimator = animator;
		}

		public FlexibleItemAnimator getAnimator() {
			return mAnimator;
		}
	}

	public enum ScrollAnimatorType {
		Alpha("Alpha (Default)"),
		SlideInFromTop("SlideIn from Top"),
		SlideInFromBottom("SlideIn from Bottom"),
		SlideInTopBottom("SlideIn Top + Bottom"),
		SlideInFromLeft("SlideIn from Left"),
		SlideInFromRight("SlideIn from Right"),
		ScaleIn("ScaleIn");

		private String name;

		ScrollAnimatorType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

}