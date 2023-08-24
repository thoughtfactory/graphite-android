package com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun GenericBottomSheetSkeleton2(
	modifier: Modifier = Modifier,
	title : String = "Attachments",
	subtitle : String? = null,
	paddingValues: PaddingValues = PaddingValues(horizontal = 24.dp),
	content : @Composable ColumnScope.() -> Unit = {}
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(paddingValues)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.headlineSmall,
			fontWeight = FontWeight.Bold,
		)
		subtitle?.let {
			Text(
				text = it,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
			)
		}
		Spacer(modifier = Modifier.height(8.dp))
		content()
		Spacer(modifier = Modifier.height(24.dp))
	}
}
