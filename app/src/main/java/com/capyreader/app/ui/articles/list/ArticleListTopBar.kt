package com.capyreader.app.ui.articles.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewHeadline
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleListLayout
import org.koin.compose.koinInject
import com.capyreader.app.ui.articles.FilterActionMenu
import com.capyreader.app.ui.articles.FilterAppBarTitle
import com.capyreader.app.ui.articles.listPaneInsets
import com.capyreader.app.ui.components.ArticleSearch
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.accounts.Source

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListTopBar(
    onRequestJumpToTop: () -> Unit,
    onNavigateToDrawer: () -> Unit,
    onRemoveFolder: (folderTitle: String, completion: (result: Result<Unit>) -> Unit) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    search: ArticleSearch,
    filter: ArticleFilter,
    currentFeed: Feed?,
    feeds: List<Feed>,
    savedSearches: List<SavedSearch>,
    folders: List<Folder>,
    source: Source,
) {
    // Search runs in its own full-surface overlay (see SearchView), so the list top bar only needs
    // to launch it; the active-search field/back-arrow used to live here.
    TopAppBar(
        scrollBehavior = scrollBehavior,
        windowInsets = TopAppBarDefaults.windowInsets.listPaneInsets(),
        title = {
            FilterAppBarTitle(
                filter = filter,
                allFeeds = feeds,
                allFolders = folders,
                allSavedSearches = savedSearches,
                onRequestJumpToTop = onRequestJumpToTop
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateToDrawer
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = stringResource(R.string.feed_nav_drawer_open)
                )
            }
        },
        actions = {
            LayoutToggleButton()
            FilterActionMenu(
                filter = filter,
                currentFeed = currentFeed,
                onRemoveFolder = onRemoveFolder,
                onRequestSearch = { search.start() },
                hideSearchIcon = false,
                source = source,
            )
        }
    )
}

@Composable
private fun LayoutToggleButton(appPreferences: AppPreferences = koinInject()) {
    val scope = rememberCoroutineScope()
    val preference = appPreferences.articleListOptions.layout
    val layout by preference.stateIn(scope).collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = layout.icon,
                contentDescription = stringResource(R.string.article_list_layout_menu)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ArticleListLayout.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.translationKey)) },
                    leadingIcon = { Icon(imageVector = option.icon, contentDescription = null) },
                    trailingIcon = if (option == layout) {
                        { Icon(imageVector = Icons.Rounded.Check, contentDescription = null) }
                    } else {
                        null
                    },
                    onClick = {
                        preference.set(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private val ArticleListLayout.icon: ImageVector
    get() = when (this) {
        ArticleListLayout.LIST -> Icons.Rounded.ViewAgenda
        ArticleListLayout.COMPACT -> Icons.Rounded.ViewHeadline
        ArticleListLayout.MAGAZINE -> Icons.Rounded.Dashboard
    }

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun FeedListTopBarPreview() {
    val scrollBehavior = pinnedScrollBehavior()
    ArticleListTopBar(
        onRequestJumpToTop = { },
        onNavigateToDrawer = { },
        onRemoveFolder = { _, _ -> },
        scrollBehavior = scrollBehavior,
        search = ArticleSearch(),
        filter = ArticleFilter.default(),
        currentFeed = null,
        feeds = listOf(),
        savedSearches = emptyList(),
        folders = emptyList(),
        source = Source.LOCAL,
    )
}
