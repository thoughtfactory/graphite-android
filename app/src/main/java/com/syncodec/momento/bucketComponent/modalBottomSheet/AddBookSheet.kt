package com.syncodec.momento.bucketComponent.modalBottomSheet

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketItemActivity
import com.syncodec.momento.bucketComponent.BucketViewModel
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.ClimateChangeMessage
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import compose.icons.TablerIcons
import compose.icons.tablericons.Notebook
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddBookSheet() {
	val context = LocalContext.current
	val viewModel: BucketViewModel = viewModel()

	val objectMapper = ObjectMapper().registerModule(KotlinModule())

	var bookNameText by rememberSaveable { mutableStateOf("") }
	var bookNameTextBackgroundColor by remember { mutableStateOf(Color(244, 244, 245)) }

	val baseUrl = "https://openlibrary.org/search.json?title="
	val endUrl = "&fields=key,title,author_name,cover_i,first_publish_year&limit=10&offset=0"
	val requestQueue = Volley.newRequestQueue(context)
	var tag: String = "tag"

	var booksData by remember { mutableStateOf(listOf<BookData>()) }

	var isSearching by remember { mutableStateOf(false) }
	var isSearchResultAvailable by remember { mutableStateOf(false) }

	val activity = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult()
	) {
		viewModel.openBucket()
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(Color.White),
	) {

		BottomSheetStrip()

		BottomSheetHeader(title = "Umm... What was that book", imageVector = TablerIcons.Notebook)

		Spacer(modifier = Modifier.height(8.dp))

		BasicTextField(
			value = bookNameText,
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Search
			),
			keyboardActions = KeyboardActions(
				onSearch = {
					isSearching = true

					requestQueue.cancelAll(tag)

					val requestUrl = "${baseUrl}${URLEncoder.encode(bookNameText, StandardCharsets.UTF_8.toString())}$endUrl"
					val stringRequest = StringRequest(
						Request.Method.GET,
						requestUrl,
						{ requestResult ->
							val jsonObject = JSONObject(requestResult)
							val docs = jsonObject.getJSONArray("docs")
							val length = docs.length()
							val bookDataList: MutableList<BookData> = mutableListOf()
							isSearching = false
							isSearchResultAvailable = true
							for (i in 0 until length) {
								val bookData = objectMapper.readValue<BookData>(docs.get(i).toString())
								bookDataList.add(bookData)
							}
							booksData = bookDataList
						},
						{
						}
					)

					tag = requestUrl
					stringRequest.tag = tag
					requestQueue.add(stringRequest)
				}
			),
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.background(bookNameTextBackgroundColor)
				.onFocusChanged {
					bookNameTextBackgroundColor = if (it.isFocused) Color.White else Color(244, 244, 245)
				},
			onValueChange = {
				bookNameText = it
			},
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			decorationBox = { innerTextField ->
				Card(
					backgroundColor = Color.Transparent,
					shape = RoundedCornerShape(2.dp),
					border = BorderStroke(1.dp, Color.LightGray),
					elevation = 0.dp
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(16.dp, 0.dp)
					) {
						Box {
							if (bookNameText.isEmpty()) {
								Text(
									"Search for title",
									style = MaterialTheme.typography.bodyMedium,
									color = Color.LightGray
								)
							}
							innerTextField()
						}
					}
				}
			}
		)

		Spacer(modifier = Modifier.height(8.dp))

		AnimatedVisibility(visible = !isSearching && !isSearchResultAvailable) {
			ClimateChangeMessage()
		}

		AnimatedVisibility(visible = isSearching) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxWidth()
					.height(256.dp),
			) {
				val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))

				LottieAnimation(
					composition = lottieComposition,
					iterations = LottieConstants.IterateForever,
					modifier = Modifier
						.requiredSize(64.dp)
				)
			}
		}

		AnimatedVisibility(visible = !isSearching && isSearchResultAvailable) {
			if (booksData.isEmpty()) {
				Column(
					modifier = Modifier
						.fillMaxWidth(0.8f)
				) {
					Image(
						painter = painterResource(id = R.drawable.il_result_unavailable_2),
						contentDescription = "No result found",
						modifier = Modifier
							.fillMaxWidth()
							.height(256.dp)
					)

					Spacer(modifier = Modifier.height(16.dp))

					Text(
						text = "Sorry, we can't find that",
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.secondary,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.fillMaxWidth()
					)
				}

			} else {
				LazyVerticalGrid(
					cells = GridCells.Adaptive(96.dp),
					modifier = Modifier
						.padding(8.dp)
				) {
					items(booksData) { bookData ->
						BookButton(
							bookData = bookData,
							modifier = Modifier
								.aspectRatio(0.75f)
						) {
							Intent(context, BucketItemActivity::class.java).apply {
								putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.Type.BOOKS.ordinal)
								putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, viewModel.bucketKey)
								putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name, objectMapper.writeValueAsString(bookData))
								activity.launch(this)
							}
						}
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BookButton(
	modifier: Modifier,
	bookData: BookData,
	onClick: () -> Unit
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp),
	) {
		Card(
			elevation = 12.dp,
			modifier = modifier,
			onClick = { onClick() }
		) {

			if (bookData.coverI != null) {
				Image(
					painter = rememberImagePainter(
						data = "https://covers.openlibrary.org/b/id/${bookData.coverI}-M.jpg",
					),
					contentDescription = null,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop
				)
			}
		}

		Text(
			text = bookData.title,
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)

		if (bookData.authorName != null) {
			var author = ""
			bookData.authorName.forEach { author += " $it" }
			Text(
				text = "~ $author",
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier
					.padding(0.dp, 4.dp, 0.dp, 0.dp)
			)
		}
	}

}

data class BookData(
	@JsonProperty("key")
	val key: String,

	@JsonProperty("title")
	val title: String,

	@JsonProperty("cover_i")
	val coverI: String?,

	@JsonProperty("author_name")
	val authorName: List<String>?,

	@JsonProperty("first_publish_year")
	val firstPublishYear: Int?
)
