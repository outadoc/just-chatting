package fr.outadoc.justchatting.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.HapticIconButton
import fr.outadoc.justchatting.ui.chat.ExpandedTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit
) {
    ExpandedTopAppBar(
        modifier = modifier.statusBarsPadding(),
        title = { Text(stringResource(R.string.search)) }
    ) {
        TextField(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            trailingIcon = {
                AnimatedVisibility(visible = query.isNotEmpty()) {
                    HapticIconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Filled.Cancel,
                            contentDescription = "Clear query"
                        )
                    }
                }
            }
        )
    }
}
