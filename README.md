[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FlexibleAdapter-green.svg?style=flat)](https://android-arsenal.com/details/1/2207) [![Download](https://api.bintray.com/packages/davideas/maven/flexible-adapter/images/download.svg) ](https://bintray.com/davideas/maven/flexible-adapter/_latestVersion)

# FlexibleAdapter

###### A pattern for every RecyclerView - Master branch: v4.1 of 2015.11.29 - Dev branch: v4.2

####ANNOUNCEMENT: Important changes are foreseen in v4.2.0 and in v5.0.0. Please see [issues](https://github.com/davideas/FlexibleAdapter/issues) and [releases](https://github.com/davideas/FlexibleAdapter/releases).

#### Main functionalities
* Base item selection (but also SINGLE & MULTI selection mode) in the Recycler View with ripple effect.
* Undo the deleted items with custom animations.
* Customizable FastScroller (imported library, see change log for details).
* SearchFilter with string selection in Item titles and subtitles.

#### How is made
The base functionality is taken from different Blogs (see at the bottom of the page), merged and methods have been improved for speed and scalability, for all Activities that use a RecyclerView.

* At lower class there is SelectableAdapter that provides selection functionalities and it's able to _maintain the state_ after the rotation, you just need to call the onSave/onRestore methods from the Activity!
* Then, the class FlexibleAdapter handles the content paying attention at the animations (calling notify only for the position. _Note:_ you still need to set your animation to the RecyclerView when you create it in the Activity).
* Then you need to extend over again this class. Here you add and implement methods as you wish for your own ViewHolder and your Domain/Model class (data holder).

I've put the Set click listeners at the creation and not in the Binding method, because onBindViewHolder is called at each invalidate (each notify..() methods).

Finally note that, this adapter handles the basic clicks: _single_ and _long clicks_. If you need a double tap you need to implement the android.view.GestureDetector.

# Screenshots
![Main screen](/screenshots/main_screen.png) ![Multi Selection](/screenshots/multi_selection.png)
![Search screen](/screenshots/search.png) ![Undo Screen](/screenshots/undo.png)

#Setup
Using JCenter
```
dependencies {
	compile 'eu.davidea:flexible-adapter:4.1.0'
}
```
Using bintray.com
```
repositories {
	maven { url "http://dl.bintray.com/davideas/maven" }
}
dependencies {
	compile 'eu.davidea:flexible-adapter:4.1.0@aar'
}
```
Or you can just *copy* SelectableAdapter.java & FlexibleAdapter.java in your *common* package and start to *extend* FlexibleAdapter from your custom Adapter (see my ExampleAdapter).

Remember to initialize `mItems` (List already included in FlexibleAdapter) in order to manage list items. Method `updateDataSet(..)` can help in this.
#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!

# Usage for Single Selection
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details! In your Activity/Fragment creation set the Mode SINGLE.
In onListItemClick, call *toggleSeletion* to register the selection on that position:
``` java
public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		...
		mAdapter = new YourAdapterClass(..., ..., ...);
		mAdapter.setMode(YourAdapterClass.MODE_SINGLE);
		...
	}

	@Override
	public boolean onListItemClick(int position) {
		toggleSelection(position);
	}
}
```

# Usage for Multi Selection
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details! In your Activity/Fragment change the Modes for the _ActionMode_ object.
``` java
public class MainActivity extends AppCompatActivity implements
		ActionMode.Callback {
	...
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.menu_context, menu);
		mAdapter.setMode(YourAdapterClass.MODE_MULTI);
		return true;
	}
	
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mAdapter.setMode(YourAdapterClass.MODE_SINGLE);
		mAdapter.clearSelection();
		mActionMode = null;
	}
}
```

# Usage for Undo
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details!
``` java
@Override
public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	switch (item.getItemId()) {
		...
		case R.id.action_delete:
			//Keep synchronized the Adapter: Remove selected items from Adapter
			mAdapter.removeItems(mAdapter.getSelectedItems());

			//Start countdown with startUndoTimer(millisec)
			mAdapter.startUndoTimer(7000); //Default 5''
			mActionMode.finish();
			return true;
	}
}

@Override
public void onDeleteConfirmed() {
	for (Item item : mAdapter.getDeletedItems()) {
		//Remove items from your Database. Example:
		DatabaseService.getInstance().removeItem(item);
	}
}
```

# Usage for FastScroller
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details! First add the drawable files to the project, then the layout, finally add the implementation for the Adapter and Activity/Fragment:
``` java
public class YourAdapterClass extends FlexibleAdapter<ExampleAdapter.SimpleViewHolder, Item>
		implements FastScroller.BubbleTextGetter {
	...
	@Override
	public String getTextToShowInBubble(int position) {
		return getItem(position).getTitle().substring(0,1).toUpperCase();
	}
}
```
``` java
public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		...
		FastScroller fastScroller = (FastScroller) findViewById(R.id.fast_scroller);
		fastScroller.setRecyclerView(mRecyclerView);
		fastScroller.setViewsToUse(R.layout.fast_scroller, R.id.fast_scroller_bubble, R.id.fast_scroller_handle);
	}
	...
}
```
# Usage for the Filter
See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details! In _YourAdapterClass.updateDataSet()_, call _filterItems()_;
``` java
public class YourAdapterClass extends FlexibleAdapter<ExampleAdapter.SimpleViewHolder, Item> {
	...
	@Override
	public void updateDataSet(String param) {
		//Fill and Filter mItems with your custom list
		filterItems(DatabaseService.getInstance().getListById(param));
	}
}
```

# Change Log
###### v4.1.0 - 2015.11.29
- Improved **Undo** functionality: added new callback _onDeleteConfirmed_ in OnUpdateListener. See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details! 
- Improved **Filter** functionality: added new intelligent function _filterItems_. See [Wiki](https://github.com/davideas/FlexibleAdapter/wiki) for full details!
- Logs are now in verbose level.
- Adapted example App accordingly.

###### v4.0.1 - 2015.11.01
- Refactored module names, package signatures and gradle build files. Code remains unchanged.
- Configuration for JCenter, now FlexibleAdapter is a lightweight standalone library!
  **Note:** FastScroller and ItemAnimators are excluded from the library, but you can see them in the example App.
- New icon.

###### v4.0 - 2015.10.18
- Added **FilterAsyncTask** to asynchronously load the list (This experimental and might not work well and binding is excluded from Async).
- Enabled **Filter** through _updateDataSet_ method (Note: as the example is made, the search regenerate the list!).
- Included some ItemAnimators from https://github.com/wasabeef/recyclerview-animators
  (in the example SlideInRightAnimator is used), but all need to be adapted if used with Support v23.1.0.
- Included and customized **FastScroller** library from https://github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller
  My version has FrameLayout extended instead of LinearLayout, and a friendly layout.
- Added **SwipeRefreshLayout**, just an usage example.
- Use of Handler instead of TimerTask while Undo.
- _FlexibleAdapter_ can now return deleted items before they are removed from memory.
- _SelectableAdapter.selectAll()_ can now skip selection on one specific ViewType.
- Adapted MainActivity.

###### Old releases
See [releases](https://github.com/davideas/FlexibleAdapter/releases) for old versions.

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
