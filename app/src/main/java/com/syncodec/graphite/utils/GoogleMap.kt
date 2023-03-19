package com.syncodec.graphite.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.WorkerThread
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
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObjectLite
import java.io.IOException
import java.util.Locale


fun GoogleMap.isMarkerVisible(markerPosition : LatLng?) = markerPosition?.let { projection.visibleRegion.latLngBounds.contains(it) } ?: false


class AtlasClusterItem(
	val latLng : LatLng,
	val itemTitle : String?,
) : ClusterItem {
	override fun getPosition() : LatLng = latLng
	override fun getTitle() : String? = itemTitle
	override fun getSnippet() : String? = null
}

class AtlasNoteClusterItem(
	val note : NoteObjectLite,
	val latLng : LatLng,
	val itemTitle : String?,
) : ClusterItem {
	override fun getPosition() : LatLng = latLng
	override fun getTitle() : String? = itemTitle
	override fun getSnippet() : String? = null
}


class ClusterRenderer<T : ClusterItem>(
	val context : Context,
	map : GoogleMap,
	clusterManager : ClusterManager<T>
) : DefaultClusterRenderer<T>(context, map, clusterManager) {
	override fun onBeforeClusterRendered(cluster : Cluster<T>, markerOptions : MarkerOptions) {
		val atlasItem = AtlasItem(context = context, itemSize = cluster.size)
		val bitmap = createBitmapFromView(atlasItem, 144, 144)
		markerOptions.title("").icon(BitmapDescriptorFactory.fromBitmap(bitmap))
	}

	override fun onBeforeClusterItemRendered(item : T, markerOptions : MarkerOptions) {
		val atlasItem = AtlasItem(context = context, itemSize = 1)
		val bitmap = createBitmapFromView(atlasItem, 144, 144)
		markerOptions.title("").icon(BitmapDescriptorFactory.fromBitmap(bitmap))
	}

	override fun onClusterUpdated(cluster : Cluster<T>, marker : Marker) {
		val atlasItem = AtlasItem(context = context, itemSize = cluster.size)
		val bitmap = createBitmapFromView(atlasItem, 144, 144)
		marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
	}

//	override fun onClusterItemUpdated(item: T, marker: Marker) {
//		super.onClusterItemUpdated(item, marker)
//
//		val atlasItem = AtlasItem(context = context, itemSize = 1)
//		val bitmap = createBitmapFromView(atlasItem, 128, 128)
//		marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
//	}

	override fun shouldRenderAsCluster(cluster : Cluster<T>) : Boolean = cluster.size > 1
}

class AtlasItem(context : Context, val itemSize : Int) : FrameLayout(context) {
	init {
		layoutParams = LayoutParams(144, 144)

		addView(
			RelativeLayout(context).apply {
				layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
					this.gravity = Gravity.CENTER
				}

				addView(
					ImageView(context).apply {
						layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
						setImageResource(R.drawable.ic_note_cluster)
					}
				)

				if (itemSize != 1) {
					addView(
						TextView(context).apply {
							layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
								this.gravity = Gravity.CENTER
							}
							setPadding(8, 8, 8, 8)
							text = "$itemSize"
							textSize = 12f
							setTypeface(ResourcesCompat.getFont(context, R.font.ubuntu_bold), Typeface.BOLD)
							setTextColor(Color.BLACK)
							setBackgroundColor(Color.WHITE)
						}
					)
				}
			}
		)
	}
}

@WorkerThread
fun Context.reverseGeocode(
	latitude : Double,
	longitude : Double,
	onAddressAvailable : (Address?) -> Unit,
	onIoException : () -> Unit = {},
	onException : () -> Unit = {}
) {
	try {
		val geocoder = Geocoder(this, Locale.getDefault())
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
				onAddressAvailable(addresses.getOrNull(0))
			}
		} else {
//			Deprecation is handled in upper block
			val addresses = geocoder.getFromLocation(latitude, longitude, 1)
			onAddressAvailable(addresses?.firstOrNull())
		}

	} catch (exception : IOException) {
		onIoException()
	} catch (exception : Exception) {
		onException()
	}
}
