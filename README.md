[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FlexibleAdapter-green.svg?style=flat)](https://android-arsenal.com/details/1/2207) [![Download](https://api.bintray.com/packages/davideas/maven/flexible-adapter/images/download.svg) ](https://bintray.com/davideas/maven/flexible-adapter/_latestVersion) [![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

# FlexibleAdapter

###### A pattern for every RecyclerView
- Stable _light_ version: 4.2 built on 2015.12.16
- NEW! Beta version: 5.0.0-b6 built on 2016.05.01 (usable library!)

####ANNOUNCEMENT: Important and Revolutionary changes are foreseen in v5.0.0. Please see [issues](https://github.com/davideas/FlexibleAdapter/issues) and [releases](https://github.com/davideas/FlexibleAdapter/releases).

> When initially Android introduced the RecyclerView widget, we had to implement the Adapter in several applications, again and again to provide the items for our views. Since I created this library, it has become easy to configure how views will be displayed in a list. Thanks to a library like this, nobody wants to use a ListView anymore.

The FlexibleAdapter helps developers to simplify this process without to worry about the adapter anymore. It's easy to extends, it has predefined logic for different situations and prevents common mistakes.
This library is configurable and it guides the developers (thanks to quality comments on the methods) to create a better user experience and now, even more with the new ViewHolders and new actions.

#### Main functionalities
* Simple item selection with ripple effect, Single & Multi selection mode.
* Restore deleted items (undo delete), **NEW** works with Expandable items too!
* FastScroller, **NEW** now in the library and with GridLayout support.
* Customizable ItemDecoration.
* SearchFilter with Spannable text, **NEW** now items are animated. Works with sub items too!
* Add and Remove items with custom animations.
* **NEW!** Adapter Animations with custom configuration based on adapter position and beyond.
* **NEW!** Predefined ViewHolders.
* **NEW!** Expandable items with <u>Selection Coherence</u>, multi-level expansion.
* **NEW!** Drag&Drop and Swipe-To-Dismiss with Leave-Behind pattern, with <u>Selection Coherence</u>.
* **NEW!** Headers/Sections with sticky behaviour fully clickable, collapsible, automatic linkage!
* **NEW!** Auto mapping ViewTypes with Item interfaces.
* **NEW!** 1 simple constructor for all events.
* **NEW!** EndlessScroll with Adapter binding.
* **NEW!** UndoHelper &amp; ActionModeHelper.

#### How is made
Some simple functionalities have been implemented thanks to the some Blogs (see at the bottom of the page), merged and methods have been improved for speed and scalability, for all Activities that use a RecyclerView.

* At lower level there is `SelectableAdapter` class. It provides selection functionalities and it's able to _maintain the state_ after the rotation: you just need to call the onSave/onRestore methods from the Activity!
* At middle level, the `AnimatorAdapter` class has been added to give some animation at startup and when user scrolls.
* At front level, the core class `FlexibleAdapter`. It holds and handles the main list, performs actions on all different types of item paying attention at the adding and removal of the items, as well as the new concept of "selection coherence".
* Item interfaces and predefined ViewHolders complete the whole library giving more actions to the items and configuration options to the developers and the end user.

# Screenshots
![Drag Grid & Overall](/screenshots/drag_grid_overall.png)
![Secondary Functionalities](/screenshots/secondary_functionalities.png)
![StickyHeaders & EndlessScrolling](/screenshots/sticky_headers.png)

![Multi Selection & SC](/screenshots/multi_selection_sc.png)
![Search](/screenshots/search_sections.png)
![Undo](/screenshots/undo_single_selection.png)

![Drag Linear](/screenshots/drag_linear.png)
![swipe-to-dismiss1](/screenshots/swipe-to-dismiss1.png)
![swipe-to-dismiss2](/screenshots/swipe-to-dismiss2.png)

![Adapter Animations](/screenshots/adapter_animations.png)
![Dynamic Sections](/screenshots/dynamic_sections.png)

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
	//Using bintray.com
	compile 'eu.davidea:flexible-adapter:4.2.0@aar'
	compile 'eu.davidea:flexible-adapter:5.0.0-b6@aar'
	
	//Using JCenter
	compile 'eu.davidea:flexible-adapter:4.2.0'
	compile 'eu.davidea:flexible-adapter:5.0.0-b6'
	
	//Using MavenSnapshots repository for continuous updates from my development
	compile 'eu.davidea:flexible-adapter:5.0.0-SNAPSHOT'
}
```

#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!<br/>
Active discussion [Test FlexibleAdapter v5.0.0](https://github.com/davideas/FlexibleAdapter/issues/39).

# Wiki!
I strong suggest to read the **new [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) pages**.

Wiki pages have been completely reviewed to support all the coming functionalities from 5.0.0.

Not all pages are filled, working in progress :-)

# Change Log
###### v5.0.0-b6 - 2016.05.01
_Refactor_
- Removed `isSelected()` and `setSelected()` from `IFlexible` interface.
- Deprecated and removed `getSectionableOf()` in favor of the new `getSectionItems()`.
- Improved instance state on rotation.
- Renamed package `eu.davidea.examples` in `eu.davidea.samples`.

_Improvements_
- Brand new Swipe-To-Dismiss implementation: added custom rear views _Left/Right_! [See #21].
- Added `UndoHelper` [See #29].
- Added `ActionModeHelper` [See #30].
- Added **EndlessScroll/OnLoadMore**, using adapter binding, _not scroll listener!_ [See #43].
- Modified `DividerItemDecorator` to accept a custom resource and gap between sections [See #60].
- New methods: `addSection()`, `addItemToSection()` [See #62].
- Improved `isEnabled()` on `IExpandable` items.
- Improved methods `getSectionItems()` and `getHeaderItems()`.
- Support for `StaggeredGridLayoutManager` when using Adapter Animation.
- Added possibility to filter Header item AND to keep the header also if a subItem has been filtered in [See #73].

_Multi-level expansion_
- Added possibility to chain Expandable items into others expandable [See #63].
- Item restoration works in multi-level expansion: expanded children are forced to collapse before restoration.
- Selection coherence in multi-level expansion.

_FastScroller_
- Added a small gap between bubble and handle.
- Auto handling a change of a new LayoutManager when `FastScroller` is configured. Now FastScroller should work in every situations.
- Added SmoothScroll for `GridLayoutManager` and optimized smooth scrolling classes.

_Fixes_
- Fixed inconsistency bug when expanding/collapsing _sticky_ header items _not_ fully visible.
- Fixed StickyHeaders with `GridLayoutManager` when all sub-items of a section fit the single row of span count [See #61].
- Fixed #74: Initialization of the stickyHeader is laid out in post.
- Fixed the automatic linkage for headers and sections when delete and undo.
- Fixed the automatic linkage for headers and sections. Force collapsing when dragging expandable.
- Fixed NPE when setting a null `FastScroller` instance [See #66].
- Fixed new position in `addSection()`.
- Adjusted behaviours for swipe and drag view activation.
- Fixed the delay on the 1st item when animating the view.

_Demo App_
- Big code reorganization using fragments [See #36].
- Fixed #64 for demo app for API < 23.
- Enabled Header and Sections example with Design BottomSheet.
- Enabled FragmentSelectionModes example.
- Enabled Instagram example with Endless Scroll with Glide lib.

###### Old releases
See [releases](https://github.com/davideas/FlexibleAdapter/releases) for old versions.

v5.0.0-b5 - 2016.04.04 | 
v5.0.0-b4 - 2016.02.21 | v5.0.0-b3 - 2016.02.08 |
v5.0.0-b2 - 2016.01.31 | v5.0.0-b1 - 2016.01.03 |
v4.2.0 - 2015.12.12 | v4.1.0 - 2015.11.29 |
v4.0.1 - 2015.11.01 | v4.0 - 2015.10.18 |
v3.1 - 2015.08.18 | v3.0 - 2015.07.29 |
v2.2 - 2015.07.20 | v2.1 - 2015.07.03 |
v2.0 - 2015.06.19 | v1.0 - 2015.05.03

# Thanks
I've used these blogs as starting point:

http://enoent.fr/blog/2015/01/18/recyclerview-basics/

https://www.grokkingandroid.com/statelistdrawables-for-recyclerview-selection/

# Imported libraries

[LollipopContactsRecyclerViewFastScroller](https://github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller)<br/>
Improved and adapted to work in conjunction with `FlexibleAnimatorAdapter`.

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
