package com.syncodec.graphite.presentation.note2.bar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.R


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditorTopBar(
    parentChapter: ChapterObject = ChapterObject.getRandomInstance(),
    onClickSave: () -> Unit = {},
    onClickBack: () -> Unit = {},
    onClickSelectChapter: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        navigationIcon = { BackButton(onClick = onClickBack) },
        title = { ChapterSelector(parentChapter = parentChapter, onClickSelectChapter = onClickSelectChapter) },
        actions = {
            Button(shape = MaterialTheme.shapes.medium, onClick = onClickSave) { Text(text = stringResource(id = R.string.save)) }
            Spacer(modifier = Modifier.width(4.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        )
    )
}

@Preview
@Composable
fun ChapterSelector(
    parentChapter: ChapterObject = ChapterObject.getRandomInstance(),
    onClickSelectChapter: () -> Unit = {},
) {
    TextButton(
        onClick = onClickSelectChapter,
        colors = ButtonDefaults.textButtonColors(),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = parentChapter.title ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_fa_notebook),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.requiredSize(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
}
