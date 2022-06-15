/*
  Copyright 2014 Magnus Woxblom
  <p/>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p/>
  http://www.apache.org/licenses/LICENSE-2.0
  <p/>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.github.andreyasadchy.xtra.ui.settings.api

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs
import com.woxthebox.draglistview.DragListView

class DragListFragment : Fragment() {

    lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = requireActivity().prefs()

        val view = inflater.inflate(R.layout.drag_list_layout, container, false)

        val views =
            mutableListOf<Pair<DragListView, Pair<String, ArrayList<Pair<Long?, String?>?>>>>()

        view.findViewById<TextView>(R.id.apiSettingsText8).text =
            requireContext().getString(R.string.search_channels)

        views.add(
            Pair(
                view.findViewById(R.id.apiSettingsList8),
                Pair(C.API_PREF_SEARCH_CHANNEL, TwitchApiHelper.searchChannelsApiDefaults)
            )
        )

        view.findViewById<TextView>(R.id.apiSettingsText10).text =
            requireContext().getString(R.string.followed_streams)

        views.add(
            Pair(
                view.findViewById(R.id.apiSettingsList10),
                Pair(C.API_PREF_FOLLOWED_STREAMS, TwitchApiHelper.followedStreamsApiDefaults)
            )
        )

        view.findViewById<TextView>(R.id.apiSettingsText12).text =
            requireContext().getString(R.string.followed_channels)

        views.add(
            Pair(
                view.findViewById(R.id.apiSettingsList12),
                Pair(C.API_PREF_FOLLOWED_CHANNELS, TwitchApiHelper.followedChannelsApiDefaults)
            )
        )

        for (i in views) {
            setupListRecyclerView(i.first, i.second)
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.apiSettingsContainer, ApiSettingsFragment())
            .commit()

        return view
    }

    private fun setupListRecyclerView(
        view: DragListView,
        items: Pair<String, ArrayList<Pair<Long?, String?>?>>
    ) {
        val list = TwitchApiHelper.listFromPrefs(prefs.getString(items.first, ""), items.second)
        view.setLayoutManager(LinearLayoutManager(context))
        val listAdapter = DragListAdapter(list, R.layout.drag_list_item, R.id.image, false)
        view.setAdapter(listAdapter, true)
        view.setCanDragHorizontally(false)
        view.setCanDragVertically(true)
        view.setDragListListener(object : DragListView.DragListListenerAdapter() {
            override fun onItemDragStarted(position: Int) {}

            override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
                if (fromPosition != toPosition) {
                    var str = ""
                    view.adapter.itemList.forEachIndexed { index, item ->
                        str = "$str${index.toLong()}:${(item as Pair<Long, String>).second},"
                    }
                    prefs.edit { putString(items.first, str) }
                }
            }
        })
    }

    companion object {
        fun newInstance(): DragListFragment {
            return DragListFragment()
        }
    }

    class ApiSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.api_preferences, rootKey)
        }
    }
}
