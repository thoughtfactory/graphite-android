package com.syncodec.graphite.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun Modifier.onlyIfComposable(modifier: @Composable Modifier.() -> Modifier, predicate: () -> Boolean): Modifier = if (predicate()) this.modifier() else this

fun Modifier.onlyIf(predicate: () -> Boolean, modifier: Modifier.() -> Modifier): Modifier = if (predicate()) this.modifier() else this

fun Modifier.onlyIf(predicate: Boolean, modifier: Modifier.() -> Modifier): Modifier = if (predicate) this.modifier() else this
