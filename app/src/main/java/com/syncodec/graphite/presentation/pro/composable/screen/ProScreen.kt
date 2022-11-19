package com.syncodec.graphite.presentation.pro.composable.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.pro.composable.bar.TopBar
import com.syncodec.graphite.presentation.pro.composable.buildingBlock.LifetimePackageView
import com.syncodec.graphite.presentation.pro.composable.buildingBlock.ProFeaturesView
import com.syncodec.graphite.presentation.pro.composable.buildingBlock.SubscriptionPackageView


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SubscriptionScreen() {
	ModalBottomSheetLayout(
		sheetContent = {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(24.dp)
			)
		},
		modifier = Modifier.fillMaxSize()
	) {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			topBar = { TopBar() }
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxSize()
					.padding(it)
					.verticalScroll(rememberScrollState())
			) {

				Text(
					text = "Unlock the full potential of Graphite with pro",
					fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
					fontWeight = FontWeight.Bold,
					fontSize = 16.sp,
					lineHeight = 18.sp,
					letterSpacing = 2.sp,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.padding(32.dp, 0.dp)
				)

				Spacer(modifier = Modifier.height(8.dp))

				ProFeaturesView()

				Spacer(modifier = Modifier.height(24.dp))
				SubscriptionPackageView()
				Spacer(modifier = Modifier.height(16.dp))
				LifetimePackageView()
				Spacer(modifier = Modifier.height(12.dp))

				Button(
					onClick = { /*TODO*/ },
					shape = RoundedCornerShape(25)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_restore),
						contentDescription = "Restore purchase"
					)
					Spacer(modifier = Modifier.width(12.dp))
					Text(text = "Restore purchase")
				}
				Spacer(modifier = Modifier.height(32.dp))
			}
		}
	}
}
