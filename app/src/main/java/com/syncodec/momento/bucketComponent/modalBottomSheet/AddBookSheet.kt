package com.syncodec.momento.bucketComponent.modalBottomSheet

import android.content.Intent
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.bucketComponent.EditActivity
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
	val objectMapper = ObjectMapper().registerModule(KotlinModule())

	var bookNameText by rememberSaveable { mutableStateOf("") }
	var bookNameTextBackgroundColor by remember { mutableStateOf(Color(244, 244, 245)) }

	val baseUrl = "https://openlibrary.org/search.json?title="
	val endUrl = "&fields=key,title,author_name,cover_i&limit=10&offset=0"
	val requestQueue = Volley.newRequestQueue(context)
	var tag: String = "tag"

	var booksData by remember { mutableStateOf(listOf<BookData>()) }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(Color.White),
	) {

		Spacer(modifier = Modifier.height(16.dp))

		Box(
			modifier = Modifier
				.width(48.dp)
				.height(6.dp)
				.background(Color(244, 244, 245, 255), RoundedCornerShape(4.dp))
		)

		Spacer(modifier = Modifier.height(12.dp))

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "Umm... What was that book \uD83E\uDD14",
				style = MaterialTheme.typography.titleMedium,
			)
			Spacer(modifier = Modifier.weight(1f))
			Icon(imageVector = TablerIcons.Notebook, "")
		}

		Spacer(modifier = Modifier.height(16.dp))

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
				)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BookButton(
	modifier: Modifier,
	bookData: BookData
) {
	val context = LocalContext.current
	val objectMapper = ObjectMapper().registerModule(KotlinModule())

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp),
	) {
		Card(
			elevation = 12.dp,
			modifier = modifier,
			onClick = {
				Intent(context, EditActivity::class.java).apply {
					putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.BOOKS)
					putExtra(Konstant.Companion.Konstant.BOOK_DATA.name, objectMapper.writeValueAsString(bookData))
					context.startActivity(this)
				}
			}
		) {

			if (bookData.coverI!=null) {
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

		if (bookData.authorName!=null) {
			var author = ""
			bookData.authorName.forEach { author+=" $it" }
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
	val authorName: List<String>?
)
