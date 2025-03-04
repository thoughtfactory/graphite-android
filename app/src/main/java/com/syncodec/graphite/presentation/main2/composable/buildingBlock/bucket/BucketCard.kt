package com.syncodec.graphite.presentation.main2.composable.buildingBlock.bucket

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.ui.ICON_SIZE
import kotlinx.serialization.InternalSerializationApi


@OptIn(InternalSerializationApi::class)
@Composable
@Preview
fun BucketCard(
    modifier: Modifier = Modifier,
    titleText: String? = null,
    descriptionText: String? = null,
    bucketSize: Int? = null,
    bucketType: BucketBox.BucketType = BucketBox.BucketType.Unknown,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {

    SelectableContainer2(
        enabled = true,
        selected = selected,
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio = 1.5f)
            .padding(all = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BucketIcon(bucketType = bucketType)
                Spacer(modifier = Modifier.weight(weight = 1f))
                Text(
                    text = bucketSize?.toString() ?: "?",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(weight = 1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText ?: stringResource(id = R.string.no_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun BucketIcon(
    bucketType: BucketBox.BucketType
) {
    val icon = when(bucketType) {
        BucketBox.BucketType.Todo -> R.drawable.ic_fa_todo_duotone
        BucketBox.BucketType.Book -> R.drawable.ic_fa_books_duotone
        BucketBox.BucketType.Show -> R.drawable.ic_fa_film_duotone
        BucketBox.BucketType.Link -> R.drawable.ic_fa_link_duotone
        BucketBox.BucketType.Location -> R.drawable.ic_fa_map_pin_duotone
        BucketBox.BucketType.Unknown -> R.drawable.ic_fa_question_mark
    }

    Icon(
        painter = painterResource(id = icon),
        contentDescription = bucketType.name,
        modifier = Modifier.size(size = ICON_SIZE)
    )
}
