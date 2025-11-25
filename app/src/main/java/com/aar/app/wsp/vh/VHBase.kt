package com.aar.app.wsp.vh

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.aar.app.wsp.features.gamethemeselector.Listener
import d.d.pvp.model.ListModel

open class VHBase(view: View, protected var listener: Listener) : RecyclerView.ViewHolder(view) {
    open fun bind(folderModel: ListModel, filterText: String?) {

    }
}

