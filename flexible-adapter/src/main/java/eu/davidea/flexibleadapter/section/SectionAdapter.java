package eu.davidea.flexibleadapter.section;

public interface SectionAdapter {

    int getSectionCount();

    long getSectionId(int sectionIndex);
    
    long getChildId(int sectionIndex, int itemIndex);

    int getChildCount(int sectionIndex);

}
