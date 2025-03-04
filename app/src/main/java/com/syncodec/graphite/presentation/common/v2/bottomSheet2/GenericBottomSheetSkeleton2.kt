package com.syncodec.graphite.presentation.common.v2.bottomSheet2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.onlyIfComposable


@Composable
fun GenericBottomSheetSkeleton2(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    paddingValues: PaddingValues = PaddingValues(horizontal = 24.dp),
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .onlyIfComposable(modifier = { verticalScroll(state = rememberScrollState()) }) { scrollable }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(alignment = Alignment.Start)
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
                modifier = Modifier.align(alignment = Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(height = 8.dp))
        content()
        Spacer(modifier = Modifier.height(height = 24.dp))
    }
}


