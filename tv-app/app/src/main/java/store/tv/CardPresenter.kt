package store.tv

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide

class CardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        // Ukuran card TV: 320 x 180
        cardView.setMainImageDimensions(320, 180)
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val app = item as AppModel
        val cardView = viewHolder.view as ImageCardView
        cardView.titleText = app.nama
        cardView.contentText = "${app.versi} • ${app.ukuran} • ${app.deskripsi}"
        Glide.with(cardView.context)
            .load(app.icon)
            .centerCrop()
            .into(cardView.mainImageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = null
    }
}
