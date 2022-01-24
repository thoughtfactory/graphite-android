package com.syncodec.momento.bucketComponent.screen

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.EditActivity
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import compose.icons.TablerIcons
import compose.icons.tablericons.CircleDotted
import compose.icons.tablericons.Plus

data class BucketBook(val title: String, val path: String, val isLocked: Boolean = false)

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun BooksScreen() {

	Log.i("npr71", "books screen...")

	LazyVerticalGrid(
		cells = GridCells.Adaptive(96.dp),
		modifier = Modifier
			.padding(8.dp)
	) {
		item {
			AddBookItem(
				modifier = Modifier
					.aspectRatio(0.75f)
					.clickable {  }
			)
		}
		items(17) {
			BookItem(
				modifier = Modifier
					.aspectRatio(0.75f)
			)
		}
	}

}

@Composable
private fun NoNotebookCard(
	modifier: Modifier = Modifier,
	openSheet: (BottomSheetType) -> Unit
) {
	Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.47f),
		modifier = modifier
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(12.dp),
		) {
			Image(
				painter = painterResource(id = R.drawable.il_book),
				contentDescription = null,
				modifier = Modifier
					.requiredSize(192.dp)
			)

			OutlinedButton(
				onClick = { openSheet(BottomSheetType.NotebookBottomSheet) },
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer
				)
			) {
				Icon(
					imageVector = TablerIcons.Plus,
					contentDescription = "Add new notebook",
					tint = MaterialTheme.colorScheme.primary
				)

				Spacer(modifier = Modifier.width(16.dp))

				androidx.compose.material3.Text(
					text = "Add your first notebook",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.primary
				)
			}
		}
	}

}


@Composable
private fun AddBookItem(
	modifier: Modifier
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp),
	) {
		Card(
			elevation = 12.dp,
			modifier = modifier,
		) {
			Icon(
				imageVector = TablerIcons.CircleDotted,
				contentDescription = null,
				tint = Color.LightGray,
				modifier = Modifier
					.requiredSize(40.dp)
			)
			Icon(
				imageVector = TablerIcons.Plus,
				contentDescription = null,
				tint = Color.LightGray,
				modifier = Modifier
					.requiredSize(20.dp)
			)
		}

		Text(
			text = "A new book?",
			style =  MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BookItem(
	modifier: Modifier
) {
	val context = LocalContext.current
	val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp),
	) {
		Card(
			elevation = 12.dp,
			onClick = {
				Intent(context, EditActivity::class.java).apply {
					val bookDataString = "{\"key\":\"/works/OL21667536W\",\"title\":\"How to Avoid a Climate Disaster\",\"cover_i\":10656063,\"author_name\":[\"Bill Gates\"]}"
					putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.BOOKS)
					putExtra(Konstant.Companion.Konstant.BOOK_DATA.name, bookDataString)
					context.startActivity(this)
				}
			}
		) {
			Image(
				painter = painterResource(id = R.drawable.home_background),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = modifier,
			)
		}

		Text(
			text = "Book",
			style =  MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}
