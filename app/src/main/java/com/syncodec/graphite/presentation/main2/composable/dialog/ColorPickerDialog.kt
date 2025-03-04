package com.syncodec.graphite.presentation.main2.composable.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.syncodec.graphite.presentation.common.v2.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.v2.dialog2.GenericDialog2State
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.v2.dialog2.GenericDialogActionButton
import com.syncodec.graphite.utils.getRandomColor
import com.syncodec.graphite.utils.toHexString


@Composable
fun ColorPickerDialog(
    state: GenericDialog2State = GenericDialog2State.initialize(),
    selectedColor: Color? = null,
    onSelectColor: (Color) -> Unit = {}
) {

    val colorPickerController = rememberColorPickerController()
    LaunchedEffect(selectedColor) { colorPickerController.selectByColor(color = selectedColor ?: getRandomColor(), fromUser = true) }

    GenericDialog2(
        state = state,
        title = stringResource(R.string.color_picker),
        secondaryButton = { GenericDialogActionButton.SecondaryButton(modifier = Modifier.weight(1f), text = stringResource(R.string.cancel)) { state.closeDialog() } },
        primaryButton = { GenericDialogActionButton.PrimaryButton(modifier = Modifier.weight(1f), text = stringResource(R.string.okay)) { onSelectColor(colorPickerController.selectedColor.value); state.closeDialog() } },
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            HsvColorPicker(
                controller = colorPickerController,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(10.dp),
            )
        }
        BrightnessSlider(
            controller = colorPickerController,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = (colorPickerController.selectedColor.value).toHexString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f))
            AlphaTile(
                controller = colorPickerController,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

}
