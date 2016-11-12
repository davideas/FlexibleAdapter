package eu.davidea.samples.flexibleadapter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.samples.flexibleadapter.items.LayoutItem;
import eu.davidea.samples.flexibleadapter.items.ULSItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;

/**
 * This is a custom implementation extending FlexibleAdapter. {@code AbstractFlexibleItem} is
 * used as most common Item for ALL view types.
 * <p>Items are bound with <b>METHOD A</b> (new way): AutoMap is active, you <u>don't have to</u>
 * implement {@code getItemViewType, onCreateViewHolder, onBindViewHolder}.</p>
 * Check {@code OverallAdapter} for <b>METHOD B</b> (classic way).
 *
 * @see OverallAdapter
 * @see AbstractFlexibleItem
 */
public class ExampleAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

	private static final String TAG = ExampleAdapter.class.getSimpleName();

	private AbstractFlexibleItem mUseCaseItem;

	public ExampleAdapter(List<AbstractFlexibleItem> items, Object listeners) {
		//stableIds ? true = Items implement hashCode() so they can have stableIds!
		super(items, listeners, true);

		//In case you need a Handler, do this:
		//- Overrides the internal Handler with a custom callback that extends the internal one
		mHandler = new Handler(Looper.getMainLooper(), new MyHandlerCallback());
	}

	@Override
	public void updateDataSet(List<AbstractFlexibleItem> items, boolean animate) {
		//NOTE: To have views/items not changed, set them into "items" before passing the final
		// list to the Adapter.

		//Overwrite the list and fully notify the change, pass false to not animate changes.
		//Watch out! The original list must a copy
		super.updateDataSet(items, animate);

		//onPostUpdate() will be automatically called at the end of the Asynchronous update process.
		// Manipulate the list inside that method only or you won't see the changes.
	}

	/*
	 * HEADER VIEW
	 * This method show how to add Header/Footer View as it was for ListView.
	 * The secret is the position! 0 for Header; itemCount for Footer ;-)
	 * The view is represented by a custom Item type to better represent any dynamic content.
	 */
	public void showLayoutInfo(boolean scrollToPosition) {
		if (!hasSearchText() && !isEmpty()) {
			//Define Example View
			final LayoutItem item = new LayoutItem("LAY-L");
			if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
				item.setId("LAY-S");
				item.setTitle(mRecyclerView.getContext().getString(R.string.staggered_layout));
			} else if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
				item.setId("LAY-G");
				item.setTitle(mRecyclerView.getContext().getString(R.string.grid_layout));
			} else {
				item.setTitle(mRecyclerView.getContext().getString(R.string.linear_layout));
			}
			item.setSubtitle(mRecyclerView.getContext().getString(
					R.string.columns,
					String.valueOf(getSpanCount(mRecyclerView.getLayoutManager())))
			);
			addItemWithDelay((getItem(0) instanceof ULSItem ? 1 : 0), item, 0L,
					(!(getItem(0) instanceof ULSItem) && scrollToPosition));
			removeItemWithDelay(item, 4000L, true);
		}
	}

	/*
	 * ANOTHER HEADER VIEW
	 * This method show how to add Header/Footer View as it was for ListView.
	 * The secret is the position! 0 for Header; itemCount for Footer ;-)
	 * The view is represented by a custom Item type to better represent any dynamic content.
	 */
	public void addUserLearnedSelection(boolean scrollToPosition) {
		if (!DatabaseConfiguration.userLearnedSelection && !hasSearchText() && !(getItem(0) instanceof ULSItem)) {
			//Define Example View
			final ULSItem item = new ULSItem("ULS");
			item.setTitle(mRecyclerView.getContext().getString(R.string.uls_title));
			item.setSubtitle(mRecyclerView.getContext().getString(R.string.uls_subtitle));
			addItemWithDelay(0, item, 1500L, scrollToPosition);
		}
	}

	@Override
	protected void onPostFilter() {
		addUserLearnedSelection(false);
	}

	/**
	 * This is a customization of the Layout that hosts the header when sticky.
	 * The code works, but it is commented because not used (default is used).
	 */
//	@Override
//	public ViewGroup getStickySectionHeadersHolder() {
//		FrameLayout frameLayout = new FrameLayout(mRecyclerView.getContext());
//		frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
//				ViewGroup.LayoutParams.WRAP_CONTENT,//or MATCH_PARENT
//				ViewGroup.LayoutParams.WRAP_CONTENT));
//		((ViewGroup) mRecyclerView.getParent()).addView(frameLayout);//This is important otherwise the Header disappears!
//		return (ViewGroup) mInflater.inflate(R.layout.sticky_header_layout, frameLayout);
//	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer (don't call super).
	 */
//	@Override
//	public int getItemViewType(int position) {
//		//Not implemented: METHOD A is used
//	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer (don't call super).
	 */
//	@Override
//	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		//Not implemented: METHOD A is used
//	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer (don't call super).
	 */
//	@Override
//	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//		//Not implemented: METHOD A is used
//	}

	@Override
	public String onCreateBubbleText(int position) {
		if (!DatabaseConfiguration.userLearnedSelection && position == 0) {//This 'if' is for my example only
			//TODO FOR YOU: This is the normal line you should use: Usually it's the first letter
			return Integer.toString(position);
		}
		return super.onCreateBubbleText(position);
	}

	/**
	 * <b>Important:</b> In order to preserve the internal calls, this custom Callback
	 * <u>must</u> extends {@link FlexibleAdapter.HandlerCallback}
	 * which implements {@link android.os.Handler.Callback},
	 * therefore you <u>must</u> call {@code super().handleMessage(message)}.
	 * <p>
	 * This handler can launch asynchronous tasks and if you catch the reserved "what",
	 * keep in mind that this code should be executed <u>before</u> that task has been completed.
	 * </p>
	 * <p><b>Note:</b> numbers 0-9 are reserved for the Adapter, use others for new values.</p>
	 */
	private class MyHandlerCallback extends HandlerCallback {
		@Override
		public boolean handleMessage(Message message) {
			boolean done = super.handleMessage(message);
			switch (message.what) {
				//currently reserved
				case 0://async updateDataSet
				case 1://async filterItems
				case 2://confirm delete
				case 8://onLoadMore remove progress item
					return done;

				//free to use
				case 10:
				case 11:
					return true;
			}
			return false;
		}
	}

}