package com.syncodec.momento.mainComponent.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.R


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun ScratchpadScreen() {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	val screenHeight = configuration.screenHeightDp.dp

	Box {
		Image(
			painter = painterResource(id = R.drawable.home_background2),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.alpha(0.71f)
				.blur(0.dp),
		)
		Box() {
			LazyColumn(
				modifier = Modifier
					.padding(0.dp, 112.dp, 0.dp, 64.dp)
			) {


				items(10) {
					ListItem {
						Text(text = "Hello world")
					}
				}
			}
		}
	}
}
