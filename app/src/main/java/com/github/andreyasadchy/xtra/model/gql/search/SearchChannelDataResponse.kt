package com.github.andreyasadchy.xtra.model.gql.search

import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch

data class SearchChannelDataResponse(val data: List<ChannelSearch>, val cursor: String?)