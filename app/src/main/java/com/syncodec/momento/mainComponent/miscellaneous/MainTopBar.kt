package com.syncodec.momento.mainComponent.miscellaneous

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.syncodec.momento.R
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.ui.theme.Stardos
import compose.icons.TablerIcons
import compose.icons.tablericons.Cloud
import compose.icons.tablericons.Search

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainTopBar(
	showBackground: Boolean = false,
	openSheet: (BottomSheetType) -> Unit
) {
	Column(
		modifier = Modifier
			.zIndex(1f)
			.background(if (showBackground) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified),
		) {

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(88.dp)
		) {
			Box(
				modifier = Modifier
					.padding(16.dp)
			) {
				Card(
					elevation = 0.dp,
					shape = RoundedCornerShape(8.dp),
					backgroundColor = Color.White,
					modifier = Modifier
						.fillMaxWidth()
						.fillMaxHeight()
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(8.dp)
					) {
						IconButton(
							onClick = {
								openSheet(BottomSheetType.MenuBottomSheet)
							},
						) {
							Icon(
								painter = painterResource(id = R.drawable.ic_icon),
								contentDescription = null,
								tint = Color.Unspecified,
								modifier = Modifier
									.size(48.dp)
									.padding(6.dp, 6.dp)
							)
						}

						Spacer(modifier = Modifier.width(0.dp))

						Text(
							text = "MOMENTO",
							style = TextStyle(
								fontFamily = Stardos,
								fontSize = 16.sp,
								fontWeight = FontWeight.Bold,
								letterSpacing = 2.sp,
							),
							color = Color(121, 177, 217, 255),
							modifier = Modifier
								.height(20.dp)
						)

						Spacer(modifier = Modifier.weight(1f))

						IconButton(
							onClick = { /*TODO*/ },
						) {
							Icon(
								imageVector = TablerIcons.Cloud,
								contentDescription = null,
								tint = Color.Unspecified,
							)
						}

						IconButton(
							onClick = { /*TODO*/ },
						) {
							Icon(
								imageVector = TablerIcons.Search,
								contentDescription = null,
								tint = Color.Unspecified,
							)
						}

					}
				}
			}
		}
	}
}
