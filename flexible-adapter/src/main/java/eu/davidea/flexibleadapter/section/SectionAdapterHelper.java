
package eu.davidea.flexibleadapter.section;

import android.support.v7.widget.RecyclerView;

public class SectionAdapterHelper {
    public static final long NO_SECTION_POSITION = 0xffffffffffffffffl;

    private static final long LOWER_32BIT_MASK = 0x00000000ffffffffl;
    private static final long LOWER_31BIT_MASK = 0x000000007fffffffl;

    /*package*/ static final int VIEW_TYPE_FLAG_IS_GROUP = 0x80000000;

    public static long getPackedPositionForChild(int sectionIndex, int sectionItemIndex) {
        return ((long) sectionItemIndex << 32) | (sectionIndex & LOWER_32BIT_MASK);
    }

    public static long getPackedPositionForSection(int sectionIndex) {
        return ((long) RecyclerView.NO_POSITION << 32) | (sectionIndex & LOWER_32BIT_MASK);
    }

    public static int getPackedPositionChild(long packedPosition) {
        return (int) (packedPosition >>> 32);
    }

    public static int getPackedPositionSection(long packedPosition) {
        return (int) (packedPosition & LOWER_32BIT_MASK);
    }

    public static long getCombinedChildId(long sectionId, long childId) {
        return ((sectionId & LOWER_31BIT_MASK) << 32) | (childId & LOWER_32BIT_MASK);
    }

    public static long getCombinedSectionId(long sectionId) {
        //noinspection PointlessBitwiseExpression
        return ((sectionId & LOWER_31BIT_MASK) << 32) | (RecyclerView.NO_ID & LOWER_32BIT_MASK);
    }

    public static boolean isSectionViewType(int rawViewType) {
        return ((rawViewType & VIEW_TYPE_FLAG_IS_GROUP) != 0);
    }

    public static int getSectionViewType(int rawViewType) {
        return (rawViewType & (~VIEW_TYPE_FLAG_IS_GROUP));
    }

    public static int getChildViewType(int rawViewType) {
        return (rawViewType & (~VIEW_TYPE_FLAG_IS_GROUP));
    }

    private SectionAdapterHelper() {
    }
}
