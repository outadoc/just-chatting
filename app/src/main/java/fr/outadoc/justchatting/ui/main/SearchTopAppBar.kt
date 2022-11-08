package fr.outadoc.justchatting.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.HapticIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = modifier
            .statusBarsPadding()
            .height(72.dp)
    ) {
        TextField(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            placeholder = {
                Text(stringResource(R.string.search))
            },
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
