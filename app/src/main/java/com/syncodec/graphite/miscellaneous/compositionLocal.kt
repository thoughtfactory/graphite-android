package com.syncodec.graphite.miscellaneous

import androidx.compose.runtime.compositionLocalOf
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.custom.richText.RichTextEditor


val LocalVaultState =
	compositionLocalOf<Graphite.Companion.VaultState> { error("Vault state unavailable...") }
//val LocalFilterState = compositionLocalOf<Filter> { error("Filter state unavailable...") }
val LocalSelectedState = compositionLocalOf<Boolean> { error("Selected state unavailable...") }
val LocalRichTextEditor = compositionLocalOf<RichTextEditor> { error("Rich Text Editor unavailable...") }
