package com.syncodec.graphite.presentation.common.component.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.AttachmentContainer
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.LockClosedContainer


@Preview
@Composable
fun StateInfo(
	isFavourite: Boolean = true,
	isLocked: Boolean = true,
	attachmentCount: Int = 7,
) {
	if (isLocked || isFavourite || attachmentCount > 0) {
		Surface(
			shape = MaterialTheme.shapes.extraSmall,
			color = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.background.toArgb(), MaterialTheme.colorScheme.surface.toArgb(), 0.47f)),
			contentColor = MaterialTheme.colorScheme.onSurface
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.padding(8.dp, 4.dp)
			) {
				if (isLocked) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_lock_close_duotone),
						contentDescription = stringResource(id = R.string.locked),
						tint = Color.LockClosedContainer,
						modifier = Modifier.requiredSize(14.dp)
					)
					if (isFavourite || attachmentCount > 0) Text(
						text = "·",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						modifier = Modifier.padding(horizontal = 2.dp)
					)
				}
				if (isFavourite) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_heart_solid),
						contentDescription = stringResource(id = R.string.favourite),
						tint = Color.FavouriteContainer.copy(alpha = 0.47f),
						modifier = Modifier.requiredSize(14.dp)
					)
					if (attachmentCount > 0) Text(
						text = "·",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						modifier = Modifier.padding(horizontal = 2.dp)
					)
				}
				if (attachmentCount > 0) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_file_duotone),
						contentDescription = stringResource(id = R.string.attachment_count),
						tint = Color.AttachmentContainer,
						modifier = Modifier.requiredSize(14.dp)
					)
					Text(
						text = "· $attachmentCount",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}
		}
	}
}

