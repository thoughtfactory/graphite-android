package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock

import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.state.ToggleableState
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted


@Composable
fun StateViewButton(
    state: BucketItemBoxDecrypted.State,
    onClickStateButton: () -> Unit
) {

    val triState by remember(key1 = state) {
        derivedStateOf {
            when (state) {
                BucketItemBoxDecrypted.State.Alpha -> ToggleableState.Off
                BucketItemBoxDecrypted.State.Beta -> ToggleableState.Indeterminate
                BucketItemBoxDecrypted.State.Gamma -> ToggleableState.On
            }
        }
    }

    TriStateCheckbox(
        state = triState,
        onClick = onClickStateButton
    )
}
