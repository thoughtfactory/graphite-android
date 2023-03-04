package com.syncodec.graphite.presentation.main.composable.buildingBlock

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R


data class DropdownMenuItem(
	val title : String,
	val icon : Int? = null,
	val iconColor : Color? = null,
	val isPro : Boolean = false,
	val onClick : () -> Unit = {},
)

@Composable
fun DropdownMenu(
	title : String? = null,
	itemList : List<DropdownMenuItem>,
	containerColor : Color = Color(
		ColorUtils.blendARGB(
			MaterialTheme.colorScheme.background.toArgb(),
			MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(),
			0.17f
		)
	),
	contentColor : Color = MaterialTheme.colorScheme.onSurface,
	isVisible : Boolean,
	offset : DpOffset? = null,
	onDismissRequest : () -> Unit,
) {
	val context = LocalContext.current
	val isPro by BaseApplication.isPro.collectAsState()

	DropdownMenu(
		expanded = isVisible,
		onDismissRequest = onDismissRequest,
		modifier = Modifier.background(containerColor),
		offset = offset ?: DpOffset.Zero
	) {
		title?.let {
			Text(
				text = it,
				style = MaterialTheme.typography.titleMedium,
				color = contentColor,
				textAlign = TextAlign.Center,
				fontWeight = FontWeight.Bold,
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp),
			)
		}
		itemList.forEach { dropdownMenuItem ->
			DropdownMenuItem(
				onClick = {
					if (! dropdownMenuItem.isPro || isPro) dropdownMenuItem.onClick()
					else Toast.makeText(context, "Join Graphite Pro to unlock this feature", Toast.LENGTH_SHORT).show()
				},
				leadingIcon = dropdownMenuItem.icon?.let {
					{
						Icon(
							painter = painterResource(id = it),
							contentDescription = null,
							tint = dropdownMenuItem.iconColor ?: contentColor,
							modifier = Modifier.requiredSize(20.dp),
						)
					}
				},
				text = {
					Text(
						text = dropdownMenuItem.title,
						style = MaterialTheme.typography.bodyMedium,
						color = contentColor,
					)
				},
				trailingIcon = if (dropdownMenuItem.isPro && ! isPro) {
					{
						Icon(
							painter = painterResource(id = R.drawable.ic_lock_close),
							contentDescription = null,
							tint = contentColor,
							modifier = Modifier.requiredSize(20.dp),
						)
					}
				} else null,
			)
		}
	}
}
