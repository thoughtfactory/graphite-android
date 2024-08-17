package com.syncodec.graphite.presentation.main2.bar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.button.SegmentedButton
import com.syncodec.graphite.presentation.common.shape.AbsoluteSmoothCornerShape
import com.syncodec.graphite.presentation.main2.HomeComponent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabNavigator(
    currentRoute: HomeComponent = HomeComponent.Note,
    onNavigate: (HomeComponent) -> Unit = {}
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        SegmentedButton(
            selected = currentRoute == HomeComponent.Note,
            onClick = { onNavigate(HomeComponent.Note) },
            modifier = Modifier.weight(1f),
            shape = AbsoluteSmoothCornerShape(cornerRadiusTL = 12.dp, cornerRadiusBL = 12.dp),
            icon = HomeComponent.Note.icon,
            label = stringResource(HomeComponent.Note.title)
        )

        SegmentedButton(
            selected = currentRoute == HomeComponent.Bucket,
            onClick = { onNavigate(HomeComponent.Bucket) },
            modifier = Modifier.weight(1f),
            shape = RectangleShape,
            icon = HomeComponent.Bucket.icon,
            label = stringResource(HomeComponent.Bucket.title)
        )

        SegmentedButton(
            selected = currentRoute == HomeComponent.Notebook,
            onClick = { onNavigate(HomeComponent.Notebook) },
            modifier = Modifier.weight(1f),
            shape = AbsoluteSmoothCornerShape(cornerRadiusTR = 12.dp, cornerRadiusBR = 12.dp),
            icon = HomeComponent.Notebook.icon,
            label = stringResource(HomeComponent.Notebook.title)
        )

    }
}
