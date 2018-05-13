/*
 * Copyright (C) 2017-2018 Davidea Solutions Sprl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.samples.flexibleadapter.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.samples.flexibleadapter.R;

/**
 * Custom header view for Toolbars to display title and subtitle.
 * <p>Can be customized to fit specific use case.</p>
 */
public class HeaderView extends LinearLayout {

    @BindView(R.id.header_view_title)
    TextView title;
    @BindView(R.id.header_view_sub_title)
    TextView subTitle;

    public HeaderView(Context context) {
        this(context, null);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void bindTo(CharSequence title) {
        bindTo(title, "");
    }

    public void bindTo(CharSequence title, CharSequence subTitle) {
        hideOrSetText(this.title, title);
        hideOrSetText(this.subTitle, subTitle);
    }

    private static void hideOrSetText(TextView tv, CharSequence text) {
        if (text == null || text.equals(""))
            tv.setVisibility(GONE);
        else
            tv.setText(text);
    }

}