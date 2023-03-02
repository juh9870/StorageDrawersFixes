### This mod is in a beta state, and some issues may arise while using it, so please report all issues you encounter.

Storage Drawers have a bunch of issues with various modded automation options like pipes. The root cause of all of that is poorly implemented "virtual slot" that caused confusion to other mods.

**Storage Drawers Fixes** attempts to fix those issues by completely removing that virtual slot.

# If you encounter any other Storage Drawers-related issues, please DO NOT REPORT THEM TO THE ORIGINAL DEVELOPERS before checking if those issues remain after removal of my mod

## Performance considerations:
The virtual slot was introduced to address performance problems when inserting large numbers of items into a controller. However, even in megabases, lag issues can occur well before that point. From my testing, removing the virtual slot did not noticeably worsen performance when inserting around 1000 items per tick into a 4096-drawer controller via Refined Storage. Real-world use cases would typically involve lower insertion rates. Additionally, the current issues with Storage Drawers caused by the virtual slot make it difficult to use them on a large scale (or even in smaller builds) without encountering problems. Removing the virtual slot would therefore benefit most players.

## Under the hood:
Virtual slot mainly brings performance benefit upon item insertion, but those are almost negligible due to the fact that under the hood implementation still iterates over all slots, trying to find one suitable for insertion. Removal of a virtual slot leverages that action to the calling machine, only introducing a thin overhead of calling `IItemHandler` methods more frequently (which most of the automation mods do anyway regardless of virtual slot presence)
