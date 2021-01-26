package com.app.lostpetapp.util

import android.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.app.lostpetapp.ui.MapFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator


class MarkerClusterRenderer(
    private val context: Context, map: GoogleMap,
    clusterManager: ClusterManager<MapFragment.MyItem>
) : DefaultClusterRenderer<MapFragment.MyItem>(
    context, map,
    clusterManager
) {

    private val iconGenerator = IconGenerator(context)
    private val imageView = ImageView(context)
    private lateinit var icon: Bitmap

    init {
        imageView.layoutParams = ViewGroup.LayoutParams(120, 80)
        imageView.setPadding(2, 0, 2, 0)
        iconGenerator.setContentView(imageView)
    }

    override fun onBeforeClusterItemRendered(
        item: MapFragment.MyItem,
        markerOptions: MarkerOptions
    ) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        icon = iconGenerator.makeIcon()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onClusterItemRendered(clusterItem: MapFragment.MyItem, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)

        Glide.with(context)
            .asBitmap()
            .load(clusterItem.url)
            .dontTransform()

            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)
                    icon = iconGenerator.makeIcon()
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
                }
            })
    }
}