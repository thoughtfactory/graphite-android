package com.syncodec.graphite.presentation.note.composable.bar.buildingBlock

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.ChapterObjectLite


@Preview
@Composable
fun ChapterSelector(
	parentChapter: ChapterObjectLite? = ChapterObjectLite.getRandomInstance(),
	onClickSelectChapter : () -> Unit = {},
) {
	TextButton(
		onClick = onClickSelectChapter,
		colors = ButtonDefaults.textButtonColors(),
		shape = MaterialTheme.shapes.medium,
		modifier = Modifier
	) {
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = parentChapter?.title ?: "",
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
		Spacer(modifier = Modifier.width(8.dp))
		Icon(
			painter = painterResource(id = R.drawable.ic_fa_notebook),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.requiredSize(16.dp)
		)
		Spacer(modifier = Modifier.width(8.dp))
	}
}

