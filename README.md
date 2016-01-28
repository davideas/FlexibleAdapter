[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FlexibleAdapter-green.svg?style=flat)](https://android-arsenal.com/details/1/2207) [![Download](https://api.bintray.com/packages/davideas/maven/flexible-adapter/images/download.svg) ](https://bintray.com/davideas/maven/flexible-adapter/_latestVersion)

# FlexibleAdapter

###### A pattern for every RecyclerView - Stable version v4.2 of 2015.12.16 - Beta on dev branch: v5.0.0-b2

####ANNOUNCEMENT: Important changes are foreseen in v5.0.0. Please see [issues](https://github.com/davideas/FlexibleAdapter/issues) and [releases](https://github.com/davideas/FlexibleAdapter/releases).

#### Main functionalities
* Base item selection with ripple effect.
* SINGLE & MULTI selection mode, **NEW** now with FlexibleViewHolder.
* Restore deleted items (undo delete).
* Customizable FastScroller, **NEW** now in the library
* SearchFilter with string selection in Item titles and any subtext.
* Add and Remove items with custom animations.
* **NEW!** Predefined ViewHolders 
* **NEW!** Adapter Animations with custom configuration based on adapter position.
* **NEW!** Expandable item with selection coherence.
* **NEW!** Drag&Drop and Swipe actions with selection coherence.
* **NEW!** 1 simple constructor for all events.

#### How is made
The base functionality is taken from different Blogs (see at the bottom of the page), merged and methods have been improved for speed and scalability, for all Activities that use a RecyclerView.

* At lower class there is SelectableAdapter that provides selection functionalities and it's able to _maintain the state_ after the rotation, you just need to call the onSave/onRestore methods from the Activity!
* Then, the class FlexibleAdapter handles the content paying attention at the adding and removal item animations (calling notify only for the position).
* Then you need to extend over again this class. Here you can add and implement methods as you wish for your own ViewHolder and your Domain/Model class (data holder).

I've put the Set click listeners at the creation and not in the Binding method, because onBindViewHolder is called at each invalidate (each notify..() methods).

Finally note that, this adapter handles the basic clicks: _single_ and _long clicks_. If you need a double tap you need to implement the android.view.GestureDetector.

# Screenshots
![Main screen](/screenshots/main_screen.png) ![Multi Selection](/screenshots/multi_selection.png)
![Search screen](/screenshots/search.png) ![Undo Screen](/screenshots/undo.png)

#Setup
Using JCenter
```
dependencies {
	compile 'eu.davidea:flexible-adapter:5.0.0-b1'
}
```
Using bintray.com
```
repositories {
	maven { url "http://dl.bintray.com/davideas/maven" }
}
dependencies {
	compile 'eu.davidea:flexible-adapter:5.0.0-b1@aar'
}
```
Or you can just *copy* SelectableAdapter.java & FlexibleAdapter.java in your *common* package and start to *extend* FlexibleAdapter from your custom Adapter (see my ExampleAdapter).

Remember to call `super(items)` or to initialize `mItems` (List already included in FlexibleAdapter) in order to manage list items.
#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!

### Usage for Single Selection
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details!
In your Activity/Fragment creation set the Mode SINGLE.
In onListItemClick, call *toggleSeletion* to register the selection on that position:
### Usage for Multi Selection
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details!
In your Activity/Fragment change the Modes for the _ActionMode_ object, set the Mode MULTI.
### Usage for Undo
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details!
### Usage for FastScroller
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details!
Use the internal layout and drawables or create custom files, finally add the implementation for the Adapter and Activity/Fragment:
### Usage for the Filter
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details!
First, call _YourAdapterClass.setSearchText()_ in the Activity, then in _YourAdapterClass.updateDataSet()_, call _filterItems()_;
### Usage for Adapter Animations
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details!
Implement your custom logic based on position with getAnimators(); Call animateView() at the end of onBindViewHolder();
``` java
public class YourAdapterClass extends FlexibleAnimatorAdapter<FlexibleViewHolder, Item> {
	...
	@Override
    public void onBindViewHolder(FlexibleViewHolder holder, final int position) {
    	//Bind the ViewHolder as usual
    	...
    	//Then call animateView which internally calls getAnimators()
    	animateView(holder.itemView, position, true/false);
    }

	@Override
    public List<Animator> getAnimators(View itemView, int position, boolean isSelected) {
    	List<Animator> animators = new ArrayList<Animator>();
    	//Implement your custom logic based on viewType - position - selection
    	//Use predefined animators or create new custom animators, add them to the local list
    }
}
```
### Usage for Expandable items
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details!

# Change Log
###### v5.0.0-b2 - 2016.01.30
- **Expandable items** with selection coherence [See #23].
- **Drag&Drop and Swipe** actions with selection coherence and ActionMode compatible [See #21].
- **Adapter Animations** with customization based on adapter position - viewType - selection [See #15].
- New concept of Item: added **IFlexibleItem** and **IExpandableItem** interfaces to implement around the domain object.
- Several new functions to handle all new situations.
- Animations while filtering [See #24].
- Simplified constructor [See #25].
- Added FastScroller in the library [See #20].
- Added support for Grid Layout also in the expandable version.
- Adapted example App accordingly.

###### v5.0.0-b1 - 2016.01.03
- Removed _FilterAsyncTask_ and all deprecated functions from _OnUpdateListener_ [See #18].
- _OnUpdateListener_ now contains only the new _onUpdateEmptyView()_ [See #17].
- Added **FlexibleViewHolder** [See #14].
- Added _enableLogs()_ to see internal logs at runtime.
- Adapted example App accordingly.

###### v4.2.0 - 2015.12.12
- Added _isEmpty()_.
- Added new constructor to initialize list items [See #12].
- Moved _onDeletedConfirmed()_ in a new listener to pass as parameter to the method _startUndoTimer()_ [See #10].
- Deprecated _OnUpdateListener_ and some relative functions.
- **Note:** _updateDataSetAsync()_ has been **deprecated** and will be removed soon from next major version. Use _updateDataSet()_
  instead. **FilterAsyncTask** will not be used anymore to load data at startup: there's no advantage to use an Asynchronous loading,
  usually the list is already loaded Asynchronously somewhere else.
- Removed _static_ declaration from the following methods: _hasSearchText(); getSearchText(); setSearchText(String searchText)_.
- Adapted example App accordingly.

###### Old releases
See [releases](https://github.com/davideas/FlexibleAdapter/releases) for old versions.

v4.1.0 - 2015.11.29 |
v4.0.1 - 2015.11.01 | v4.0 - 2015.10.18 |
v3.1 - 2015.08.18 | v3.0 - 2015.07.29 |
v2.2 - 2015.07.20 | v2.1 - 2015.07.03 |
v2.0 - 2015.06.19 | v1.0 - 2015.05.03

# Thanks
I've used these blogs as starting point:

http://enoent.fr/blog/2015/01/18/recyclerview-basics/

https://www.grokkingandroid.com/statelistdrawables-for-recyclerview-selection/

# License

    Copyright 2015 Davide Steduto

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
