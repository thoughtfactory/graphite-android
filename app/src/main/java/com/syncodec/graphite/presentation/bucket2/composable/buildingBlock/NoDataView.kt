package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted


@Composable
fun NoDataView(
    bucketType: BucketBoxEncrypted.BucketType
) {

    val icon = when (bucketType) {
        BucketBoxEncrypted.BucketType.Todo -> R.drawable.il_mm_bucket_todo_screen
        BucketBoxEncrypted.BucketType.Book -> return
        BucketBoxEncrypted.BucketType.Show -> return
        BucketBoxEncrypted.BucketType.Link -> return
        BucketBoxEncrypted.BucketType.Location -> return
        BucketBoxEncrypted.BucketType.Unknown -> return
    }

    val quoteText = when (bucketType) {
        BucketBoxEncrypted.BucketType.Todo -> "\"Not all those who wander are lost\""
        BucketBoxEncrypted.BucketType.Book -> return
        BucketBoxEncrypted.BucketType.Show -> return
        BucketBoxEncrypted.BucketType.Link -> return
        BucketBoxEncrypted.BucketType.Location -> return
        BucketBoxEncrypted.BucketType.Unknown -> return
    }
    val authorText = when (bucketType) {
        BucketBoxEncrypted.BucketType.Todo -> "~ J.R.R Tolkien"
        BucketBoxEncrypted.BucketType.Book -> return
        BucketBoxEncrypted.BucketType.Show -> return
        BucketBoxEncrypted.BucketType.Link -> return
        BucketBoxEncrypted.BucketType.Location -> return
        BucketBoxEncrypted.BucketType.Unknown -> return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(fraction = 0.47f)
        )
        Spacer(modifier = Modifier.height(height = 48.dp))
        Text(
            text = quoteText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(fraction = 0.71f)
        )
        Spacer(modifier = Modifier.height(height = 12.dp))
        Text(
            text = authorText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(fraction = 0.71f)
        )
    }
}
