[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FlexibleAdapter-green.svg?style=flat)](http://android-arsenal.com/details/1/2207)
[![Download](https://api.bintray.com/packages/davideas/maven/flexible-adapter/images/download.svg) ](https://bintray.com/davideas/maven/flexible-adapter/_latestVersion)
[![API](https://img.shields.io/badge/API-14%2B-green.svg?style=flat)](https://android-arsenal.com/api?level=14)
[![Licence](https://img.shields.io/badge/Licence-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# FlexibleAdapter

###### The only Adapter multi-function for your RecyclerView
- NEW! Beta version: 5.0.0-b7 built on 2016.06.20 (usable library!) (111KB)

####ANNOUNCEMENT: Important and Revolutionary changes are foreseen in v5.0.0. Please see [issues](https://github.com/davideas/FlexibleAdapter/issues) and [releases](https://github.com/davideas/FlexibleAdapter/releases).

> When initially Android introduced the RecyclerView widget, we had to implement a custom Adapter in several applications, again and again to provide the items for our views. Since I created this library, it has become easy to configure how views will be displayed in a list. Thanks to a library like this, nobody wants to use a ListView anymore.

The FlexibleAdapter helps developers to simplify this process without to worry about the Adapter anymore. It's easy to extend, it has predefined logic for different situations and prevents common mistakes.
This library is configurable and it guides the developers (thanks to quality comments on the methods) to create a better user experience and now, even more with the new ViewHolders and new actions.

#### Main functionalities
* Simple item selection with ripple effect, Single & Multi selection mode.
* Restore deleted items (undo delete), **NEW** works with Expandable items too!
* FastScroller, **NEW** now in the library supporting all the 3 Layouts.
* Customizable ItemDecoration.
* SearchFilter with Spannable text, **NEW** now items are animated. Works with sub items too!
* Add and Remove items with custom animations.
* **NEW!** Adapter Animations with custom configuration based on adapter position and beyond.
* **NEW!** Predefined ViewHolders.
* **NEW!** Expandable items with <u>Selection Coherence</u>, multi-level expansion.
* **NEW!** Drag&Drop and Swipe-To-Dismiss with Leave-Behind pattern, with <u>Selection Coherence</u>.
* **NEW!** Headers/Sections with sticky behaviour fully clickable, collapsible, automatic linkage!
* **NEW!** Auto mapping ViewTypes with Item interfaces.
* **NEW!** Innovative EndlessScroll with Adapter binding (<u>No OnScrollListener</u>).
* **NEW!** UndoHelper &amp; ActionModeHelper.
* **NEW!** DrawableUtils for dynamic backgrounds (<u>No XML</u>).
* **NEW!** 1 simple constructor for all events.
* **NEW!** Easy runtime position calculation for adding/moving items in sections. 

#### How is made
Some simple functionalities have been implemented thanks to some Blogs (see at the bottom of the page), merged and methods have been improved for speed and scalability, for all Activities that use a RecyclerView.

* At lower level there is `SelectableAdapter` class. It provides selection functionalities and it's able to _maintain the state_ after the rotation: you just need to call the onSave/onRestore methods from the Activity!
* At middle level, the `AnimatorAdapter` class has been added to give some animation at startup and when user scrolls.
* At front level, the core class `FlexibleAdapter`. It holds and handles the main list, performs actions on all different types of item paying attention at the adding and removal of the items, as well as the new concept of "selection coherence".
* Item interfaces and predefined ViewHolders complete the whole library giving more actions to the items and configuration options to the developers and the end user.

# Screenshots
![Drag Grid & Overall](/screenshots/drag_grid_overall.png)
![Secondary Functionalities](/screenshots/secondary_functionalities.png)
![StickyHeaders & EndlessScrolling](/screenshots/sticky_headers.png)

![Multi Selection & SC](/screenshots/multi_selection_sc.png)
![Undo](/screenshots/undo_single_selection.png)
![Drag Linear](/screenshots/drag_linear.png)

![Search](/screenshots/search_sections.png)
![swipe-to-dismiss1](/screenshots/swipe-to-dismiss1.png)
![swipe-to-dismiss2](/screenshots/swipe-to-dismiss2.png)

![Adapter Animations](/screenshots/adapter_animations.png)
![Dynamic Sections](/screenshots/dynamic_sections.png)
![Staggered Layout](/screenshots/dynamic_staggered_layout.png)

# Setup
```
repositories {
	jcenter()
	maven {url = "http://dl.bintray.com/davideas/maven" }
	maven {url = "https://oss.sonatype.org/content/repositories/snapshots/" } //For Snapshots
}
```
```
dependencies {
	//Using JCenter
	compile 'eu.davidea:flexible-adapter:4.2.0'
	compile 'eu.davidea:flexible-adapter:5.0.0-b7'
	
	//Using MavenSnapshots repository for continuous updates from my development
	compile 'eu.davidea:flexible-adapter:5.0.0-SNAPSHOT'
}
```

#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!<br/>
Active discussion [Snapshots and Pre-Releases for FlexibleAdapter v5.0.0](https://github.com/davideas/FlexibleAdapter/issues/39).

# Wiki!
I strong suggest to read the **new [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) pages**.

Wiki pages have been completely reviewed to support all the coming functionalities from 5.0.0.

Not all pages are filled, working in progress :-)

# Change Log
###### v5.0.0-b7 - 2016.06.20
_Deprecation and Refactoring_ (Changes that you have to take care from all previous versions)
- Refactored class signature for `IHeader` and `IExpandable` and their abstract implementation, [see #117].
- Refactored method `moveItem()` to `swapItem()`.
- Separated the initialization of expanded items at start up [see #86]: `expandItemsAtStartUp()` must be explicitly called by the user in Activity creation phase, `expandItemsAtStartUp()` method now uses the existent `expand()` method with initialization info.
- Items are _NOT_ Draggable and Swipeable by default.
- Experimenting `TreeSet` instead of `ArrayList` for `mSelectedPositions`. As consequence `getSelectedPositions()` returns a _copied_ List from the Set (already sorted, no duplication)!
- Deprecated method `addSection()` with refHeader, will be removed from next release.

_Improvements_
- Dragging and swiping can be enabled by type [see #80].
- Alpha animation, when enabling / disabling sticky headers.
- Added new method `getItemTouchHelperCallback()` to customize Swipe and Drag behaviors.
- Added new method `calculatePositionFor()`.
- Added new method `addSection()` with Comparator object.
- Added new method `moveItem()` that simply moves an item without swapping it.
- Added new method `calculatePositionFor()` useful for new items of every type, a custom `Comparator` is needed.
- Improved method `addItemToSection()` see JavaDoc.
- Improved method `updateDataSet()`, optionally items can be animated, when `false` a `notifyDataSetChanged()` will be executed.
- Added 2 new overload methods for `expand()` series [see #110].
- Added class `UndoHelper.SimpleActionListener` to simplify the development.
- Added class `DrawableUtils` to manage the background Drawables at runtime without XML configuration [see #35].
- Added new base interface `OnActionStateListener` for `OnItemMoveListener` and `OnItemSwipeListener` to overcome the drag issue with the `SwipeRefreshLayout`, [see #104 and #83].
- Added possibility to change mode how `DividerItemDecorator` is drawn, over or (default) underneath the item [see #106].
- Added `applyAndAnimateMovedItems()` when using `animateTo()` (in filter and `updateDataSet()`).
- Full support for `StaggeredGridLayout` [see #107].
- Better selection management: made public `addSelection()`, `removeSelection()`, the new 2 methods don't notify any change as `toggleSelection()` does.
- `Utils.colorAccent` is now public.
- `onUpdateEmptyView()` is now called only if there was a change from 0 / to 0 itemCount [see #113].
- Chaining the Adapter for all initialization and configuration methods (sets).
- Upgrade to SupportLib 23.4.0.

_Fixes_
- Fixed headers showing after rotation [see #88].
- Fixed `addItemToSection` when Header is hidden: item is now added to the unique visible list.
- Solution to improve ViewHolder with sticky behaviour, (user has to pass `true` as new parameter to the FVH super.constructor). [see #78, #79 and #92].
- Removed useless call in `onChanged()`, causing duplication of subItems when updating the data set [see #95 and #96].
- Fixed hidden status when removing a Header item.
- Fixed Item does not updated after `updateDataSet()` [see #94].
- Fixed NPE bug during filter and null subList [see #103].
- Fixed NPE on originalText in `Utils.hightlightText()` [see #112].
- Fixed expansion status after filtering expandable items.
- Fixed animation changes when calling method `animateTo()`.
- Fixed ArrayIndexOutOfBoundsException with StaggeredGridLayoutManager [see #87, part of changes of #107].
- Adjusted selection flags when removing items and fixed small bug when restoring items after applying the filter and vice-versa.
- Method `onUpdateEmptyView()` is now called when passing null or empty new list in `updateDataSet()`.
- Filter on restore instance state bug [see #111].
- Fixed header View params when higher than Item view params [see P.R. #116].
- Fixed Constraint on `FlexibleViewHolder` when using StickyHeader functionality, as consequence changed class signature of `IHeader` item interface and its implementation. Also changed the class signature of `IExpandable` item interface and its implementation to have a ViewHolder of type `ExpandableViewHolder` as well [see #117].

_Demo App_
- Added new example Fragment for StaggeredLayout with multiple functionalities:
  - merge/split items at runtime (position are recalculated);
  - move item with animation taking care of the groups using the new function `calculatePositionFor()`;
  - automatic sorting when adding new items using the new function `addItemToSection()`;
  - example of Comparator object;
  - change Drawable background at runtime, maintaining the ripple effect (no XML is used).
- Fix for SwipeRefreshLayout to be displayed on the top of StickyHeaders Layout [see #83].
- SwipeRefresh with StickyHeader is not compatible with Dragging feature, added comments in the layout to show the difference #83: User must disable the SwipeRefreshLayout programmatically, to do it, I added a new callback function and relative listener `OnActionStateListener`.
- Adjusted demo App behavior when filtering with Endless Scrolling.
- Several minor changes.

###### Old releases
See [releases](https://github.com/davideas/FlexibleAdapter/releases) for old versions.

v5.0.0-b6 - 2016.05.01 | v5.0.0-b5 - 2016.04.04 | 
v5.0.0-b4 - 2016.02.21 | v5.0.0-b3 - 2016.02.08 |
v5.0.0-b2 - 2016.01.31 | v5.0.0-b1 - 2016.01.03 |
v4.2.0 - 2015.12.12 | v4.1.0 - 2015.11.29 |
v4.0.1 - 2015.11.01 | v4.0 - 2015.10.18 |
v3.1 - 2015.08.18 | v3.0 - 2015.07.29 |
v2.2 - 2015.07.20 | v2.1 - 2015.07.03 |
v2.0 - 2015.06.19 | v1.0 - 2015.05.03

# Limitations
Item half swipe cannot be implemented due to how the `android.support.v7.widget.helper.ItemTouchHelper` is done.
Half swipe can be done with others means, please see issues #98 and #100. See also commits of Apr 25, 2016. 

# Thanks
I've used these blogs as starting point:

http://enoent.fr/blog/2015/01/18/recyclerview-basics/

https://www.grokkingandroid.com/statelistdrawables-for-recyclerview-selection/

# Imported libraries

[LollipopContactsRecyclerViewFastScroller](https://github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller)<br/>
Improved and adapted to work in conjunction with `AnimatorAdapter`.

# Apps that use this Adapter
It will be a pleasure to add your App here.

# License

    Copyright 2015-2016 Davide Steduto

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
