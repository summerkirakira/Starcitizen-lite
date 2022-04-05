package vip.kirakira.starcitizenlite.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import vip.kirakira.starcitizenlite.R

@BindingAdapter("android:imageUrl")
fun loadImage(imgView: ImageView, url: String?) {
    var imageUrl = url
    if (url!!.startsWith("/")) {
        imageUrl = "https://robertsspaceindustries.com" + url
    }
    Glide.with(imgView.context)
        .load(imageUrl).apply(
            RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(RoundedCorners(8))
        )
        .into(imgView)
}