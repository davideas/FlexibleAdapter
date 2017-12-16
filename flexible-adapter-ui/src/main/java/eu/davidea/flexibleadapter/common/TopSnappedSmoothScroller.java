/*
 * Copyright 2016 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.flexibleadapter.common;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Common class for all Smooth Scroller Layout Managers.
 *
 * @since 5.0.0-b6 Creation
 * <br>5.0.0-b8 Class is now public to allow customization of the MILLISECONDS_PER_INCH
 */
public class TopSnappedSmoothScroller extends LinearSmoothScroller {

    /**
     * The modification of this value affects the creation of ALL Layout Managers.
     * <b>Note:</b> Every time you change this value you MUST recreate the LayoutManager instance
     * and to assign it again to the RecyclerView!
     * <p>Default value is {@code 100f}. Default Android value is {@code 25f}.</p>
     */
    public static float MILLISECONDS_PER_INCH = 100f;

    private PointF vectorPosition = new PointF(0, 0);
    private IFlexibleLayoutManager layoutManager;

    @SuppressWarnings("WeakerAccess")
    public TopSnappedSmoothScroller(Context context, RecyclerView.LayoutManager layoutManager) {
        super(context);
        this.layoutManager = new FlexibleLayoutManager(layoutManager);
    }

    /**
     * Controls the direction in which smoothScroll looks for your view
     *
     * @return the vector position
     */
    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        final int firstChildPos = layoutManager.findFirstCompletelyVisibleItemPosition();
        final int direction = targetPosition < firstChildPos ? -1 : 1;

        if (layoutManager.getOrientation() == OrientationHelper.HORIZONTAL) {
            vectorPosition.set(direction, 0);
            return vectorPosition;
        } else {
            vectorPosition.set(0, direction);
            return vectorPosition;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
    }

    @Override
    protected int getVerticalSnapPreference() {
        return SNAP_TO_START;
    }

}