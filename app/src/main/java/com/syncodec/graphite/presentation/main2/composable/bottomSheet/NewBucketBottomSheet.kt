package com.syncodec.graphite.presentation.main2.composable.bottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2Defaults
import com.syncodec.graphite.presentation.common.v2.textField2.rememberTextField2Controller
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun NewBucketBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    onCreateNewBucket: (BucketBoxDecrypted) -> Unit
) {
    val scope = rememberCoroutineScope()

    val titleTextFieldController = rememberTextField2Controller(initialFocus = false)
    val descriptionTextFieldController = rememberTextField2Controller(initialFocus = false)
    var selectedBucketType: BucketBoxEncrypted.BucketType? by remember { mutableStateOf(value = null) }
    var isSelectedBucketTypeValidationError by remember { mutableStateOf(value = false) }

    fun saveBucket() {
        val titleValidationResult = titleTextFieldController.validate { it.isNotBlank() }
        val descriptionValidationResult = descriptionTextFieldController.validate { true }
        val bucketType = selectedBucketType

        isSelectedBucketTypeValidationError = bucketType == null

        if (titleValidationResult.isValidated && descriptionValidationResult.isValidated && bucketType != null) {
            val bucketBox = BucketBoxDecrypted.newInstance.copy(title = titleValidationResult.text, description = descriptionValidationResult.text, bucketType = bucketType)
            onCreateNewBucket(bucketBox)

            titleTextFieldController.reset()
            descriptionTextFieldController.reset()
            selectedBucketType = null
            bottomSheet2State.hideSheet(scope = scope)
        }
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.new_list),
        ) {

            Spacer(modifier = Modifier.height(height = 8.dp))

            BucketListRow(bucketType = selectedBucketType) { selectedBucketType = it }

            AnimatedContent(
                targetState = isSelectedBucketTypeValidationError,
                transitionSpec = { AnimationDefaults.ExpandAndShrink },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (it) Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.selected_bucket_type_validation_error),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else Spacer(modifier = Modifier.height(height = 24.dp))
            }

            GenericTextField2(
                controller = titleTextFieldController,
                label = stringResource(id = R.string.list_title),
                placeholder = stringResource(id = R.string.name_your_bucket_list),
                errorMessage = stringResource(id = R.string.list_title_error),
                keyboardOptions = GenericTextField2Defaults.Options.getTextNextKeyboardOptionsDefault()
            )

            Spacer(modifier = Modifier.height(height = 4.dp))

            GenericTextField2(
                controller = descriptionTextFieldController,
                label = stringResource(R.string.description_optional),
                placeholder = stringResource(R.string.notebook_description_placeholder),
                minLines = 4,
                maxLines = Int.MAX_VALUE,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(height = 4.dp))

            Button(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::saveBucket,
                content = { Text(text = stringResource(id = R.string.save)) }
            )
        }
    }
}

@Composable
private fun BucketListRow(
    bucketType: BucketBoxEncrypted.BucketType? = null,
    onClickBucketCard: (BucketBoxEncrypted.BucketType) -> Unit = {}
) {

    val scrollState = rememberScrollState()
    LaunchedEffect(key1 = Unit) {
        withContext(Dispatchers.IO) {
            delay(710)
            scrollState.animateScrollTo(value = scrollState.maxValue, animationSpec = AnimationDefaults.stateAnimationSpec())
            delay(310)
            scrollState.animateScrollTo(value = 0, animationSpec = AnimationDefaults.stateAnimationSpec())
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(state = scrollState)
    ) {
        BucketCard(
            icon = R.drawable.ic_fa_todo,
            typeText = stringResource(R.string.todo),
            typeDescriptionText = stringResource(R.string.list_todo_description),
            selected = bucketType == BucketBoxEncrypted.BucketType.Todo,
            onClick = { onClickBucketCard(BucketBoxEncrypted.BucketType.Todo) },
        )
        Spacer(modifier = Modifier.width(width = 12.dp))
        BucketCard(
            icon = R.drawable.ic_fa_books,
            typeText = stringResource(R.string.books),
            typeDescriptionText = stringResource(R.string.list_book_description),
            selected = bucketType == BucketBoxEncrypted.BucketType.Book,
            onClick = { onClickBucketCard(BucketBoxEncrypted.BucketType.Book) },
        )
        Spacer(modifier = Modifier.width(width = 12.dp))
        BucketCard(
            icon = R.drawable.ic_fa_film,
            typeText = stringResource(R.string.movies_shows),
            typeDescriptionText = stringResource(R.string.list_show_description),
            selected = bucketType == BucketBoxEncrypted.BucketType.Show,
            onClick = { onClickBucketCard(BucketBoxEncrypted.BucketType.Show) },
        )
        Spacer(modifier = Modifier.width(width = 12.dp))
        BucketCard(
            icon = R.drawable.ic_fa_link,
            typeText = stringResource(R.string.link),
            typeDescriptionText = stringResource(R.string.list_url_description),
            selected = bucketType == BucketBoxEncrypted.BucketType.Link,
            onClick = { onClickBucketCard(BucketBoxEncrypted.BucketType.Link) },
        )
        Spacer(modifier = Modifier.width(width = 12.dp))
        BucketCard(
            icon = R.drawable.ic_fa_map_pin,
            typeText = stringResource(R.string.location),
            typeDescriptionText = stringResource(R.string.list_location_description),
            selected = bucketType == BucketBoxEncrypted.BucketType.Location,
            onClick = { onClickBucketCard(BucketBoxEncrypted.BucketType.Location) },
        )
    }
}

@Composable
private fun BucketCard(
    icon: Int,
    typeText: String,
    typeDescriptionText: String,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {

    val containerColor by animateColorAsState(targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background, animationSpec = AnimationDefaults.stateAnimationSpec())
    val contentColor by animateColorAsState(targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground, animationSpec = AnimationDefaults.stateAnimationSpec())

    Column(
        modifier = Modifier.width(width = 194.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
            border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
            shape = MaterialTheme.shapes.large,
            onClick = onClick,
            modifier = Modifier
                .width(width = 194.dp)
                .aspectRatio(ratio = 1.666f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 16.dp)
            ) {
                Icon(painter = painterResource(id = icon), contentDescription = typeText, modifier = Modifier.size(size = 20.dp))
                Spacer(modifier = Modifier.weight(weight = 1f))
                Text(text = typeText)
            }
        }
        Spacer(modifier = Modifier.height(height = 6.dp))
        Text(text = typeDescriptionText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 6.dp))
    }

}
