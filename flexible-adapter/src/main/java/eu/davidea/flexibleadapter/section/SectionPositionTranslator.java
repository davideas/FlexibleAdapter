
package eu.davidea.flexibleadapter.section;

import android.support.v7.widget.RecyclerView;

import java.util.Arrays;

public class SectionPositionTranslator {
    private final int ALLOCATE_UNIT = 256;

    private final static long FLAG_EXPANDED = 0x0000000080000000l;
    private final static long LOWER_31BIT_MASK = 0x000000007fffffffl;
    private final static long LOWER_32BIT_MASK = 0x00000000ffffffffl;
    private final static long UPPER_32BIT_MASK = 0xffffffff00000000l;
 
    /*
     * bit 64-32: offset  (use for caching purpose)
     * bit 31:    expanded or not
     * bit 30-0:  child count
     */
    private long[] mCachedSectionPosInfo;

    /*
     * bit 31: reserved
     * bit 30-0: section id
     */
    private int[] mCachedSectionId;
    private int mSectionCount;
    private int mExpandedSectionCount;
    private int mExpandedChildCount;
    private int mEndOfCalculatedOffsetSectionPosition = RecyclerView.NO_POSITION;
    private SectionAdapter mAdapter;

    public SectionPositionTranslator() {
    }

    public void build(SectionAdapter adapter, boolean allExpanded) {
        final int sectionCount = adapter.getSectionCount();

        enlargeArraysIfNeeded(sectionCount, false);

        final long[] info = mCachedSectionPosInfo;
        final int[] ids = mCachedSectionId;
        int totalChildCount = 0;
        for (int i = 0; i < sectionCount; i++) {
            final long sectionId = adapter.getSectionId(i);
            final int childCount = adapter.getChildCount(i);

            if (allExpanded) {
                info[i] = (((long) (i + totalChildCount) << 32) | childCount) | FLAG_EXPANDED;
            } else {
                info[i] = (((long) i << 32) | childCount);
            }
            ids[i] = (int) (sectionId & LOWER_32BIT_MASK);

            totalChildCount += childCount;
        }

        mAdapter = adapter;
        mSectionCount = sectionCount;
        mExpandedSectionCount = (allExpanded) ? sectionCount : 0;
        mExpandedChildCount = (allExpanded) ? totalChildCount : 0;
        mEndOfCalculatedOffsetSectionPosition = Math.max(0, sectionCount - 1);
    }

