package com.cococue.fakecallfootballplayer.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cococue.fakecallfootballplayer.R
import com.cococue.fakecallfootballplayer.databinding.ItemPlayerBinding
import com.cococue.fakecallfootballplayer.model.FootballPlayer

class PlayerAdapter(
    private val players: List<FootballPlayer>,
    private val onPlayerSelected: (FootballPlayer) -> Unit,
    private val onAddCustomPlayerClicked: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PLAYER = 0
        private const val TYPE_ADD = 1
    }

    var selectedPosition = 0

    class PlayerViewHolder(val binding: ItemPlayerBinding) : RecyclerView.ViewHolder(binding.root)
    class AddViewHolder(val binding: ItemPlayerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (position < players.size) TYPE_PLAYER else TYPE_ADD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (viewType == TYPE_PLAYER) {
            PlayerViewHolder(binding)
        } else {
            AddViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_PLAYER) {
            val playerHolder = holder as PlayerViewHolder
            val player = players[position]
            playerHolder.binding.tvName.text = player.name

            if (player.customAvatarUri != null) {
                try {
                    playerHolder.binding.imgAvatar.setImageURI(Uri.parse(player.customAvatarUri))
                } catch (e: Exception) {
                    playerHolder.binding.imgAvatar.setImageResource(player.avatarRes)
                }
            } else {
                playerHolder.binding.imgAvatar.setImageResource(player.avatarRes)
            }

            val context = holder.itemView.context
            if (position == selectedPosition) {
                playerHolder.binding.cardPlayer.strokeWidth = 6
                playerHolder.binding.cardPlayer.strokeColor = ContextCompat.getColor(context, R.color.primary_accent)
            } else {
                playerHolder.binding.cardPlayer.strokeWidth = 0
            }

            holder.itemView.setOnClickListener {
                val prevPos = selectedPosition
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    selectedPosition = currentPos
                    notifyItemChanged(prevPos)
                    notifyItemChanged(selectedPosition)
                    onPlayerSelected(player)
                }
            }
        } else {
            val addHolder = holder as AddViewHolder
            addHolder.binding.tvName.text = holder.itemView.context.getString(R.string.add_custom_player)
            addHolder.binding.imgAvatar.setImageResource(R.drawable.ic_add_star)
            addHolder.binding.cardPlayer.strokeWidth = 2
            addHolder.binding.cardPlayer.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.primary_accent)

            holder.itemView.setOnClickListener {
                onAddCustomPlayerClicked()
            }
        }
    }

    override fun getItemCount(): Int = players.size + 1
}
