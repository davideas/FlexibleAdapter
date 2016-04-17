package eu.davidea.examples.flexibleadapter;

import android.animation.Animator;
import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.examples.flexibleadapter.services.DatabaseService;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * NOTE: AbstractModelItem is for example purpose only. I wanted to have in common
 * some Fields and Layout.
 * You, having different Layout for each item type, would use IFlexible or AbstractFlexibleItem
 * as base item to extend this Adapter.
 */
public class OverallAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

	private static final String TAG = OverallAdapter.class.getSimpleName();

	public OverallAdapter(Activity activity) {
		super(DatabaseService.getInstance().getDatabaseList(), activity);
	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer.
	 */
//	@Override
//	public int getItemViewType(int position) {
//		return R.layout.recycler_label;
//	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer.
	 */
//	@Override
//	public OverallItem.LabelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		if (mInflater == null) {
//			mInflater = LayoutInflater.from(parent.getContext());
//		}
//		return new OverallItem.LabelViewHolder(
//				mInflater.inflate(R.layout.recycler_label, parent, false), this);
//	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer.
	 */
//	@Override
//	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payload) {
//		OverallItem item = (OverallItem) getItem(position);
//		OverallItem.LabelViewHolder vHolder = (OverallItem.LabelViewHolder) holder;
//		assert item != null;
//
//		if (item.getTitle() != null) {
//			vHolder.mTitle.setText(item.getTitle());
//			vHolder.mTitle.setEnabled(item.isEnabled());
//		}
//		if (item.getDescription() != null) {
//			vHolder.mSubtitle.setText(item.getDescription());
//			vHolder.mSubtitle.setEnabled(item.isEnabled());
//		}
//		if (item.getIcon() != null) {
//			vHolder.mIcon.setImageDrawable(item.getIcon());
//		}
//
//		animateView(vHolder.itemView, position, isSelected(position));
//	}

	@Override
	public List<Animator> getAnimators(View itemView, int position, boolean isSelected) {
		List<Animator> animators = new ArrayList<Animator>();
		if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
			//GridLayout
			if (position % 2 != 0)
				addSlideInFromRightAnimator(animators, itemView, 0.5f);
			else
				addSlideInFromLeftAnimator(animators, itemView, 0.5f);
		} else {
			//LinearLayout
			switch (getItemViewType(position)) {
				default:
					addSlideInFromBottomAnimator(animators, itemView);
					break;
			}
		}

		//Alpha Animator is automatically added
		return animators;
	}

}