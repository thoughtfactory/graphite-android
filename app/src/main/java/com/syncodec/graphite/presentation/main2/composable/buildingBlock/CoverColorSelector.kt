package com.syncodec.graphite.presentation.main2.composable.buildingBlock

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.main2.composable.bottomSheet.SelectedThumbnail
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.toHexString
import kotlinx.serialization.InternalSerializationApi


val ChapterCoverColorList = listOf(
    Color(0xFFF06292),
    Color(0xFFBA68C8),
    Color(0xFF9575CD),
    Color(0xFF7986CB),
    Color(0xFF64B5F6),
    Color(0xFF4FC3F7),
    Color(0xFF4DD0E1),
    Color(0xFF4DB6AC),
    Color(0xFF81C784),
    Color(0xFFAED581),
    Color(0xFFDCE775),
    Color(0xFFFFD54F),
    Color(0xFFFFB74D),
    Color(0xFFFF8A65),
    Color(0xFFA1887F),
    Color(0xFF90A4AE),
    Color(0xFFE0E0E0),
    Color(0xFFBDBDBD),
    Color(0xFF9E9E9E),
    Color(0xFF757575),
    Color(0xFF616161),
    Color(0xFF424242),
    Color(0xFF000000),
)

@OptIn(ExperimentalLayoutApi::class, InternalSerializationApi::class)
@Composable
fun CoverColorSelector(
    selectedThumbnail: SelectedThumbnail,
    onSelectColor: (SelectedThumbnail) -> Unit,
    onClickCustomColorPicker: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VerticalGrid(
            columns = SimpleGridCells.Adaptive(54.dp),
            modifier = Modifier.wrapContentHeight()
        ) {
            ChapterCoverColorList.forEach { color ->
                val isSelected = selectedThumbnail is SelectedThumbnail.Color && selectedThumbnail.value == color
                val padding by animateDpAsState(if (isSelected) 2.dp else 8.dp)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(padding)
                        .background(color = color, shape = MaterialTheme.shapes.small)
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onSelectColor(SelectedThumbnail.Color(value = color)) }
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSelected,
                        enter = AnimationDefaults.ScaleAndFadeEnter,
                        exit = AnimationDefaults.ScaleAndFadeExit
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_check), tint = color.getInverseBWColor(), contentDescription = null)
                    }
                }
            }
        }

        ColorPreview(selectedThumbnail = selectedThumbnail, onClickCustomColorPicker = onClickCustomColorPicker)
    }
}

@Composable
private fun ColorPreview(
    selectedThumbnail: SelectedThumbnail,
    onClickCustomColorPicker: () -> Unit = {}
) {
    val descriptionTextFieldController = GenericTextField2.rememberTextField2Controller(initialFocus = false)
    LaunchedEffect(key1 = selectedThumbnail) { if (selectedThumbnail is SelectedThumbnail.Color) descriptionTextFieldController.onValueChange(selectedThumbnail.value.toHexString()) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.custom_color)
        )

        Spacer(modifier = Modifier.weight(1f))


        Surface(
            color = (selectedThumbnail as? SelectedThumbnail.Color)?.value ?: MaterialTheme.colorScheme.background,
            contentColor = ((selectedThumbnail as? SelectedThumbnail.Color)?.value ?: MaterialTheme.colorScheme.background).getInverseBWColor(),
            shape = MaterialTheme.shapes.medium,
            onClick = onClickCustomColorPicker
        ) {
            Text(
                text = "${(selectedThumbnail as? SelectedThumbnail.Color)?.value?.toHexString()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
    }
}
