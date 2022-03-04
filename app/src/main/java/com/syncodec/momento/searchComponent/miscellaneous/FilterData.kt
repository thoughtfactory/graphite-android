package com.syncodec.momento.searchComponent.miscellaneous

fun filterData(
	showArchived: Boolean,
	isArchived: Boolean,
	showFavourite: Boolean,
	isFavourite: Boolean,
	showLocked: Boolean,
	isLocked: Boolean,
): Boolean {
	return when {
		showArchived and showFavourite and showLocked -> isArchived and isFavourite and isLocked
		showArchived and showFavourite and !showLocked -> isArchived and isFavourite and !isLocked
		showArchived and !showFavourite and showLocked -> isArchived and !isFavourite and isLocked
		showArchived and !showFavourite and !showLocked -> isArchived and !isFavourite and !isLocked
		!showArchived and showFavourite and showLocked -> !isArchived and isFavourite and isLocked
		!showArchived and showFavourite and !showLocked -> !isArchived and isFavourite and !isLocked
		!showArchived and !showFavourite and showLocked -> !isArchived and !isFavourite and isLocked
		!showArchived and !showFavourite and !showLocked -> !isLocked
		else -> false
	}
}
