/*
 * Copyright 2016-2018 Davide Steduto, Davidea Solutions Sprl
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
package eu.davidea.flexibleadapter.utils;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;
import eu.davidea.flexibleadapter.common.FlexibleLayoutManager;

/**
 * @author Davide Steduto
 * @since 07/12/2017 Created
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class LayoutUtils {

    /**
     * @return the string representation of the provided {@link Mode}
     * @since 5.0.0-rc1
     */
    @NonNull
    @SuppressLint("SwitchIntDef")
    public static String getModeName(@Mode int mode) {
        switch (mode) {
            case Mode.SINGLE:
                return "SINGLE";
            case Mode.MULTI:
                return "MULTI";
            default:
                return "IDLE";
        }
    }

    /**
     * @return the SimpleClassName of the provided object
     * @since 5.0.0-rc1
     */
    @NonNull
    public static String getClassName(@Nullable Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    /*-------------------------------*/
    /* RECYCLER-VIEW UTILITY METHODS */
    /*-------------------------------*/

    /**
     * Finds the layout orientation of the RecyclerView, no matter which LayoutManager is in use.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return one of {@link OrientationHelper#HORIZONTAL}, {@link OrientationHelper#VERTICAL}
     */
    public static int getOrientation(RecyclerView recyclerView) {
        return new FlexibleLayoutManager(recyclerView).getOrientation();
    }

    /**
     * Helper method to retrieve the number of the columns (span count) of the given LayoutManager.
     * <p>All Layouts are supported.</p>
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the span count
     * @since 5.0.0-b7
     */
    public static int getSpanCount(RecyclerView recyclerView) {
        return new FlexibleLayoutManager(recyclerView).getSpanCount();
    }

    /**
     * Helper method to find the adapter position of the <b>first completely</b> visible view
     * [for each span], no matter which Layout is.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the adapter position of the <b>first fully</b> visible item or {@code RecyclerView.NO_POSITION}
     * if there aren't any visible items.
     * @see #findFirstVisibleItemPosition(RecyclerView)
     * @since 5.0.0-b8
     */
    public static int findFirstCompletelyVisibleItemPosition(RecyclerView recyclerView) {
        return new FlexibleLayoutManager(recyclerView).findFirstCompletelyVisibleItemPosition();
    }

    /**
     * Helper method to find the adapter position of the <b>first partially</b> visible view
     * [for each span], no matter which Layout is.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the adapter position of the <b>first partially</b> visible item or {@code RecyclerView.NO_POSITION}
     * if there aren't any visible items.
     * @see #findFirstCompletelyVisibleItemPosition(RecyclerView)
     * @since 5.0.0-rc1
     */
    public static int findFirstVisibleItemPosition(RecyclerView recyclerView) {
        return new FlexibleLayoutManager(recyclerView).findFirstVisibleItemPosition();
    }

    /**
     * Helper method to find the adapter position of the <b>last completely</b> visible view
     * [for each span], no matter which Layout is.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the adapter position of the <b>last fully</b> visible item or {@code RecyclerView.NO_POSITION}
     * if there aren't any visible items.
     * @see #findLastVisibleItemPosition(RecyclerView)
     * @since 5.0.0-b8
     */
    public static int findLastCompletelyVisibleItemPosition(RecyclerView recyclerView) {
        return new FlexibleLayoutManager(recyclerView).findLastCompletelyVisibleItemPosition();
    }

    /**
     * Helper method to find the adapter position of the <b>last partially</b> visible view
     * [for each span], no matter which Layout is.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the adapter position of the <b>last partially</b> visible item or {@code RecyclerView.NO_POSITION}
     * if there aren't any visible items.
     * @see #findLastCompletelyVisibleItemPosition(RecyclerView)
     * @since 5.0.0-rc1
     */
    public static int findLastVisibleItemPosition(RecyclerView recyclerView) {
        return new FlexibleLayoutManager(recyclerView).findLastVisibleItemPosition();
    }

}