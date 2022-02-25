package com.syncodec.momento.custom.entry


//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@ExperimentalMaterialApi
//@ExperimentalPagerApi
//@Composable
//fun EntryScreen() {
//	val viewModel: MainViewModel = viewModel()
//
//	val showArchived by viewModel.mainActivityState.showArchived
//	val showFavourite by viewModel.mainActivityState.showFavourite
//	val showTrash by viewModel.mainActivityState.showTrash
//	var isSelected by viewModel.mainActivityState.isSelected
//
//	val diaryList by viewModel.diaryRepository.diaryDbEntryListLiveData.observeAsState()
//	val isDiaryEmpty: Boolean = diaryList?.isEmpty() ?: true
//
//	val diaryDbEntryDayMap: MutableMap<Long, MutableList<DiaryDbEntry>> = mutableMapOf()
//
//	val selectedEntryList = viewModel.mainActivityState.selectedEntryList
//
//	val calendar = Calendar.getInstance()
//	diaryList
//		?.filter {
//			if (showArchived && showFavourite && showTrash) {
//				it.isArchived && it.isFavourite && it.deletedTimestamp!=-1L
//			} else if (showArchived && showFavourite) {
//				it.isArchived && it.isFavourite && it.deletedTimestamp == -1L
//			} else if (showArchived && showTrash) {
//				it.isArchived && it.deletedTimestamp!=-1L
//			} else if(showFavourite && showTrash) {
//				it.isFavourite && it.deletedTimestamp!=-1L
//			} else if(showArchived) {
//				it.isArchived && it.deletedTimestamp == -1L
//			} else if(showFavourite) {
//				it.isFavourite && it.deletedTimestamp == -1L
//			} else if(showTrash) {
//				it.deletedTimestamp != -1L
//			} else {
//				it.deletedTimestamp == -1L
//			}
//		}
//		?.forEach { diary ->
//			calendar.apply {
//				timeInMillis = diary.userTimestamp
//				set(Calendar.MILLISECOND, 0)
//				set(Calendar.SECOND, 0)
//				set(Calendar.MINUTE, 0)
//				set(Calendar.HOUR, 0)
//			}
//			if (diaryDbEntryDayMap.containsKey(calendar.timeInMillis)) {
//				diaryDbEntryDayMap[calendar.timeInMillis]!!.add(diary)
//			} else {
//				diaryDbEntryDayMap[calendar.timeInMillis] = mutableListOf(diary)
//			}
//		}
//
//	if (isDiaryEmpty) {
//		NoDiaryCard(!(showArchived || showFavourite || showTrash))
//	} else {
//		LazyColumn(
//			modifier = Modifier
//		) {
//			if (!(showArchived || showFavourite || showTrash)) {
//				item {
//					QuoteCard()
//					Spacer(modifier = Modifier.height(16.dp))
//				}
//			}
//
//			diaryDbEntryDayMap.forEach { (day, diaryList) ->
//				stickyHeader {
//					DayHeaderCard(
//						title = timeStampToPrettyDay(day),
//						noEntries = diaryList.size
//					)
//				}
//
//				diaryList.forEachIndexed { index, diaryDbEntry ->
//					item {
//						val tint = MaterialTheme.colorScheme.secondaryContainer
//
//						DiaryCard(
//							diaryDbEntry = diaryDbEntry,
//							isLast = index == diaryList.size - 1,
//							isSelected = diaryDbEntry.primaryKey in selectedEntryList,
//							tint = tint,
//							onClick = {
//								if (isSelected) {
//									isSelected = true
//									if (diaryDbEntry.primaryKey in selectedEntryList) {
//										selectedEntryList.remove(diaryDbEntry.primaryKey)
//									} else {
//										selectedEntryList.add(diaryDbEntry.primaryKey)
//									}
//								} else {
//								}
//							}
//						) {
//							isSelected = true
//							selectedEntryList.add(diaryDbEntry.primaryKey)
//						}
//						if (index != diaryList.size - 1) {
//							DiaryDaySpacer(tint = tint)
//						}
//					}
//				}
//			}
//
//			item {
//				Spacer(modifier = Modifier.height(128.dp))
//			}
//		}
//	}
//}
