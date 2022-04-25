package com.syncodec.momento.bucketComponent.modalBottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.syncodec.momento.custom.ClimateChangeMessage
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class SheetState {
	INIT,
	SEARCHING,
	RESULT_FOUND,
	RESULT_NOT_FOUND,
	ERROR
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
) : java.io.Serializable

@OptIn(ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun AddBookSheet(
	onAction: (BookData) -> Unit
) {
	val context = LocalContext.current

	val objectMapper = ObjectMapper().registerModule(KotlinModule())

	var bookNameText by rememberSaveable { mutableStateOf("") }
	var isBookNameTextFocused by remember { mutableStateOf(false) }

	var sheetState by remember { mutableStateOf(SheetState.INIT) }

	val baseUrl = "https://openlibrary.org/search.json?title="
	val endUrl = "&fields=key,title,author_name,cover_i,first_publish_year&limit=10&offset=0"
	val requestQueue = Volley.newRequestQueue(context)
	var tag: String = "tag"

	val booksData = remember { mutableStateListOf<BookData>() }

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color= MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(360.dp),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
		) {

			BottomSheetStrip()

			BottomSheetHeader(
				title = "Umm... What was that book",
				icon = R.drawable.ic_book
			)

			Spacer(modifier = Modifier.height(8.dp))

			LargeTextField(
				modifier = Modifier.padding(24.dp, 0.dp),
				text = bookNameText,
				placeholder = "Search for books",
				keyboardOptions = KeyboardOptions.Default.copy(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Search
				),
				keyboardActions = KeyboardActions(
					onSearch = {
						sheetState = SheetState.SEARCHING

						requestQueue.cancelAll(tag)

						val requestUrl = "${baseUrl}${
							URLEncoder.encode(
								bookNameText,
								StandardCharsets.UTF_8.toString()
							)
						}$endUrl"
						val stringRequest = StringRequest(
							Request.Method.GET,
							requestUrl,
							{ requestResult ->
								val jsonObject = JSONObject(requestResult)
								val docs = jsonObject.optJSONArray("docs")
								val length = docs?.length() ?: 0
								booksData.removeIf { true }
								for (i in 0 until length) {
									val bookData =
										objectMapper.readValue<BookData>(docs!!.get(i).toString())
									booksData.add(bookData)
								}
								sheetState = if (length > 0) {
									SheetState.RESULT_FOUND
								} else {
									SheetState.RESULT_NOT_FOUND
								}
							},
							{
								sheetState = SheetState.ERROR
								it.printStackTrace()
							}
						)

						tag = requestUrl
						stringRequest.tag = tag
						requestQueue.add(stringRequest)
					}
				),
				isFocused = isBookNameTextFocused,
				onFocusChanged = {isBookNameTextFocused = it},
				onValueChanged = {bookNameText = it}
			)

			Spacer(modifier = Modifier.height(8.dp))

			AnimatedContent(targetState = sheetState) {
				when (it) {
					SheetState.INIT -> ClimateChangeMessage()
					SheetState.SEARCHING -> {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.fillMaxWidth()
								.height(256.dp),
						) {
							val lottieComposition by rememberLottieComposition(
								LottieCompositionSpec.RawRes(R.raw.lottie_loading)
							)

							LottieAnimation(
								composition = lottieComposition,
								iterations = LottieConstants.IterateForever,
								modifier = Modifier.requiredSize(64.dp)
							)
						}
					}
					SheetState.RESULT_FOUND -> {
						LazyVerticalGrid(
							columns = GridCells.Adaptive(96.dp),
							modifier = Modifier.padding(8.dp)
						) {
							items(booksData) { bookData ->
								BookCard(
									bookData = bookData,
									modifier = Modifier.aspectRatio(0.75f)
								) { onAction(bookData) }
							}
						}
					}
					SheetState.RESULT_NOT_FOUND -> {
						Column(
							modifier = Modifier.height(256.dp)
						) {
							Image(
								painter = painterResource(id = R.drawable.il_result_unavailable_2),
								contentDescription = "No result found",
								modifier = Modifier
									.fillMaxWidth()
									.padding(16.dp)
							)

							Spacer(modifier = Modifier.height(16.dp))

							Text(
								text = "Sorry, we can't find that",
								style = MaterialTheme.typography.titleMedium,
								color = MaterialTheme.colorScheme.secondary,
								textAlign = TextAlign.Center,
								modifier = Modifier.fillMaxWidth()
							)
						}
					}
					SheetState.ERROR -> {
						Column(
							modifier = Modifier.height(256.dp)
						) {
							Image(
								painter = painterResource(id = R.drawable.il_result_unavailable_2),
								contentDescription = "Sorry, we cant find that now",
								modifier = Modifier
									.fillMaxWidth()
									.padding(16.dp)
							)

							Spacer(modifier = Modifier.height(16.dp))

							Text(
								text = "Sorry, we cant find that now",
								style = MaterialTheme.typography.titleMedium,
								color = MaterialTheme.colorScheme.secondary,
								textAlign = TextAlign.Center,
								modifier = Modifier.fillMaxWidth()
							)
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BookCard(
	modifier: Modifier,
	bookData: BookData,
	onAction: () -> Unit
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.padding(8.dp),
	) {
		Card(
			containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
			elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
			shape = RoundedCornerShape(12.dp),
			modifier = modifier,
			onClick = { onAction() }
		) {
			if (bookData.coverI != null) {
				Image(
					painter = rememberImagePainter(
						data = "https://covers.openlibrary.org/b/id/${bookData.coverI}-M.jpg",
						builder = { crossfade(true) }
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
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)

		if (bookData.authorName != null) {
			var author = ""
			bookData.authorName.forEach { author += " $it" }
			Text(
				text = "~ $author",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)
			)
		}
	}
}
