package eu.davidea.samples.flexibleadapter.models;

import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ConfigurationItem extends AbstractFlexibleItem<ConfigurationItem.ViewHolder> {

	public static final int SEEK_BAR = 0, SWITCH = 1;

	private String id, title, description;
	private int widgetType;
	private boolean moreDescription = false;
	private int value, maxValue, stepValue;

	@IntDef({SEEK_BAR, SWITCH})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Type {
	}

	public ConfigurationItem(String id, @Type int widgetType) {
		super();
		this.id = id;
		this.widgetType = widgetType;
	}

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof ConfigurationItem) {
			ConfigurationItem inItem = (ConfigurationItem) inObject;
			return this.getId().equals(inItem.getId());
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getWidgetType() {
		return widgetType;
	}

	public void setWidgetType(int widgetType) {
		this.widgetType = widgetType;
	}

	public String getTitle() {
		return title;
	}

	public ConfigurationItem withTitle(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public ConfigurationItem withDescription(String description) {
		this.description = description;
		this.moreDescription = description.length() > 50;
		return this;
	}

	public int getValue() {
		return value;
	}

	public ConfigurationItem withValue(int value) {
		this.value = value;
		return this;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public ConfigurationItem withMaxValue(int maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	public int getStepValue() {
		return stepValue;
	}

	public ConfigurationItem withStepValue(int stepValue) {
		this.stepValue = stepValue;
		return this;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_configuration_item;
	}

	@Override
	public ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void bindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
		holder.mTitle.setText(getTitle());
		if (getDescription() != null)
			holder.mDescription.setText(getDescription());
		switch (getWidgetType()) {
			case SEEK_BAR:
				holder.mSeekBar.setVisibility(View.VISIBLE);
				holder.mSwitchView.setVisibility(View.GONE);
				break;
			case SWITCH:
				holder.mSeekBar.setVisibility(View.GONE);
				holder.mSwitchView.setVisibility(View.VISIBLE);
				break;
		}
	}

	static class ViewHolder extends FlexibleViewHolder {

		public TextView mTitle;
		public TextView mDescription;
		public TextView mMore;
		public SeekBar mSeekBar;
		public Switch mSwitchView;

		public ViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter, true);//True for sticky
			mTitle = (TextView) view.findViewById(R.id.title);
			mDescription = (TextView) view.findViewById(R.id.description);
			mMore = (TextView) view.findViewById(R.id.more);
			mMore.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

				}
			});
			mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
			mSwitchView = (Switch) view.findViewById(R.id.switch_box);
		}

		@Override
		public void onClick(View view) {
			super.onClick(view);
			if (mSwitchView.getVisibility() == View.VISIBLE) {
				mSwitchView.setChecked(!mSwitchView.isChecked());
			}
		}

	}

	@Override
	public String toString() {
		return "ConfigurationItem[id=" + id + "]";
	}

}