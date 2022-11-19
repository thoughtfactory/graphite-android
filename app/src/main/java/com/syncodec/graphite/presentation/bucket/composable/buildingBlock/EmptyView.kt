package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import kotlin.random.Random


@Composable
fun EmptyView(
	bucketType : BucketType
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	var quoteWidth by remember { mutableStateOf<Int?>(null) }

	val image = remember {
		when (bucketType) {
			BucketType.TODO -> R.drawable.il_todo
			BucketType.BOOK -> if (Random.nextBoolean()) R.drawable.il_book_b else R.drawable.il_book_g
			BucketType.SHOW -> R.drawable.il_show
			BucketType.LINK -> R.drawable.il_link
			BucketType.UNKNOWN -> R.drawable.il_error
		}
	}

	val quote = remember {
		when (bucketType) {
			BucketType.TODO -> "\"Not all those who wander are lost.\""
			BucketType.BOOK -> "\"One was a book thief.\nThe other stole the sky.\""
			BucketType.SHOW -> "\"The secret to film is that it's an illusion.\""
			BucketType.LINK -> "\"As a research tool, the Internet is a invaluable.\""
			BucketType.UNKNOWN -> "\"Uh Ohh!!! Something's not right\""
		}
	}

	val author = remember {
		when (bucketType) {
			BucketType.TODO -> "― J.R.R. Tolkien"
			BucketType.BOOK -> "― Markus Zusak"
			BucketType.SHOW -> "― George Lucas"
			BucketType.LINK -> "― Noam Chomsky"
			BucketType.UNKNOWN -> "Error reading data"
		}
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Image(
			painter = painterResource(id = image),
			contentDescription = "No items found",
			modifier = Modifier.size(screenWidth * 3 / 4)
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = quote,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			fontSize = 14.sp,
			lineHeight = 16.sp,
			letterSpacing = 2.sp,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier
				.widthIn(max = screenWidth * 3 / 4)
				.onGloballyPositioned {
				quoteWidth = it.size.width
			}
		)

		Spacer(modifier = Modifier.height(8.dp))

		AnimatedVisibility(
			visible = quoteWidth != null,
			enter = fadeIn(tween(300)),
			exit = fadeOut(tween(300))
		) {
			Text(
				text = author,
				fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
				fontWeight = FontWeight.Bold,
				fontSize = 14.sp,
				lineHeight = 16.sp,
				letterSpacing = 2.sp,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.width(with(LocalDensity.current) { (quoteWidth ?: 0).toDp() }),
				textAlign = TextAlign.End
			)
		}
	}
}
