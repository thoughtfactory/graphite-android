package com.syncodec.graphite.presentation.bucketItem2.composable.screen.movieScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.importer.ThumbnailData
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.di.network.trakt.TraktApi
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.BucketItemStateView
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.DataChip
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.DescriptionView
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.ThumbnailView
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.TitleView
import com.syncodec.graphite.presentation.common.v2.button.SurfaceVariantButton
import com.syncodec.graphite.presentation.ui.LocalIsDarkTheme
import com.syncodec.graphite.utils.DataLoader
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun MovieTraktScreenCompact(
    bucketItemBox: BucketItemBoxDecrypted? = null,
    thumbnailDataFlow: StateFlow<DataLoader<ThumbnailData>>,
    isEditing: Boolean = false,
    onToggleBucketItemState: (BucketItemBoxDecrypted.State) -> Unit = {},
) {
    val isDarkTheme = LocalIsDarkTheme.current

    val uriHandler = LocalUriHandler.current
    val traktApi: TraktApi = koinInject()

    val bucketItemData by remember(key1 = bucketItemBox?.bucketItemData) { derivedStateOf { bucketItemBox?.bucketItemData } }
    val movieData by remember(key1 = bucketItemData) { derivedStateOf { bucketItemData as? BucketItemShow.TraktMovie } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(height = 16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ThumbnailView(thumbnailDataFlow = thumbnailDataFlow, isEditing = isEditing)
        }
        Spacer(modifier = Modifier.height(height = 24.dp))

        BucketItemStateView(
            bucketType = BucketBoxEncrypted.BucketType.Book,
            bucketItemState = bucketItemBox?.state?.ordinal ?: BucketItemBoxDecrypted.State.Alpha.ordinal,
            modifier = Modifier.padding(horizontal = 20.dp),
            onToggleBucketItemState = onToggleBucketItemState
        )
        Spacer(modifier = Modifier.height(height = 12.dp))

        TitleView(
            title = movieData?.title,
            subTitle = movieData?.tagline
        )
        Spacer(modifier = Modifier.height(height = 12.dp))

        DescriptionView(description = movieData?.overview)

        Spacer(modifier = Modifier.height(height = 8.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 0.dp)
        ) {
            movieData?.runtime?.let { DataChip(text = "$it minutes") }
            movieData?.year?.let { DataChip(text = "$it") }
            movieData?.country?.let { DataChip(text = it) }
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 0.dp)
        ) {
            movieData?.genres?.forEach {
                DataChip(text = it)
            }
        }

        Spacer(modifier = Modifier.height(height = 4.dp))
        SurfaceVariantButton(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            onClick = { uriHandler.openUri(uri = traktApi.getMovieUrlFromId(id = movieData?.ids?.trakt) ?: return@SurfaceVariantButton) }
        ) {
            Text(text = stringResource(id = R.string.open_in_trakt_tv))
            Spacer(modifier = Modifier.width(width = 12.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_launch),
                contentDescription = null,
                modifier = Modifier.size(size = 18.dp)
            )
        }

        Spacer(modifier = Modifier.height(height = 10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Image(
                painter = painterResource(id = if (isDarkTheme) R.drawable.il_logo_trakt_dark else R.drawable.il_logo_trakt_light),
                contentDescription = "trakt.tv",
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.471f)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(height = 128.dp))
    }
}
