package com.cococue.fakecallfootballplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cococue.fakecallfootballplayer.databinding.ItemWallpaperBinding
import com.cococue.fakecallfootballplayer.model.WallpaperItem

class WallpaperAdapter(
    private val wallpapers: List<WallpaperItem>,
    private val onWallpaperClicked: (WallpaperItem) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder>() {

    class WallpaperViewHolder(val binding: ItemWallpaperBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val binding = ItemWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WallpaperViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        val item = wallpapers[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.imgWallpaper.setImageResource(item.resId)
        holder.binding.tvVideoBadge.visibility = if (item.isVideo) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onWallpaperClicked(item)
        }
    }

    override fun getItemCount(): Int = wallpapers.size
}
