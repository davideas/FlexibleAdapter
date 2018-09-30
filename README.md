[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FlexibleAdapter-green.svg?style=flat)](http://android-arsenal.com/details/1/2207)
[![Download](https://api.bintray.com/packages/davideas/maven/flexible-adapter/images/download.svg)](https://bintray.com/davideas/maven/flexible-adapter/_latestVersion)
[![API](https://img.shields.io/badge/API-14%2B-green.svg?style=flat)](https://android-arsenal.com/api?level=14)
[![Licence](https://img.shields.io/badge/Licence-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
![Methods and Size](https://img.shields.io/badge/Methods%20and%20size-Core:%20759,%20124%20KB%20|%20UI:%20267,%2068%20KB-e91e63.svg)

# FlexibleAdapter
- [v5.1.0](https://github.com/davideas/FlexibleAdapter/releases/tag/5.1.0) built on 2018.09.30 with AndroidX
- If you come from previous versions, update your code following the Wiki page [Migrations](https://github.com/davideas/FlexibleAdapter/wiki/Migrations).
- Please read also [issues](https://github.com/davideas/FlexibleAdapter/issues) and [releases](https://github.com/davideas/FlexibleAdapter/releases).

### One Adapter many Apps
> :mega: When initially Android:registered: team introduced the RecyclerView widget, we had to implement a custom Adapter in several applications, again and again to provide the items for our views.<br/>
We didn't know how to add selection and to combine all the use cases in the same Adapter.
Since I created this library, it has become easy to configure multiple views and now, nobody wants to use a ListView anymore.

The idea behind is to regroup multiple features in a unique library, without the need to customize and import several third libraries not compatible among them.

The FlexibleAdapter helps developers to simplify this process without worrying too much about the Adapter anymore. It's easy to use, it has predefined logic for different situations and prevents common mistakes.<br/>
This library is configurable and it guides the developers to create a better user experience and now, even more with the new features.

### Features in main library
* Simple, Single & Multi selection mode.
* Mapping multi-view types with [Item interfaces](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Item-Interfaces).
* Predefined [ViewHolders](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-ViewHolders) with (child) click listeners and others callbacks.
* Async [Updates](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Update-Data-Set) with optional _DiffUtil_ for small lists.
* Async [Filter](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Search-Filter) with spannable text (:eyeglasses:); Result list is animated; With optional original list; Works with sub items; Multi filter.
* [High performance](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Search-Filter#performance-result-when-animations-are-active) filter and updates for medium and big lists.
* [Headers and Sections](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Headers-and-Sections) with sticky behaviour fully clickable and collapsible, elevation, transparency and automatic linkage on item move!
* [Scrollable Headers and Footers](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Scrollable-Headers-and-Footers) items that lay respectively at the top and at the bottom of the main items.
* [Expandable items](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Expandable-items) with _Selection Coherence_ and multi-level expansion.
* [Drag&Drop and Swipe-To-Dismiss](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Drag&Drop-and-Swipe#swiping-the-front-view) with Leave-Behind pattern and with _Selection Coherence_.
* Innovative bottom and top [EndlessScroll](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-On-Load-More) (_No OnScrollListener_).
* Customizable [FastScroller](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-FastScroller) with several features.
* Customizable [Scrolling Animations](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Adapter-Animations) based on adapter position and beyond.
* Customizable [Animations](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Adapter-Animations#item-animations-when-items-are-notified) when adding and removing items.
* [LayoutUtils](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Utils) for orientation, span count and visible items calculation.
* Support for any [thirds LayoutManagers](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Thirds-Layout-Managers).
* Easy runtime position calculation for adding/moving items in sections.
* Custom Tags for multiple adapter instances that ease our debug.
* Comprehensive [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) pages and JavaDoc documentation.

### UI extension library :eyeglasses:
* Faster setup selection with [ActionModeHelper](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-ActionModeHelper).
* Faster setup for item restoration with [UndoHelper](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-UndoHelper); Works with expandable and swiped items too!
* Basic empty view handling with [EmptyViewHelper](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-EmptyViewHelper).
* [Advanced item decoration](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Flexible-Item-Decoration) (customizable dividers, sections gap and item offsets).
* 3 layout managers that support smooth scrolling.
* [FlexibleUtils](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Utils) for text highlighting, accent color fetching, API versions.
* [DrawableUtils](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Utils) for dynamic backgrounds with ripple effect (_No XML_).

### Others _experimental_ extension libraries
* [Live Data](https://github.com/davideas/FlexibleAdapter/wiki/Extensions-%7C-LiveData).
* Data Binding.

# Setup
#### build.gradle
```
repositories {
    jcenter()
}
```
```
dependencies {
    // Using JCenter
    implementation 'eu.davidea:flexible-adapter:5.1.0'
    implementation 'eu.davidea:flexible-adapter-ui:1.0.0'
    implementation 'eu.davidea:flexible-adapter-livedata:1.0.0-b3'
    // From 1.0.0-b3, library is compiled with data binding v2
    implementation 'eu.davidea:flexible-adapter-databinding:1.0.0'
}
```
#### Stay Updated
|Flexible Adapter|UI|Live Data|Data Binding|
|---|---|---|---|
|<div align="center">5.1.0</div>|<div align="center">1.0.0</div>|<div align="center">1.0.0-b3</div>|<div align="center">1.0.0</div>|
|<a href='https://bintray.com/davideas/maven/flexible-adapter?source=watch' alt='Get automatic notifications about new "flexible-adapter" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_color.png'></a>|<a href='https://bintray.com/davideas/maven/flexible-adapter-ui?source=watch' alt='Get automatic notifications about new "flexible-adapter-ui" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_bw.png'></a>|<a href='https://bintray.com/davideas/maven/flexible-adapter-livedata?source=watch' alt='Get automatic notifications about new "flexible-adapter-livedata" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_bw.png'></a>|<a href='https://bintray.com/davideas/maven/flexible-adapter-databinding?source=watch' alt='Get automatic notifications about new "flexible-adapter-databinding" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_bw.png'></a>|

# Wiki!
I strongly recommend to read the **[Wiki](https://github.com/davideas/FlexibleAdapter/wiki) pages**, where you can find a comprehensive Tutorial.<br/>

### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!<br/>
Active discussions:
- [The next steps of development: New demoApp > Extensions](https://github.com/davideas/FlexibleAdapter/issues/361).

### Under the hood
Some simple features have been implemented, thanks to some Blogs (see at the bottom of the page), merged and methods have been improved for speed and scalability.

<p align="center"><img src="./screenshots/wiki_diagram.png"></p>

* At lower level there is `SelectableAdapter` class. It provides selection features and it's able to _maintain the state_ after the rotation: you just need to call the onSave/onRestore methods from the Activity!
* At middle level, the `AnimatorAdapter` class has been added to give some animation at startup and when user scrolls.
* At front level, the core class `FlexibleAdapter`. It holds and handles the main list, performs actions on all different types of item paying attention at the adding and removal of the items, as well as the new concept of "selection coherence".
* Useful extensions and helpers have been added during the time to simplify the development.
* Item interfaces and predefined ViewHolders complete the whole library giving more actions to the items and configuration options to developers.

# Showcase of the demo App
You can [download](https://github.com/davideas/FlexibleAdapter/releases) the latest demo App from the latest release page OR run it with the emulator.<br>
This [Wiki page](https://github.com/davideas/FlexibleAdapter/wiki/5.x-%7C-Demo-App) will give you a short briefing of the demo App.

![Overall](/screenshots/demo20_overall.png)
![Adapter Animations](/screenshots/demo20_adapter_animations.png)
![Undo](/screenshots/demo20_undo_single_selection.png)
![Staggered Layout](/screenshots/demo20_dynamic_staggered_layout.png)

![Endless](/screenshots/demo20_endless.png)
![Instagram](/screenshots/demo20_instagram.png)
![Expandables](/screenshots/demo20_expandables.png)
![Multi Selection & SC](/screenshots/demo20_selection_coherence.png)

![Drag & Drop](/screenshots/demo20_drag_linear.png)
![swipe-to-dismiss1](/screenshots/demo20_swipe-to-dismiss1.png)
![swipe-to-dismiss2](/screenshots/demo20_swipe-to-dismiss2.png)
![ViewPager](/screenshots/demo20_view_pager.png)

![Async Filter](/screenshots/demo20_async_filter.png)
![Search Filter](/screenshots/demo20_search_filter.png)
![Search Sections](/screenshots/demo20_search_sections.png)

# Change Log
###### Latest release
[v5.1.0](https://github.com/davideas/FlexibleAdapter/releases/tag/5.1.0) - 2018.09.30

###### Old releases
[v5.0.6](https://github.com/davideas/FlexibleAdapter/releases/tag/5.0.6) - 2018.09.16 |
[v5.0.5](https://github.com/davideas/FlexibleAdapter/releases/tag/5.0.5) - 2018.06.03 |
[v5.0.4](https://github.com/davideas/FlexibleAdapter/releases/tag/5.0.4) - 2018.05.01 |
[v5.0.3](https://github.com/davideas/FlexibleAdapter/releases/tag/5.0.3) - 2018.03.23 |
[v5.0.2](https://github.com/davideas/FlexibleAdapter/releases/tag/5.0.2) - 2018.03.17 |
[v5.0.1](https://github.com/davideas/FlexibleAdapter/releases/tag/5.0.1) - 2018.03.11 |
[v5.0.0](https://github.com/davideas/FlexibleAdapter/releases/tag/5.0.0) - 2018.03.04

# Limitations
Item half swipe cannot be implemented due to how the `android.support.v7.widget.helper.ItemTouchHelper` is done, also clicks on rear view are not possible, same reason.
Half swipe can be done with others means, please see issues #98 and #100. See also commits of Apr 25, 2016. 

# Thanks
###### Inspired by
- http://enoent.fr/blog/2015/01/18/recyclerview-basics/
- https://www.grokkingandroid.com/statelistdrawables-for-recyclerview-selection/

###### Special thanks goes to
- Martin Guillon ([Akylas](https://github.com/Akylas)) to have contributed at the development of the new technique for the Sticky Header.
- [Arpinca](https://github.com/arpinca) who added new features for FastScroller like _autoHide_ and _ignoreTouchesOutsideHandle_ and more.

###### Donations
This library is currently free to use and modify, if you would like to say _Thank You_ by donating any amount, you're very welcome! Here the link to PayPal.me:

[![PayPal.me](https://www.paypalobjects.com/webstatic/i/sparta/logo/logo_paypal_106x29.png)](https://www.paypal.me/davideas)

# Imported libraries
- The library [LollipopContactsRecyclerViewFastScroller](https://github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller) has been imported, heavily improved and adapted to work in conjunction with `AnimatorAdapter`.
- The library [sticky-headers-recyclerview](https://github.com/timehop/sticky-headers-recyclerview) was initially imported, then it was removed in favor of the new technique able to manage a real _View_ and so to handle the click events properly.
- The item spacing technique comes from the library [CommonItemDecoration](https://github.com/ibosong/CommonItemDecoration) and it has been improved with new features.

# Apps that use this Adapter
It will be a pleasure to add your App here, once it is published.

[Module.org](https://play.google.com/store/apps/details?id=org.module.app) |
[Neuronizer](https://play.google.com/store/apps/details?id=de.djuelg.neuronizer) |
[Nextcloud Talk](https://play.google.com/store/apps/details?id=com.nextcloud.talk2) |
[Socio - Shake and Connect!](https://play.google.com/store/apps/details?id=com.atsocio.socio) |
[Shibagram](https://play.google.com/store/apps/details?id=com.apripachkin.shibagram) |
[BNVR Client](https://play.google.com/store/apps/details?id=ru.beward.bnvr)

# License

#### FlexibleAdapter & Extensions

    Copyright 2015-2018 Davide Steduto, Davidea Solutions Sprl

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

#### FastScroller

    Copyright 2016-2017 AndroidDeveloperLB, Davide Steduto & Arpinca

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
