package eu.davidea.samples.flexibleadapter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.samples.flexibleadapter.items.ScrollableExpandableItem;
import eu.davidea.samples.flexibleadapter.items.ScrollableFooterItem;
import eu.davidea.samples.flexibleadapter.items.ScrollableLayoutItem;
import eu.davidea.samples.flexibleadapter.items.ScrollableSubItem;
import eu.davidea.samples.flexibleadapter.items.ScrollableULSItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;

/**
 * This is a custom implementation extending FlexibleAdapter. {@code AbstractFlexibleItem} is
 * used as most common Item for ALL view types.
 * <p>Binding is delegated via items (AutoMap), you <u>cannot</u> implement
 * {@code getItemViewType, onCreateViewHolder, onBindViewHolder}.</p>
 *
 * @see OverallAdapter
 * @see AbstractFlexibleItem
 */
public class ExampleAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

    private static final String TAG = ExampleAdapter.class.getSimpleName();

    public ExampleAdapter(List<AbstractFlexibleItem> items, Object listeners) {
        // stableIds ? true = Items implement hashCode() so they can have stableIds!
        super(items, listeners, true);

        // In case you need a Handler, do this:
        // - Overrides the internal Handler with a custom callback that extends the internal one
        mHandler = new Handler(Looper.getMainLooper(), new MyHandlerCallback());
    }

    @Override
    public void updateDataSet(List<AbstractFlexibleItem> items, boolean animate) {
        // NOTE: To have views/items not changed, set them into "items" before passing the final
        // list to the Adapter.

        // Overwrite the list and fully notify the change, pass false to not animate changes.
        // Watch out! The original list must a copy.
        super.updateDataSet(items, animate);

        // onPostUpdate() will automatically be called at the end of the Asynchronous update
        // process. Manipulate the list inside that method only or you won't see the changes.
    }

    /*
     * You can override this method to define your own concept of "Empty".
     * This method is never called internally.
     */
    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    /*
     * HEADER VIEW
     * This method shows how to add Header View as it was for ListView.
     * Same Header item is enqueued for removal with a delay.
     * The view is represented by a custom Item type to better represent any dynamic content.
     */
    public void showLayoutInfo(boolean scrollToPosition) {
        if (!hasSearchText()) {
            final ScrollableLayoutItem item = new ScrollableLayoutItem("LAY-L");
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
                    String.valueOf(getFlexibleLayoutManager().getSpanCount()))
            );
            // NOTE: If you have to change at runtime the LayoutManager AND add
            // Scrollable Headers, consider to add them in post, using a delay >= 0
            // otherwise scroll animations on all items will not start correctly.
            addScrollableHeaderWithDelay(item, 1200L, scrollToPosition);
            removeScrollableHeaderWithDelay(item, 4000L);
        }
    }

    /*
     * ANOTHER HEADER VIEW
     * This method showcases how to add a Header View with a delay.
     * The view is represented by a custom Item type to better represent any dynamic content.
     */
    public void addUserLearnedSelection(boolean scrollToPosition) {
        if (!DatabaseConfiguration.userLearnedSelection && !hasSearchText() && !(getItem(0) instanceof ScrollableULSItem)) {
            final ScrollableULSItem item = new ScrollableULSItem("ULS");
            item.setTitle(mRecyclerView.getContext().getString(R.string.uls_title));
            item.setSubtitle(mRecyclerView.getContext().getString(R.string.uls_subtitle));
            addScrollableHeaderWithDelay(item, 1000L, scrollToPosition);
        }
    }

    /*
     * FOOTER VIEW
     * This method showcases how to delay add a Footer View.
     * The view is represented by a custom Item type to better represent any dynamic content.
     */
    public void addScrollableFooter() {
        final ScrollableFooterItem item = new ScrollableFooterItem("SFI");
        item.setTitle(mRecyclerView.getContext().getString(R.string.scrollable_footer_title));
        item.setSubtitle(mRecyclerView.getContext().getString(R.string.scrollable_footer_subtitle));
        addScrollableFooterWithDelay(item, 1000L, false);
    }

    /*
     * Showcase for EXPANDABLE HEADER VIEW
     */
    public void addScrollableExpandableAsHeader() {
        final ScrollableExpandableItem expandable = new ScrollableExpandableItem("SEHI");
        expandable.setTitle(mRecyclerView.getContext().getString(R.string.scrollable_expandable_header_title));
        expandable.setSubtitle(mRecyclerView.getContext().getString(R.string.scrollable_expandable_header_subtitle));
        expandable.addSubItem(new ScrollableSubItem("SEHI_1"));
        expandable.addSubItem(new ScrollableSubItem("SEHI_2"));
        addScrollableHeaderWithDelay(expandable, 1500L, false);
    }

    /*
     * Showcase for EXPANDABLE FOOTER VIEW
     */
    public void addScrollableExpandableAsFooter() {
        final ScrollableExpandableItem expandable = new ScrollableExpandableItem("SEFI");
        expandable.setTitle(mRecyclerView.getContext().getString(R.string.scrollable_expandable_footer_title));
        expandable.setSubtitle(mRecyclerView.getContext().getString(R.string.scrollable_expandable_footer_subtitle));
        expandable.addSubItem(new ScrollableSubItem("SEFI_1"));
        expandable.addSubItem(new ScrollableSubItem("SEFI_2"));
        addScrollableFooterWithDelay(expandable, 1500L, false);
    }

    /*
     * Delegated via item objects. You should not implement this method!
     */
//	@Override
//	public int getItemViewType(int position) {
//		//Not implemented: METHOD A is used
//	}

	/*
	 * Delegated via item objects. You should not implement this method!
	 */
//	@Override
//	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		// Not implemented: METHOD A is used
//	}

    /*
     * Delegated via item objects. You should not implement this method!
     */
//	@Override
//	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//		// Not implemented: METHOD A is used
//	}

    @Override
    public String onCreateBubbleText(int position) {
        if (position < getScrollableHeaders().size()) {
            return "Top";
        } else if (position >= getItemCount() - getScrollableFooters().size()) {
            return "Bottom";
        } else {
            position -= getScrollableHeaders().size() + 1;
        }
        // TODO FOR YOU: The basic value, usually, is the first letter
        // return getItem(position).getBubbleText(position);

        // For me the position is (position + 1):
        return super.onCreateBubbleText(position);
    }

    /**
     * Showcase to reuse the internal Handler.
     *
     * <b>IMPORTANT:</b> In order to preserve the internal calls, this custom Callback
     * <u>must</u> extends {@link FlexibleAdapter.HandlerCallback}
     * which implements {@link android.os.Handler.Callback},
     * therefore you <u>must</u> call {@code super().handleMessage(message)}.
     * <p>This handler can launch asynchronous tasks.</p>
     * If you catch the reserved "what", keep in mind that this code should be executed
     * <u>before</u> that task has been completed.
     * <p><b>Note:</b> numbers 0-9 are reserved for the Adapter, use others for new values.</p>
     */
    private class MyHandlerCallback extends HandlerCallback {
        @Override
        public boolean handleMessage(Message message) {
            boolean done = super.handleMessage(message);
            switch (message.what) {
                // Currently reserved (you don't need to check these numbers!)
                case 1: //async updateDataSet
                case 2: //async filterItems
                case 3: //confirm delete
                case 8: //onLoadMore remove progress item
                    return done;

                // Free to use, example:
                case 10:
                case 11:
                    return true;
            }
            return false;
        }
    }

}