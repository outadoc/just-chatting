package fr.outadoc.justchatting.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.HapticIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            var showOverflow by remember { mutableStateOf(false) }

            HapticIconButton(onClick = { onSearchClick() }) {
                Icon(
                    modifier = Modifier.padding(bottom = 3.dp),
                    imageVector = Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }

            IconButton(onClick = { showOverflow = !showOverflow }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.menu_item_showOverflow)
                )
            }

            DropdownMenu(
                expanded = showOverflow,
                onDismissRequest = { showOverflow = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.settings)) },
                    onClick = {
                        onSettingsClick()
                        showOverflow = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.log_out)) },
                    onClick = {
                        onLogoutClick()
                        showOverflow = false
                    }
                )
            }
        }
    )
}
