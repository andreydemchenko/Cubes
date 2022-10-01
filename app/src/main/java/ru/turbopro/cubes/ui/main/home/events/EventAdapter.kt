package ru.turbopro.cubes.ui.main.home.events

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.turbopro.cubes.data.Event
import ru.turbopro.cubes.data.CubsAppSessionManager
import ru.turbopro.cubes.databinding.EventListItemBinding
import ru.turbopro.cubes.databinding.LayoutHomeAdBinding

class EventAdapter(evList: List<Any>, private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data = evList

    lateinit var onClickListener: OnClickListener
    private val sessionManager = CubsAppSessionManager(context)

    inner class ItemViewHolder(binding: EventListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val evCard = binding.eventCard
        private val evName = binding.eventTitleTv
        private val eventImage = binding.eventImageView
        private val evDeleteButton = binding.eventDeleteButton
        private val evEditBtn = binding.eventEditButton

        fun bind(eventData: Event) {
            evCard.setOnClickListener{
                onClickListener.onClick(eventData)
            }
            evName.text = eventData.name
            if (eventData.images.isNotEmpty()) {
                val imgUrl = eventData.images[0].toUri().buildUpon().scheme("https").build()
                Glide.with(context)
                    .asBitmap()
                    .load(imgUrl)
                    .into(eventImage)

                eventImage.clipToOutline = true
            }

            if (sessionManager.isUserSeller()) {
                evEditBtn.setOnClickListener {
                    onClickListener.onEditClick(eventData.eventId)
                }

                evDeleteButton.setOnClickListener {
                    onClickListener.onDeleteClick(eventData)
                }
            } else {
                evEditBtn.visibility = View.GONE
                evDeleteButton.visibility = View.GONE
            }
        }
    }

    inner class AdViewHolder(binding: LayoutHomeAdBinding) : RecyclerView.ViewHolder(binding.root) {
        val adImageView: ImageView = binding.adImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> AdViewHolder(
                LayoutHomeAdBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ItemViewHolder(
                EventListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val achData = data[position]) {
            is Int -> (holder as AdViewHolder).adImageView.setImageResource(achData)
            is Event -> (holder as ItemViewHolder).bind(achData)
        }
    }

    override fun getItemCount(): Int = data.size

    companion object {
        const val VIEW_TYPE_EVENT = 1
        const val VIEW_TYPE_AD = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is Int -> VIEW_TYPE_AD
            is Event -> VIEW_TYPE_EVENT
            else -> VIEW_TYPE_EVENT
        }
    }

    interface OnClickListener {
        fun onClick(eventData: Event)
        fun onDeleteClick(eventData: Event)
        fun onEditClick(eventId: String) {}
    }
}