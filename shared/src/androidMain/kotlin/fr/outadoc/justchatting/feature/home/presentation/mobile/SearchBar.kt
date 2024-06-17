package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onChannelClick: (String) -> Unit,
    sizeClass: WindowSizeClass,
) {
    // Talkback focus order sorts based on x and y position before considering z-index. The
    // extra Box with semantics and fillMaxWidth is a workaround to get the search bar to focus
    // before the content.
    Box(
        modifier
            .semantics { isTraversalGroup = true }
            .zIndex(1f)
            .fillMaxWidth(),
    ) {
        SearchScreenBar(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth(),
            onChannelClick = onChannelClick,
            sizeClass = sizeClass,
        )
    }
}
