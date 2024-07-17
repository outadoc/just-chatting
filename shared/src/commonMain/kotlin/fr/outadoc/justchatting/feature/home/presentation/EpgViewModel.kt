package fr.outadoc.justchatting.feature.home.presentation

import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.utils.presentation.ViewModel

internal class EpgViewModel(
    private val twitchRepository: TwitchRepository,
) : ViewModel()