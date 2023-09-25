package com.syncodec.graphite.presentation.common.attachment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.attachment.previewer.PreviewData


@Composable
fun UnknownAttachmentPreview(previewData: PreviewData, small: Boolean = false) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier.fillMaxSize()
	) {
		Icon(
			painter = painterResource(id = R.drawable.ic_fa_file_duotone),
			contentDescription = stringResource(id = R.string.thumbnail),
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.requiredSize(if (small) 24.dp else 48.dp)
		)
		previewData.extension?.let {
			Spacer(modifier = Modifier.height(if (small) 4.dp else 8.dp))
			Text(
				text = it,
				style = if (small) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)
		}
	}
}
