package fr.outadoc.justchatting.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.utils.ui.HapticIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit
) {
    val focusRequester = FocusRequester()
    TextField(
        modifier = modifier
            .statusBarsPadding()
            .padding(16.dp)
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        ),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
            autoCorrect = false
        ),
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotEmpty()) {
                HapticIconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = stringResource(R.string.search_clear_cd)
                    )
                }
            }
        }
    )

    LaunchedEffect(onQueryChange) {
        focusRequester.requestFocus()
    }
}
