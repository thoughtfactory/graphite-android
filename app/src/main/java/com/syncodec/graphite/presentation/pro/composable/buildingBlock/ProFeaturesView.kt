package com.syncodec.graphite.presentation.pro.composable.buildingBlock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.pager.HorizontalPagerIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProFeaturesView() {

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 3132) { 50000 }

    LaunchedEffect(key1 = null) {
        launch {
            while (true) {
                delay(3000)
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
        ) {
            when (it % 7) {
                0 -> Pair(R.drawable.il_pro_rich_text, "Unleash the power of rich text editing")
                1 -> Pair(R.drawable.il_pro_attachment, "No limit on the number of attachments you store locally")
                2 -> Pair(R.drawable.il_pro_geo_tagging, "Automatically geo tag your notes")
                3 -> Pair(R.drawable.il_pro_notification, "Pin your notes to the notification bar")
                4 -> Pair(R.drawable.il_pro_bucket_list, "Create multiple buckets of similar types")
                5 -> Pair(R.drawable.il_pro_chapter, "Add chapters in your notebooks")
                6 -> Pair(R.drawable.il_pro_tag, "Tag your notes for easy search")
                else -> Pair(R.drawable.il_pro_rich_text, "Unleash the power of rich text editing")
            }.let {
                ProFeatureItemView(imageId = it.first, text = it.second)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalPagerIndicator(
            pagerState = pagerState,
            pageCount = 7,
            pageIndexMapping = { it % 7 },
        )
    }
}

@Composable
private fun ProFeatureItemView(
    imageId: Int,
    text: String,
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = imageId),
            contentDescription = null,
            modifier = Modifier.height(screenHeight / 3)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
            modifier = Modifier.padding(24.dp, 0.dp)
        )
    }
}
