[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FlexibleAdapter-green.svg?style=flat)](https://android-arsenal.com/details/1/2207) [![Download](https://api.bintray.com/packages/davideas/maven/flexible-adapter/images/download.svg) ](https://bintray.com/davideas/maven/flexible-adapter/_latestVersion) [![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

# FlexibleAdapter

###### A pattern for every RecyclerView - Stable _light_ version v4.2 of 2015.12.16 - NEW! dev branch in beta: v5.0.0-b5 (usable library!)

####ANNOUNCEMENT: Important and Revolutionary changes are foreseen in v5.0.0. Please see [issues](https://github.com/davideas/FlexibleAdapter/issues) and [releases](https://github.com/davideas/FlexibleAdapter/releases).

> When initially Android introduced the RecyclerView widget, we had to implement the Adapter in several applications, again and again to provide the items for our views. Since I created this library, it has become easy to configure how views will be displayed in a list. Thanks to a library like this, nobody wants to use a ListView anymore.

The FlexibleAdapter helps developers to simplify this process without to worry about the adapter anymore. It's easy to extends, it has predefined logic for different situations and prevents common mistakes.
This library is configurable and it guides the developers (thanks to quality comments on the methods) to create a better user experience and now, even more with the new ViewHolders and new actions.

#### Main functionalities (New features might still change)
* Simple item selection with ripple effect, Single & Multi selection mode.
* Restore deleted items (undo delete), **NEW** works with Expandable items too!
* FastScroller, **NEW** now in the library and with GridLayout support.
* Customizable ItemDecoration.
* SearchFilter with Spannable text, **NEW** now items are animated. Works with sub items too!
* Add and Remove items with custom animations.
* **NEW!** Adapter Animations with custom configuration based on adapter position and beyond.
* **NEW!** Predefined ViewHolders.
* **NEW!** Expandable items with <u>selection coherence</u>, multi-level expansion.
* **NEW!** Drag&Drop and Swipe actions with <u>selection coherence</u>.
* **NEW!** Headers/Sections with sticky behaviour fully clickable, collapsible, automatic linkage!
* **NEW!** Auto mapping ViewTypes with Item interfaces.
* **NEW!** 1 simple constructor for all events.

#### How is made
Some simple functionalities have been implemented thanks to the some Blogs (see at the bottom of the page), merged and methods have been improved for speed and scalability, for all Activities that use a RecyclerView.

* At lower level there is `SelectableAdapter` class. It provides selection functionalities and it's able to _maintain the state_ after the rotation: you just need to call the onSave/onRestore methods from the Activity!
* At middle level, the `FlexibleAnimatorAdapter` class has been added to give some animation at startup and when user scrolls.
* At front level, the core class `FlexibleAdapter`. It holds and handles the main list, performs actions on all different types of item paying attention at the adding and removal of the items, as well as the new concept of "selection coherence".
* Item interfaces and predefined ViewHolders complete the whole library giving more actions to the items and configuration options to the developers and the end user.

# Screenshots
![GridView & Secondary Functionalities](/screenshots/gridview_secondary_functionalities.png)
![Search screen](/screenshots/search.png)
![Undo Screen](/screenshots/undo.png)
![Adapter Animations](/screenshots/adapter_animations.png)
![StickyHeaders & Header Linkage & Restore items](/screenshots/stickyheaders_headerlinkage_restoreitems.png)
![StickyHeaders & Expandable items](/screenshots/stickyheaders_expandableitems.png)
![Multi Selection](/screenshots/multi_selection.png)

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
	compile 'eu.davidea:flexible-adapter:5.0.0-b5@aar'
	
	//Using JCenter
	compile 'eu.davidea:flexible-adapter:4.2.0'
	compile 'eu.davidea:flexible-adapter:5.0.0-b5'
	
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
###### v5.0.0-b5 - 2016.04.04
_Refactor_
- Constructor of `AbstractSectionableItem` now accepts the `IHeader` interface.
- `setHeader(header)` in `ISectionable` interface is changed to **void** method.
- Renamed method `Utils.setHighlightText()` to `Utils.highlightText()`.
- `StickyHeaderDecoration` is now deprecated, as consequence `enableStickyHeaders(maxCachedHeaders)` is also deprecated: use `enableStickyHeaders()`.

_Improvements_
- Headers are clickable when sticky! Thanks to @farfromrefug. StickyHeaders can now benefit of the existing click listeners in `FlexibleViewHolder` [See #41]. Pay attention at the method `getStickySectionHeadersHolder()`.
- `IHeader` item can be combined with `IExpandable` item to make a section expandable with header.
- Adding items with new headers, headers are also shown.
- Added `AbstractExpandableHeaderItem`. 
- Added a new Wiki page about how to implement sections with sticky headers and expandable sections. Direct link: [Headers and Sections](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Headers-and-Sections).
- Added new methods _getItemCountOfTypesUntil(), removeItemsOfType(), hasNewSearchText()_.
- Multiple code improvements with **SonarQube** tool, thanks to @georgekankava.

_Fixes_
- Fixed NPE in `isEnabled(position)` when position is unknown.
- Fixed header linkage when deleting/restore items with header. _It's important now that all sectionable items must initialize the own header at startup_.
- Bug fixing on SearchView and FilterMethods.
- Fixed view activation elevation [See #38].
- Fixed adapter item animation at startup.

_Demo App_
- New example app supporting multiple fragments [See #19].
- Added an example of `FlexibleItemHolder` to use simultaneously the same _modelData_ object in multiple RecyclerView/Adapters.
- Adapted code for Expandable Sections with headers. Enabled demo option for `FragmentExpandableSections`.
- Demo app has more clear DatabaseService. It shows how to create item interfaces.
- FlipView animation for Fragments.
- Unfortunately the demo app has a _known_ bug on FastScroller when changing Fragment, but FastScroller is planned to be changed.

###### v5.0.0-b4 - 2016.02.21
- Added **Sticky Header** functionality [See #32].
- _IHeader_ interface has been added to identify the fact the item is a Header item, as consequence, _ISectionable_ interface now has to be assigned to the items(!!) in order for them to hold the reference to the _IHeader_ item.
- Headers work in combination with restore items, in all situations, they can be dragged, swapped and receive the new swapped item. Support for **Orphan Headers** has been added.
- **New clean way to filter items**. Added _IFilterable_ item interface: Items can now implement _filter()_ method in order to be collected for the filtered list. 
- Brand new logic, how a removed item saves restoration info, now it never misses a restore. 
- Added a good _DividerItemDecorator_ into library [See #33].
- Added support for _isSelectable()_, _isEnabled()_.
- Added methods _addSubItems()_ and _addAllSubItemsFrom()_.
- Code optimization, nice to mention: removed unnecessary 8 casts; better use of Payload when _notifyItemChanged_ is triggered; Much faster restoration with big numbers (~1000)

###### Old releases
See [releases](https://github.com/davideas/FlexibleAdapter/releases) for old versions.

v5.0.0-b3 - 2016.02.08 |
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
