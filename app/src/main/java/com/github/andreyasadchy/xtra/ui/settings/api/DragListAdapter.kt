/*
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.andreyasadchy.xtra.ui.settings.api

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.Pair
import com.github.andreyasadchy.xtra.R
import com.woxthebox.draglistview.DragItemAdapter

internal class DragListAdapter(
    list: ArrayList<Pair<Long?, String?>?>?,
    private val mLayoutId: Int,
    private val mGrabHandleId: Int,
    private val mDragOnLongPress: Boolean
) : DragItemAdapter<Pair<Long?, String?>?, DragListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(mLayoutId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val text = mItemList[position]!!.second!!
        holder.mText.text = text
        holder.itemView.tag = mItemList[position]
    }

    override fun getUniqueItemId(position: Int): Long {
        return mItemList[position]!!.first!!
    }

    internal inner class ViewHolder(itemView: View) : DragItemAdapter.ViewHolder(itemView, mGrabHandleId, mDragOnLongPress) {
        var mText: TextView = itemView.findViewById<View>(R.id.text) as TextView
        override fun onItemClicked(view: View) {}
        override fun onItemLongClicked(view: View): Boolean {
            return true
        }
    }

    init {
        itemList = list
    }
}