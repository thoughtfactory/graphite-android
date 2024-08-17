package com.syncodec.graphite.presentation.common.component.notePreview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.presentation.base.LocationContainer
import com.syncodec.graphite.presentation.common.component.HeaderText
import com.syncodec.graphite.presentation.common.component.LocalComponentHeight
import com.syncodec.graphite.presentation.common.component.SelectableContainer
import com.syncodec.graphite.presentation.common.component.SelectableContainerColors
import com.syncodec.graphite.presentation.common.component.SelectableContainerDefaults
import com.syncodec.graphite.presentation.common.component.StateInfo
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.timeStampToTime
import io.realm.kotlin.types.RealmUUID
import java.time.Instant


@Preview
@Composable
fun NoteListCard2(
    id: RealmUUID = RealmUUID.random(),
    timestamp: String = Instant.now().toEpochMilli().timeStampToTime(),
    title: String? = null,
    contentThumbnail: String? = null,
    address: String? = null,
    latLng: LatLng? = null,
    isFavourite: Boolean = false,
    isLocked: Boolean = false,
    tagList: List<TagObjectLite> = listOf(),
    selected: Boolean = false,
    colors: SelectableContainerColors = SelectableContainerDefaults.selectableContainerColors(),
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val context = LocalContext.current

    val contentColor by colors.contentColor(selected = selected)

//    val attachmentCount = context.getAttachmentCountFromNoteId(parentId = id)
    val attachmentCount = 0

    SelectableContainer(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.31f)),
        selected = selected,
        colors = colors,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Header(
                timestamp = timestamp,
                title = title,
                isFavourite = isFavourite,
                isLocked = isLocked,
                attachmentCount = attachmentCount,
            )
            Spacer(modifier = Modifier.height(4.dp))
            ContentPreview(
                textContent = contentThumbnail,
//                previewData = previewData,
                tagList = tagList
            )
            LocationView(
                latLng = latLng,
                address = address,
            )
        }
    }
}

@Preview
@Composable
private fun Header(
    timestamp: String = Instant.now().toEpochMilli().timeStampToTime(),
    title: String? = null,
    isFavourite: Boolean = false,
    isLocked: Boolean = false,
    attachmentCount: Int = 0,
) {

    val headerText by remember(timestamp, title) { derivedStateOf { if (title.isNullOrEmpty()) timestamp else "$timestamp · $title" } }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.heightIn(22.dp)
    ) {
        HeaderText(
            text = headerText,
            modifier = Modifier.weight(1f)
        )
        StateInfo(
            isFavourite = isFavourite,
            isLocked = isLocked,
            attachmentCount = attachmentCount,
        )
    }
}

@Preview
@Composable
private fun ContentPreview(
    textContent: String? = null,
//    previewData: PreviewData? = null,
    tagList: List<TagObjectLite> = listOf()
) {

    val componentHeight = LocalComponentHeight.current

    Row(
        modifier = Modifier.height(componentHeight)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = textContent ?: "",
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 16.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (tagList.isNotEmpty()) TagList(tagList = tagList)
        }

//        if (previewData != null) {
//            Spacer(modifier = Modifier.width(12.dp))
//            Surface(
//                shape = MaterialTheme.shapes.small,
//                modifier = Modifier.size(if (componentHeight < 108.dp) (componentHeight - 20.dp) else 88.dp),
//            ) {
//                when (previewData) {
//                    is PreviewData.Image -> ImageAttachmentPreview(previewData = previewData, blur = false)
//                    is PreviewData.Video -> VideoAttachmentPreview(previewData = previewData, blur = false)
//                    is PreviewData.Pdf -> PdfAttachmentPreview(previewData = previewData)
//                    else -> UnknownAttachmentPreview(previewData = previewData, small = true)
//                }
//            }
//        }
    }
}

@Preview
@Composable
private fun TagList(
    tagList: List<TagObjectLite> = listOf(TagObject.getRandomInstance().toLite(), TagObject.getRandomInstance().toLite())
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        tagList.forEach { tagObject -> TagItemView(tagObject = tagObject) }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Preview
@Composable
private fun TagItemView(
    tagObject: TagObjectLite = TagObject.getRandomInstance().toLite()
) {
    val containerColor by remember(tagObject.color) { derivedStateOf { Color(tagObject.color).copy(alpha = 0.42f) } }
    val contentColor by remember(tagObject.color) { derivedStateOf { Color(tagObject.color).getInverseBWColor() } }

    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .background(containerColor, MaterialTheme.shapes.extraSmall)
    ) {
        Text(
            text = tagObject.tag,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview
@Composable
private fun LocationView(
    latLng: LatLng? = null,
    address: String? = null,
) {
    if (latLng != null || !address.isNullOrEmpty()) {
        Column {
            Spacer(modifier = Modifier.height(6.dp))
            when {
                !address.isNullOrEmpty() -> Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fa_map_marker_dot_solid),
                        contentDescription = stringResource(id = R.string.location_marker),
                        tint = Color.LocationContainer.copy(alpha = 0.47f),
                        modifier = Modifier.requiredSize(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = address,
                        style = MaterialTheme.typography.labelMedium,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                latLng != null -> Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fa_map_marker_dot),
                        contentDescription = stringResource(id = R.string.location_marker),
                        tint = Color.LocationContainer.copy(alpha = 0.47f),
                        modifier = Modifier.requiredSize(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = latLng.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun NoteListCardPreview() {
    CompositionLocalProvider(
        LocalComponentHeight provides 128.dp
    ) {
        NoteListCard2(
            id = RealmUUID.random(),
            timestamp = Instant.now().toEpochMilli().timeStampToTime(),
            title = "Ramanujan",
            contentThumbnail = "Srinivasa Ramanujan FRS (/ˈsriːnɪvɑːsə rɑːˈmɑːnʊdʒən/ SREE-nih-vah-sə rah-MAH-nuuj-ən;[1] born Srinivasa Ramanujan Aiyangar, Tamil: [sriːniʋaːsa ɾaːmaːnud͡ʑan ajːaŋgar]; 22 December 1887 – 26 April 1920)[2][3] was an Indian mathematician. Though he had almost no formal training in pure mathematics, he made substantial contributions to mathematical analysis, number theory, infinite series, and continued fractions, including solutions to mathematical problems then considered unsolvable.",
            address = "18 Alahiri Street, Erode, Tamil Nadu, India",
            latLng = LatLng(latitude = 11.340889, longitude = 77.717111),
            isFavourite = true,
            isLocked = true,
            tagList = listOf(TagObject.getRandomInstance().toLite(), TagObject.getRandomInstance().toLite()),
            selected = false,
            onClick = {},
            onLongClick = {},
        )
    }
}
