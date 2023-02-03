package com.syncodec.graphite.presentation.common.scaffold


data class GenericButton(
	val text : String?,
	val icon : Int,
	val onClick : () -> Unit
)
