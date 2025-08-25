package com.aar.app.wsp.features.gamehistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aar.app.wsp.R
import com.aar.app.wsp.commons.DurationFormatter.fromInteger
import com.aar.app.wsp.custom.easyadapter.AdapterDelegate
import com.aar.app.wsp.databinding.ItemGameDataHistoryBinding
import com.aar.app.wsp.model.GameDataInfo

class GameDataAdapterDelegate :
    AdapterDelegate<GameDataInfo, GameDataAdapterDelegate.ViewHolder>(GameDataInfo::class.java) {

    var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_data_history, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(model: GameDataInfo, holder: ViewHolder) {
        ItemGameDataHistoryBinding.bind(holder.itemView).apply {
            textName.text = model.name
            textOverallDuration.text = fromInteger(model.duration)

            var desc = this.root.context.getString(R.string.game_data_desc)
            desc =
                desc.replace(":gridSize".toRegex(), "${model.gridRowCount}x${model.gridColCount}")
            desc = desc.replace(":wordCount".toRegex(), model.usedWordsCount.toString())
            textDesc.text = desc

            this.root.setOnClickListener {
                onClickListener?.onClick(model)
            }
            deleteListItem.setOnClickListener {
                onClickListener?.onDeleteClick(model)
            }
        }

    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnClickListener {
        fun onClick(gameDataInfo: GameDataInfo?)
        fun onDeleteClick(gameDataInfo: GameDataInfo?)
    }
}