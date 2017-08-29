package eu.davidea.flexibleadapter;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class ItemComparatorTest {

    private SimpleHeader[] headers;
    private SimpleItemComparator comparator;
    private List<AbstractFlexibleItem> initItems;

    @Before
    public void setup() {
        headers = new SimpleHeader[]{new SimpleHeader("a"), new SimpleHeader("b"), new SimpleHeader("c")};
        comparator = new SimpleItemComparator();
        initItems = new ArrayList<>();
        initItems.add(new SimpleItem(headers[1], "ba"));
        initItems.add(new SimpleItem(headers[1], "bb"));
        initItems.add(new SimpleItem(headers[1], "bc"));
        initItems.add(new SimpleItem(headers[2], "ca"));
        initItems.add(new SimpleItem(headers[2], "cb"));
        initItems.add(new SimpleItem(headers[2], "cc"));
    }

    @Test
    public void testCalculatePositionFor_AddItem() {
        FlexibleAdapter<AbstractFlexibleItem> adapter = new FlexibleAdapter<>(initItems);
        adapter.setDisplayHeadersAtStartUp(true);

        SimpleItem aa = new SimpleItem(headers[0], "aa");
        SimpleItem ab = new SimpleItem(headers[0], "ab");
        SimpleItem ac = new SimpleItem(headers[0], "ac");
        System.out.println("addItem precondition: " + initItems);

        adapter.addItem(adapter.calculatePositionFor(ab, comparator), ab);
        assertEquals(1, adapter.getGlobalPositionOf(ab));
        System.out.println("addItem item ab: " + initItems);

        adapter.addItem(adapter.calculatePositionFor(aa, comparator), aa);
        assertEquals(1, adapter.getGlobalPositionOf(aa));
        System.out.println("addItem item aa: " + initItems);

        adapter.addItem(adapter.calculatePositionFor(ac, comparator), ac);
        assertEquals(3, adapter.getGlobalPositionOf(ac));
        System.out.println("addItem item ac: " + initItems);
    }

    @Test
    public void testCalculatePositionFor_MoveItem() {
        //Integrate sorted initItems with new section of 3 sectionables + new header
        SimpleItem aa = new SimpleItem(headers[0], "aa");
        SimpleItem ab = new SimpleItem(headers[0], "ab");
        SimpleItem ac = new SimpleItem(headers[0], "ac");
        initItems.add(aa);
        initItems.add(ab);
        initItems.add(ac);
        Collections.sort(initItems, comparator);

        FlexibleAdapter<AbstractFlexibleItem> adapter = new FlexibleAdapter<>(initItems);
        adapter.setDisplayHeadersAtStartUp(true);

        //Precondition = Item "aa" has been added correctly
        System.out.println("moveItem Preconditions: " + initItems);
        assertEquals(1, adapter.getGlobalPositionOf(aa));

        //---------------------------------------------------------------
        //TEST1 = Item is before the target position in the same section "a"
        //Precondition = value for item from "aa" to "aba"
        aa.title = "aba";

        //Calculate position for "aba" to be after element "ab"
        int positionTo = adapter.calculatePositionFor(aa, comparator);
        adapter.moveItem(adapter.getGlobalPositionOf(aa), positionTo);
        assertEquals(2, adapter.getGlobalPositionOf(aa));
        System.out.println("moveItem item a.aa to a.aba expecting position 2: " + initItems);

        //---------------------------------------------------------------
        //TEST2 = Item is before the target position different section "b"
        // Precondition = Change header and value for item "aa" to "bd"
        aa.setHeader(headers[1]);
        aa.title = "bd";

        positionTo = adapter.calculatePositionFor(aa, comparator);
        adapter.moveItem(adapter.getGlobalPositionOf(aa), positionTo);
        assertEquals(7, adapter.getGlobalPositionOf(aa));
        System.out.println("moveItem item a.aba to b.bd expecting position 7: " + initItems);

        //---------------------------------------------------------------
        //TEST3 = Item is after the target position same section "b"
        // Precondition = Change value for item "aa" to "baa"
        aa.title = "baa";

        positionTo = adapter.calculatePositionFor(aa, comparator);
        adapter.moveItem(adapter.getGlobalPositionOf(aa), positionTo);
        assertEquals(5, adapter.getGlobalPositionOf(aa));
        System.out.println("moveItem item b.bd to b.baa expecting position 5: " + initItems);

        //---------------------------------------------------------------
        //TEST4 = Item is after the target position different section "a" (back to its original position)
        // Precondition = Change header and value for item "aa" to "bd"
        aa.setHeader(headers[0]);
        aa.title = "aa";

        positionTo = adapter.calculatePositionFor(aa, comparator);
        adapter.moveItem(adapter.getGlobalPositionOf(aa), positionTo);
        assertEquals(1, adapter.getGlobalPositionOf(aa));
        System.out.println("moveItem item b.baa to a.aa expecting position 1: " + initItems);
    }

    @Test
    public void testCalculatePositionFor_AddSection() {
        FlexibleAdapter<AbstractFlexibleItem> adapter = new FlexibleAdapter<>(initItems);
        adapter.setDisplayHeadersAtStartUp(true);

        //TEST1 - Head of the list
        int position = adapter.calculatePositionFor(headers[0], comparator);
        adapter.addItem(position, headers[0]);
        assertEquals(0, adapter.getGlobalPositionOf(headers[0]));
        System.out.println("addItem new Section a (Head): " + initItems);

        //TEST1 - Tail of the list
        SimpleHeader d = new SimpleHeader("d");
        position = adapter.calculatePositionFor(d, comparator);
        adapter.addItem(position, d);
        assertEquals(9, adapter.getGlobalPositionOf(d));
        System.out.println("addItem new Section d (Tail): " + initItems);
    }

    @Test
    public void testComparator_AddSection() {
        FlexibleAdapter<AbstractFlexibleItem> adapter = new FlexibleAdapter<>(initItems);
        adapter.setDisplayHeadersAtStartUp(true);

        adapter.addSection(headers[0], comparator);
        assertEquals(0, adapter.getGlobalPositionOf(headers[0]));
        System.out.println("addSection new Section a (Head): " + initItems);
    }

    @Test
    public void testComparator_AddItemToSection() {
        FlexibleAdapter<AbstractFlexibleItem> adapter = new FlexibleAdapter<>(initItems);
        adapter.setDisplayHeadersAtStartUp(true);

        SimpleItem aa = new SimpleItem(headers[0], "aa");
        SimpleItem ab = new SimpleItem(headers[0], "ab");

        //TEST1 = New Item for a New Section (internal use of calculatePositionFor())
        adapter.addItemToSection(aa, headers[0], comparator);
        assertEquals(1, adapter.getGlobalPositionOf(aa));
        System.out.println("addItemToSection item aa, new Section: " + initItems);

        //TEST2 = New Item for an Existing Section
        adapter.addItemToSection(ab, headers[0], comparator);
        assertEquals(2, adapter.getGlobalPositionOf(ab));
        System.out.println("addItemToSection item ab, existing Section: " + initItems);
    }

    @Test
    public void testComparator() {
        SimpleItem aa = new SimpleItem(headers[0], "aa");
        SimpleItem ba = new SimpleItem(headers[1], "ba");
        SimpleItem cb = new SimpleItem(headers[2], "cb");

        List<AbstractFlexibleItem> sortArray = new ArrayList<>();
        sortArray.add(ba);
        sortArray.add(aa);
        sortArray.add(cb);
        sortArray.add(headers[2]);
        sortArray.add(headers[0]);
        sortArray.add(headers[1]);

        Collections.sort(sortArray, comparator);

        assertEquals(sortArray.get(0), headers[0]);
        assertEquals(sortArray.get(1), aa);
        assertEquals(sortArray.get(2), headers[1]);
        assertEquals(sortArray.get(3), ba);
        assertEquals(sortArray.get(4), headers[2]);
        assertEquals(sortArray.get(5), cb);
    }

    class SimpleItemComparator implements Comparator<IFlexible> {

        @Override
        public int compare(IFlexible v1, IFlexible v2) {
            int result = 0;
            if (v1 instanceof SimpleHeader && v2 instanceof SimpleHeader) {
                result = ((SimpleHeader) v1).title.compareTo(((SimpleHeader) v2).title);

            } else if (v1 instanceof SimpleItem && v2 instanceof SimpleItem) {
                result = ((SimpleItem) v1).getHeader().title.compareTo(((SimpleItem) v2).getHeader().title);
                if (result == 0)
                    result = ((SimpleItem) v1).title.compareTo(((SimpleItem) v2).title);

            } else if (v1 instanceof SimpleItem && v2 instanceof SimpleHeader) {
                result = ((SimpleItem) v1).getHeader().title.compareTo(((SimpleHeader) v2).title);
                if (result == 0) result--;

            } else if (v1 instanceof SimpleHeader && v2 instanceof SimpleItem) {
                result = ((SimpleHeader) v1).title.compareTo(((SimpleItem) v2).getHeader().title);
                if (result == 0) result--;
            }
            return result;
        }
    }

    private class SimpleHeader extends AbstractHeaderItem<FlexibleViewHolder> {
        String title;

        SimpleHeader(String title) {
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SimpleHeader && title.equals(((SimpleHeader) o).title);
        }

        @Override
        public int getLayoutRes() {
            return 0;
        }

        @Override
        public FlexibleViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
            return null;
        }

        @Override
        public void bindViewHolder(FlexibleAdapter adapter, FlexibleViewHolder holder, int position, List payloads) {

        }

        @Override
        public String toString() {
            return "/" + title;
        }
    }

    private class SimpleItem extends AbstractSectionableItem<FlexibleViewHolder, SimpleHeader> {
        String title;

        SimpleItem(SimpleHeader header, String title) {
            super(header);
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SimpleItem && title.equals(((SimpleItem) o).title);
        }

        @Override
        public int getLayoutRes() {
            return 0;
        }

        @Override
        public FlexibleViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
            return null;
        }

        @Override
        public void bindViewHolder(FlexibleAdapter adapter, FlexibleViewHolder holder, int position, List payloads) {

        }

        @Override
        public String toString() {
            return getHeader().title + "." + title;
        }
    }

}