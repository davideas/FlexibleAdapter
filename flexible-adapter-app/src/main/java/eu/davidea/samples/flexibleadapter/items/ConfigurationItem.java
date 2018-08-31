package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import eu.davidea.common.SimpleSeekBarChangeListener;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ConfigurationItem extends AbstractFlexibleItem<ConfigurationItem.ViewHolder> {

    public static final int NONE = -1, SEEK_BAR = 0, SWITCH = 1;

    private String id, title, description;
    private int widgetType;
    private int value, maxValue, stepValue;

    @IntDef({NONE, SEEK_BAR, SWITCH})
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
    public ViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ViewHolder(view, adapter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindViewHolder(FlexibleAdapter adapter, final ViewHolder holder, int position, List payloads) {

        if (getDescription() != null) {
            holder.mDescription.setVisibility(View.VISIBLE);
            holder.mDescription.setText(Utils.fromHtmlCompat(getDescription()));
        } else {
            holder.mDescription.setVisibility(View.GONE);
        }
        switch (getWidgetType()) {
            case NONE:
                holder.mTitle.setText(getTitle());
                Utils.textAppearanceCompat(holder.mTitle,
                        android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Display1);
                Utils.textAppearanceCompat(holder.mDescription,
                        android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Subhead);
                holder.mSeekBar.setVisibility(View.GONE);
                holder.mSwitchView.setVisibility(View.GONE);
                break;

            case SEEK_BAR:
                holder.mTitle.setText(Utils.fromHtmlCompat(getTitle() + " " + getValue()));
                holder.mSeekBar.setVisibility(View.VISIBLE);
                holder.mSwitchView.setVisibility(View.GONE);
                holder.mSeekBar.setMax(getMaxValue());
                holder.mSeekBar.setProgress(getValue());
                holder.mSeekBar.setKeyProgressIncrement(getStepValue());
                holder.mSeekBar.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        withValue(progress);
                        holder.mTitle.setText(Utils.fromHtmlCompat(getTitle() + " " + progress));
                        DatabaseConfiguration.setConfiguration(getId(), progress);
                    }
                });
                break;

            case SWITCH:
                holder.mTitle.setText(getTitle());
                holder.mSeekBar.setVisibility(View.GONE);
                holder.mSwitchView.setVisibility(View.VISIBLE);
                holder.mSwitchView.setChecked(getValue() == 1);
                holder.mSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) withValue(1);
                        else withValue(0);
                        DatabaseConfiguration.setConfiguration(getId(), getValue());
                    }
                });
                break;
        }
    }

    static class ViewHolder extends FlexibleViewHolder {

        public TextView mTitle;
        public TextView mDescription;
        public SeekBar mSeekBar;
        public Switch mSwitchView;

        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//True for sticky
            mTitle = view.findViewById(R.id.title);
            mDescription = view.findViewById(R.id.subtitle);
            mSeekBar = view.findViewById(R.id.seek_bar);
            mSwitchView = view.findViewById(R.id.switch_box);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            if (mSwitchView.getVisibility() == View.VISIBLE) {
                mSwitchView.setChecked(!mSwitchView.isChecked());
            }
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.alphaAnimator(animators, itemView, 0f);
        }
    }

    @Override
    public String toString() {
        return "ConfigurationItem[id=" + id + "]";
    }

}