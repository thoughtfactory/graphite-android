package com.syncodec.momento.mainComponent.miscellaneous

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.syncodec.momento.R
import com.syncodec.momento.miscellaneous.GraphicUtils


class AtlasClusterItem(
	val latLng: LatLng,
	val itemTitle: String?,
) : ClusterItem {
	override fun getPosition(): LatLng = latLng
	override fun getTitle(): String? = itemTitle
	override fun getSnippet(): String? = null
}

class ClusterRenderer<T : ClusterItem>(
	val context: Context,
	map: GoogleMap,
	clusterManager: ClusterManager<T>
) : DefaultClusterRenderer<T>(context, map, clusterManager) {
	override fun onBeforeClusterRendered(cluster: Cluster<T>, markerOptions: MarkerOptions) {
		super.onBeforeClusterRendered(cluster, markerOptions)

		val atlasItem = AtlasItem(context = context, itemSize = cluster.size)
		val bitmap = GraphicUtils.createBitmapFromView(atlasItem, 128, 128)
		markerOptions.title("").icon(BitmapDescriptorFactory.fromBitmap(bitmap))
	}

	override fun onBeforeClusterItemRendered(item: T, markerOptions: MarkerOptions) {
		super.onBeforeClusterItemRendered(item, markerOptions)

		val atlasItem = AtlasItem(context = context, itemSize = 1)
		val bitmap = GraphicUtils.createBitmapFromView(atlasItem, 128, 128)
		markerOptions.title("").icon(BitmapDescriptorFactory.fromBitmap(bitmap))
	}

	override fun onClusterUpdated(cluster: Cluster<T>, marker: Marker) {
		super.onClusterUpdated(cluster, marker)

		val atlasItem = AtlasItem(context = context, itemSize = 1)
		val bitmap = GraphicUtils.createBitmapFromView(atlasItem, 128, 128)
		marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
	}

	override fun onClusterItemUpdated(item: T, marker: Marker) {
		super.onClusterItemUpdated(item, marker)

		val atlasItem = AtlasItem(context = context, itemSize = 1)
		val bitmap = GraphicUtils.createBitmapFromView(atlasItem, 128, 128)
		marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
	}
}

class AtlasItem(context: Context, val itemSize: Int) : FrameLayout(context) {
	init {
		layoutParams = LayoutParams(128, 128)
		addView(
			ImageView(context).apply {
				layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
				setImageResource(R.drawable.ic_notes_3)
			}
		)

		if (itemSize != 1) {
			addView(
				TextView(context).apply {
					layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
						this.gravity = Gravity.CENTER
					}
					text = "$itemSize"
					textSize = 12f
					setTypeface(ResourcesCompat.getFont(context, R.font.ubuntu_bold), Typeface.BOLD)
					setTextColor(Color.BLACK)
				}
			)
		}
	}
}
