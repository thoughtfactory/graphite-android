package com.syncodec.graphite.presentation.common.v2.bottomSheet2.graBottomSheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.shape.AbsoluteSmoothCornerShape
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.utils.SharedPref
import ir.amirreza.composepreferences.state.rememberPreferenceStateOf
import kotlinx.serialization.InternalSerializationApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun FilterAndSortBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
) {
    val scope = rememberCoroutineScope()

    var viewType by rememberPreferenceStateOf(key = SharedPref.Key.ViewType.name, defaultValue = SharedPref.ViewType.List.name)

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.filter_and_sort),
            scrollable = false
        ) {

            Spacer(modifier = Modifier.height(height = 12.dp))

            ViewType(viewType = viewType) { viewType = it.name }

            Spacer(modifier = Modifier.height(height = 8.dp))
        }
    }
}

@Composable
private fun ColumnScope.ViewType(
    viewType: String,
    actionUpdateViewType: (SharedPref.ViewType) -> Unit
) {
    with(receiver = this) {
        Text(
            text = stringResource(id = R.string.view_type),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(height = 2.dp))

        MultiChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                checked = viewType == SharedPref.ViewType.List.name,
                onCheckedChange = { actionUpdateViewType(SharedPref.ViewType.List) },
                shape = AbsoluteSmoothCornerShape(cornerRadiusTL = 16.dp, smoothnessAsPercentTL = 100, cornerRadiusBL = 16.dp, smoothnessAsPercentBL = 100),
                modifier = Modifier.weight(weight = 1f),
                icon = { Icon(painter = painterResource(id = R.drawable.ic_fa_list), contentDescription = stringResource(id = R.string.list), modifier = Modifier.requiredSize(size = 18.dp)) },
                label = { Text(text = stringResource(id = R.string.list)) }
            )
            SegmentedButton(
                checked = viewType == SharedPref.ViewType.Grid.name,
                onCheckedChange = { actionUpdateViewType(SharedPref.ViewType.Grid) },
                shape = AbsoluteSmoothCornerShape(cornerRadiusTR = 16.dp, smoothnessAsPercentTR = 100, cornerRadiusBR = 16.dp, smoothnessAsPercentBR = 100),
                modifier = Modifier.weight(weight = 1f),
                icon = { Icon(painter = painterResource(id = R.drawable.ic_fa_grid), contentDescription = stringResource(id = R.string.grid), modifier = Modifier.requiredSize(size = 18.dp)) },
                label = { Text(text = stringResource(id = R.string.grid)) }
            )
        }
    }
}