    public void restoreExpandedSectionItems(
            int[] restoreSectionIds,
            SectionAdapter adapter) {
        if (restoreSectionIds == null || restoreSectionIds.length == 0) {
            return;
        }

        if (mCachedSectionPosInfo == null) {
            return;
        }

        // make ID + position packed array
        final long[] idAndPos = new long[mSectionCount];

        for (int i = 0; i < mSectionCount; i++) {
            idAndPos[i] = ((long) mCachedSectionId[i] << 32) | i;
        }

        // sort both arrays
        Arrays.sort(idAndPos);

        final boolean fromUser = false;

        // find matched items & apply
        int index = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < restoreSectionIds.length; i++) {
            final int id1 = restoreSectionIds[i];

            for (int j = index; j < idAndPos.length; j++) {
                final int id2 = (int) (idAndPos[j] >> 32);
                final int position = (int) (idAndPos[j] & LOWER_31BIT_MASK);

                if (id2 < id1) {
                    index = j;
                    collapseSection(position);
                } else if (id2 == id1) {
                    // matched
                    index = j + 1;
                    expandSection(position);
                } else { // id2 > id1
                    break;
                }
            }
        }

//        if (adapter != null || collapseListener != null) {
            for (int i = index; i < idAndPos.length; i++) {
                final int id2 = (int) (idAndPos[i] >> 32);
                final int position = (int) (idAndPos[i] & LOWER_31BIT_MASK);
                collapseSection(position);
//                if (adapter == null || adapter.onHookSectionCollapse(position, fromUser)) {
//                    if (collapseSection(position)) {
//                        if (collapseListener != null) {
//                            collapseListener.onSectionCollapse(position, fromUser);
//                        }
//                    }
//                }
            }
//        }
    }

    public int[] getSavedStateArray() {
        int[] expandedSections = new int[mExpandedSectionCount];

        int index = 0;
        for (int i = 0; i < mSectionCount; i++) {
            final long t = mCachedSectionPosInfo[i];
            if ((t & FLAG_EXPANDED) != 0) {
                expandedSections[index] = mCachedSectionId[i];
                index += 1;
            }
        }

        if (index != mExpandedSectionCount) {
            throw new IllegalStateException("may be a bug  (index = " + index + ", mExpandedSectionCount = " + mExpandedSectionCount + ")");
        }

        Arrays.sort(expandedSections);

        return expandedSections;
    }

    public int getItemCount() {
        return mSectionCount + mExpandedChildCount;
    }

    public boolean isSectionExpanded(int sectionPosition) {
        return ((mCachedSectionPosInfo[sectionPosition] & FLAG_EXPANDED) != 0);
    }

    public int getChildCount(int sectionPosition) {
        return (int) (mCachedSectionPosInfo[sectionPosition] & LOWER_31BIT_MASK);
    }

    public int getVisibleChildCount(int sectionPosition) {
        if (isSectionExpanded(sectionPosition)) {
            return getChildCount(sectionPosition);
        } else {
            return 0;
        }
    }

    public boolean collapseSection(int sectionPosition) {
        if ((mCachedSectionPosInfo[sectionPosition] & FLAG_EXPANDED) == 0) {
            return false;
        }

        final int childCount = (int) (mCachedSectionPosInfo[sectionPosition] & LOWER_31BIT_MASK);

        mCachedSectionPosInfo[sectionPosition] &= (~FLAG_EXPANDED);
        mExpandedSectionCount -= 1;

        mExpandedChildCount -= childCount;
        mEndOfCalculatedOffsetSectionPosition = Math.min(mEndOfCalculatedOffsetSectionPosition, sectionPosition);

        // requires notifyItemRangeRemoved()
        return true;
    }

    public boolean expandSection(int sectionPosition) {
        if ((mCachedSectionPosInfo[sectionPosition] & FLAG_EXPANDED) != 0) {
            return false;
        }

        final int childCount = (int) (mCachedSectionPosInfo[sectionPosition] & LOWER_31BIT_MASK);

        mCachedSectionPosInfo[sectionPosition] |= FLAG_EXPANDED;
        mExpandedSectionCount += 1;

        mExpandedChildCount += childCount;
        mEndOfCalculatedOffsetSectionPosition = Math.min(mEndOfCalculatedOffsetSectionPosition, sectionPosition);

        // requires notifyItemRangeInserted()
        return true;
    }

    public void moveSectionItem(int fromSectionPosition, int toSectionPosition) {
        if (fromSectionPosition == toSectionPosition) {
            return;
        }

        final long tmp1 = mCachedSectionPosInfo[fromSectionPosition];
        final int tmp2 = mCachedSectionId[fromSectionPosition];

        if (toSectionPosition < fromSectionPosition) {
            // shift to backward
            for (int i = fromSectionPosition; i > toSectionPosition; i--) {
                mCachedSectionPosInfo[i] = mCachedSectionPosInfo[i - 1];
                mCachedSectionId[i] = mCachedSectionId[i - 1];
            }
        } else {
            // shift to forward
            for (int i = fromSectionPosition; i < toSectionPosition; i++) {
                mCachedSectionPosInfo[i] = mCachedSectionPosInfo[i + 1];
                mCachedSectionId[i] = mCachedSectionId[i + 1];
            }
        }

        mCachedSectionPosInfo[toSectionPosition] = tmp1;
        mCachedSectionId[toSectionPosition] = tmp2;

        final int minPosition = Math.min(fromSectionPosition, toSectionPosition);

        if (minPosition > 0) {
            mEndOfCalculatedOffsetSectionPosition = Math.min(mEndOfCalculatedOffsetSectionPosition, minPosition - 1);
        } else {
            mEndOfCalculatedOffsetSectionPosition = RecyclerView.NO_POSITION;
        }
    }

    public void moveChildItem(int fromSectionPosition, int fromChildPosition, int toSectionPosition, int toChildPosition) {
        if (fromSectionPosition == toSectionPosition) {
            return;
        }

        final int fromChildCount = (int) (mCachedSectionPosInfo[fromSectionPosition] & LOWER_31BIT_MASK);
        final int toChildCount = (int) (mCachedSectionPosInfo[toSectionPosition] & LOWER_31BIT_MASK);

        if (fromChildCount == 0) {
            throw new IllegalStateException("moveChildItem(" +
                    "fromSectionPosition = " + fromSectionPosition +
                    ", fromChildPosition = " + fromChildPosition +
                    ", toSectionPosition = " + toSectionPosition +
                    ", toChildPosition = " + toChildPosition + ")  --- may be a bug.");
        }

        mCachedSectionPosInfo[fromSectionPosition] = (mCachedSectionPosInfo[fromSectionPosition] & (UPPER_32BIT_MASK | FLAG_EXPANDED)) | (fromChildCount - 1);
        mCachedSectionPosInfo[toSectionPosition] = (mCachedSectionPosInfo[toSectionPosition] & (UPPER_32BIT_MASK | FLAG_EXPANDED)) | (toChildCount + 1);

        if ((mCachedSectionPosInfo[fromSectionPosition] & FLAG_EXPANDED) != 0) {
            mExpandedChildCount -= 1;
        }
        if ((mCachedSectionPosInfo[toSectionPosition] & FLAG_EXPANDED) != 0) {
            mExpandedChildCount += 1;
        }

        final int minPosition = Math.min(fromSectionPosition, toSectionPosition);

        if (minPosition > 0) {
            mEndOfCalculatedOffsetSectionPosition = Math.min(mEndOfCalculatedOffsetSectionPosition, minPosition - 1);
        } else {
            mEndOfCalculatedOffsetSectionPosition = RecyclerView.NO_POSITION;
        }
    }

    public long getExpandablePosition(int flatPosition) {
        if (flatPosition == RecyclerView.NO_POSITION) {
            return SectionAdapterHelper.NO_SECTION_POSITION;
        }

        final int sectionCount = mSectionCount;

        // final int startIndex = 0;
        final int startIndex = binarySearchSectionPositionByFlatPosition(mCachedSectionPosInfo, mEndOfCalculatedOffsetSectionPosition, flatPosition);
        long expandablePosition = SectionAdapterHelper.NO_SECTION_POSITION;
        int endOfCalculatedOffsetSectionPosition = mEndOfCalculatedOffsetSectionPosition;
        int offset = (startIndex == 0) ? 0 : (int) (mCachedSectionPosInfo[startIndex] >>> 32);

        for (int i = startIndex; i < sectionCount; i++) {
            final long t = mCachedSectionPosInfo[i];

            // update offset info
            mCachedSectionPosInfo[i] = (((long) offset << 32) | (t & LOWER_32BIT_MASK));
            endOfCalculatedOffsetSectionPosition = i;

            if (offset >= flatPosition) {
                // found (section item)
                expandablePosition = SectionAdapterHelper.getPackedPositionForSection(i);
                break;
            } else {
                offset += 1;
            }

            if ((t & FLAG_EXPANDED) != 0) {
                final int childCount = (int) (t & LOWER_31BIT_MASK);

                if ((childCount > 0) && (offset + childCount - 1) >= flatPosition) {
                    // found (child item)
                    expandablePosition = SectionAdapterHelper.getPackedPositionForChild(i, (flatPosition - offset));
                    break;
                } else {
                    offset += childCount;
                }
            }
        }

        mEndOfCalculatedOffsetSectionPosition = Math.max(mEndOfCalculatedOffsetSectionPosition, endOfCalculatedOffsetSectionPosition);

        return expandablePosition;
    }

    public int getFlatPosition(long packedPosition) {
        if (packedPosition == SectionAdapterHelper.NO_SECTION_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        final int sectionPosition = SectionAdapterHelper.getPackedPositionSection(packedPosition);
        final int childPosition = SectionAdapterHelper.getPackedPositionChild(packedPosition);
        final int sectionCount = mSectionCount;

        if (!(sectionPosition >= 0 && sectionPosition < sectionCount)) {
            return RecyclerView.NO_POSITION;
        }

        if (childPosition != RecyclerView.NO_POSITION) {
            if (!isSectionExpanded(sectionPosition)) {
                return RecyclerView.NO_POSITION;
            }
        }

        // final int startIndex = 0;
        final int startIndex = Math.max(0, Math.min(sectionPosition, mEndOfCalculatedOffsetSectionPosition));
        int endOfCalculatedOffsetSectionPosition = mEndOfCalculatedOffsetSectionPosition;
        int offset = (int) (mCachedSectionPosInfo[startIndex] >>> 32);
        int flatPosition = RecyclerView.NO_POSITION;

        for (int i = startIndex; i < sectionCount; i++) {
            final long t = mCachedSectionPosInfo[i];

            // update offset info
            mCachedSectionPosInfo[i] = (((long) offset << 32) | (t & LOWER_32BIT_MASK));
            endOfCalculatedOffsetSectionPosition = i;

            final int childCount = (int) (t & LOWER_31BIT_MASK);

            if (i == sectionPosition) {
                if (childPosition == RecyclerView.NO_POSITION) {
                    flatPosition = offset;
                } else if (childPosition < childCount) {
                    flatPosition = (offset + 1) + childPosition;
                }
                break;
            } else {
                offset += 1;

                if ((t & FLAG_EXPANDED) != 0) {
                    offset += childCount;
                }
            }
        }

        mEndOfCalculatedOffsetSectionPosition = Math.max(mEndOfCalculatedOffsetSectionPosition, endOfCalculatedOffsetSectionPosition);

        return flatPosition;
    }


    private static int binarySearchSectionPositionByFlatPosition(long[] array, int endArrayPosition, int flatPosition) {
        if (endArrayPosition <= 0) {
            return 0;
        }

        final int v1 = (int) (array[0] >>> 32);
        final int v2 = (int) (array[endArrayPosition] >>> 32);

        if (flatPosition <= v1) {
            return 0;
        } else if (flatPosition >= v2) {
            return endArrayPosition;
        }

        int lastS = 0;
        int s = 0;
        int e = endArrayPosition;

        while (s < e) {
            final int mid = (s + e) >>> 1;
            final int v = (int) (array[mid] >>> 32);

            if (v < flatPosition) {
                lastS = s;
                s = mid + 1;
            } else {
                e = mid;
            }
        }

        return lastS;
    }

    public void removeChildItem(int sectionPosition, int childPosition) {
        removeChildItems(sectionPosition, childPosition, 1);
    }

    public void removeChildItems(int sectionPosition, int childPositionStart, int count) {
        final long t = mCachedSectionPosInfo[sectionPosition];
        final int curCount = (int) (t & LOWER_31BIT_MASK);

        if (!((childPositionStart >= 0) && ((childPositionStart + count) <= curCount))) {
            throw new IllegalStateException(
                    "Invalid child position " +
                            "removeChildItems(sectionPosition = " + sectionPosition + ", childPosition = " + childPositionStart + ", count = " + count + ")");
        }

        if ((t & FLAG_EXPANDED) != 0) {
            mExpandedChildCount -= count;
        }

        mCachedSectionPosInfo[sectionPosition] = (t & (UPPER_32BIT_MASK | FLAG_EXPANDED)) | (curCount - count);
        mEndOfCalculatedOffsetSectionPosition = Math.min(mEndOfCalculatedOffsetSectionPosition, sectionPosition - 1);
    }

    public void insertChildItem(int sectionPosition, int childPosition) {
        insertChildItems(sectionPosition, childPosition, 1);
    }

    public void insertChildItems(int sectionPosition, int childPositionStart, int count) {
        final long t = mCachedSectionPosInfo[sectionPosition];
        final int curCount = (int) (t & LOWER_31BIT_MASK);

        if (!((childPositionStart >= 0) && (childPositionStart <= curCount))) {
            throw new IllegalStateException(
                    "Invalid child position " +
                            "insertChildItems(sectionPosition = " + sectionPosition + ", childPositionStart = " + childPositionStart + ", count = " + count + ")");
        }

        if ((t & FLAG_EXPANDED) != 0) {
            mExpandedChildCount += count;
        }

        mCachedSectionPosInfo[sectionPosition] = (t & (UPPER_32BIT_MASK | FLAG_EXPANDED)) | (curCount + count);
        mEndOfCalculatedOffsetSectionPosition = Math.min(mEndOfCalculatedOffsetSectionPosition, sectionPosition);
    }

    public int insertSectionItems(int sectionPosition, int count, boolean expanded) {
        if (count <= 0) {
            return 0;
        }

        final int n = count;

        enlargeArraysIfNeeded(mSectionCount + n, true);

        // shift to backward
        final SectionAdapter adapter = mAdapter;
        final long[] info = mCachedSectionPosInfo;
        final int[] ids = mCachedSectionId;

        int start = mSectionCount - 1 + n;
        int end = sectionPosition - 1 + n;
        for (int i = start; i > end; i--) {
            info[i] = info[i - n];
            ids[i] = ids[i - n];
        }

        // insert items
        final long expandedFlag = (expanded) ? FLAG_EXPANDED : 0;
        int insertedChildCount = 0;
        int end2 = sectionPosition + n;
        for (int i = sectionPosition; i < end2; i++) {
            final long sectionId = adapter.getSectionId(i);
            final int childCount = adapter.getChildCount(i);

            info[i] = (((long) i << 32) | childCount) | expandedFlag;
            ids[i] = (int) (sectionId & LOWER_32BIT_MASK);

            insertedChildCount += childCount;
        }

        mSectionCount += n;
        if (expanded) {
            mExpandedSectionCount += n;
            mExpandedChildCount += insertedChildCount;
        }

        int calculatedOffset = (mSectionCount == 0) ? RecyclerView.NO_POSITION : (sectionPosition - 1);
        mEndOfCalculatedOffsetSectionPosition = Math.min(mEndOfCalculatedOffsetSectionPosition, calculatedOffset);

        return (expanded) ? (n + insertedChildCount) : n;
    }

    public int insertSectionItem(int sectionPosition, boolean expanded) {
        return insertSectionItems(sectionPosition, 1, expanded);
    }

    public int removeSectionItems(int sectionPosition, int count) {
        if (count <= 0) {
            return 0;
        }

        final int n = count;
        int removedVisibleItemCount = 0;

        for (int i = 0; i < n; i++) {
            final long t = mCachedSectionPosInfo[sectionPosition + i];

            if ((t & FLAG_EXPANDED) != 0) {
                int visibleChildCount = (int) (t & LOWER_31BIT_MASK);
                removedVisibleItemCount += visibleChildCount;
                mExpandedChildCount -= visibleChildCount;
                mExpandedSectionCount -= 1;
            }
        }
        removedVisibleItemCount += n;
        mSectionCount -= n;

        // shift to forward
        for (int i = sectionPosition; i < mSectionCount; i++) {
            mCachedSectionPosInfo[i] = mCachedSectionPosInfo[i + n];
            mCachedSectionId[i] = mCachedSectionId[i + n];
        }

        int calculatedOffset = (mSectionCount == 0) ? RecyclerView.NO_POSITION : (sectionPosition - 1);
        mEndOfCalculatedOffsetSectionPosition = Math.min(mEndOfCalculatedOffsetSectionPosition, calculatedOffset);

        return removedVisibleItemCount;
    }

    public int removeSectionItem(int sectionPosition) {
        return removeSectionItems(sectionPosition, 1);
    }


    private void enlargeArraysIfNeeded(int size, boolean preserveData) {
        int allocSize = (size + (2 * ALLOCATE_UNIT - 1)) & ~(ALLOCATE_UNIT - 1);

        long[] curInfo = mCachedSectionPosInfo;
        int[] curId = mCachedSectionId;
        long[] newInfo = curInfo;
        int[] newId = curId;

        if (curInfo == null || curInfo.length < size) {
            newInfo = new long[allocSize];
        }
        if (curId == null || curId.length < size) {
            newId = new int[allocSize];
        }

        if (preserveData) {
            if (curInfo != null && curInfo != newInfo) {
                System.arraycopy(curInfo, 0, newInfo, 0, curInfo.length);
            }
            if (curId != null && curId != newId) {
                System.arraycopy(curId, 0, newId, 0, curId.length);
            }
        }

        mCachedSectionPosInfo = newInfo;
        mCachedSectionId = newId;
    }

    public int getExpandedSectionsCount() {
        return mExpandedSectionCount;
    }

    public int getCollapsedSectionsCount() {
        return mSectionCount - mExpandedSectionCount;
    }

    public boolean isAllExpanded() {
        return !isEmpty() && (mExpandedSectionCount == mSectionCount);
    }

    public boolean isAllCollapsed() {
        return isEmpty() || (mExpandedSectionCount == 0);
    }

    public boolean isEmpty() {
        return mSectionCount == 0;
    }
}
