package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.Payload;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.items.ProgressItem.ProgressViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * @author Davide Steduto
 * @since 22/04/2016
 */
public class ProgressItem extends AbstractFlexibleItem<ProgressViewHolder> {

	@Override
	public boolean equals(Object o) {
		return this == o;//The default implementation
	}

	@Override
	public int getLayoutRes() {
		return R.layout.progress_item;
	}

	@Override
	public ProgressViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ProgressViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, ProgressViewHolder holder, int position, List payloads) {
		if (!payloads.contains(Payload.NO_MORE_LOAD) && adapter.isEndlessScrollEnabled()) {
			holder.progressBar.setVisibility(View.VISIBLE);
			holder.progressMessage.setVisibility(View.GONE);
		} else if (!adapter.isEndlessScrollEnabled()) {
			holder.progressBar.setVisibility(View.GONE);
			holder.progressMessage.setVisibility(View.VISIBLE);
			Context context = holder.itemView.getContext();
			holder.progressMessage.setText(context.getString(R.string.endless_finished));
		} else if (payloads.contains(Payload.NO_MORE_LOAD)) {
			holder.progressBar.setVisibility(View.GONE);
			holder.progressMessage.setVisibility(View.VISIBLE);
			Context context = holder.itemView.getContext();
			holder.progressMessage.setText(context.getString(R.string.no_more_load));
		}
	}

	static class ProgressViewHolder extends FlexibleViewHolder {

		@BindView(R.id.progress_bar)
		ProgressBar progressBar;
		@BindView(R.id.progress_message)
		TextView progressMessage;

		public ProgressViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			ButterKnife.bind(this, view);
		}
		@Override
		public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
			AnimatorHelper.scaleAnimator(animators, itemView, 0f);
		}
	}

}