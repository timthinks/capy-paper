package com.capyreader.app.ui.articles

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.capyreader.app.R
import com.capyreader.app.ui.articles.list.ArticleActionMenu
import com.capyreader.app.ui.components.LocalSnackbarHost
import com.capyreader.app.ui.theme.ReaderSerif
import com.jocmp.capy.Article
import com.jocmp.capy.EnclosureType
import com.jocmp.capy.MarkRead
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Card sizes for the magazine layout. The size is picked from what the article
 * actually has (lead image, summary length), so a feed of photo posts and a feed
 * of text essays lay out differently.
 */
enum class MagazineCardSize {
    /** Full-width hero: lead image plus a substantial summary. */
    FEATURE,

    /** Image on top with a few lines of summary. */
    LARGE,

    /** Image on top, headline only. */
    PHOTO,

    /** No image but a long summary: a text-forward card. */
    TEXT,

    /** Headline only. */
    BRIEF,
}

private const val LONG_SUMMARY = 160
private const val SHORT_SUMMARY = 40

/** At most one full-width feature per block of this many articles. */
private const val FEATURE_BLOCK = 6

fun Article.magazineCardSize(): MagazineCardSize {
    val summaryLength = summary.trim().length
    val hasImage = !imageURL.isNullOrBlank()

    return when {
        hasImage && summaryLength >= LONG_SUMMARY -> MagazineCardSize.FEATURE
        hasImage && summaryLength >= SHORT_SUMMARY -> MagazineCardSize.LARGE
        hasImage -> MagazineCardSize.PHOTO
        summaryLength >= LONG_SUMMARY -> MagazineCardSize.TEXT
        else -> MagazineCardSize.BRIEF
    }
}

/**
 * A feature-eligible article only spans the full width if it is the first eligible
 * article in its block, so the grid keeps a magazine rhythm instead of collapsing
 * into a list when every post in a feed has a big image.
 */
private fun resolveCardSize(articles: LazyPagingItems<Article>, index: Int, article: Article): MagazineCardSize {
    val size = article.magazineCardSize()

    if (size != MagazineCardSize.FEATURE) {
        return size
    }

    val blockStart = index - (index % FEATURE_BLOCK)
    val earlierFeature = (blockStart until index).any { i ->
        articles.peek(i)?.magazineCardSize() == MagazineCardSize.FEATURE
    }

    return if (earlierFeature) MagazineCardSize.LARGE else MagazineCardSize.FEATURE
}

@Composable
fun MagazineGrid(
    articles: LazyPagingItems<Article>,
    gridState: LazyStaggeredGridState,
    selectedArticleKey: String?,
    onSelect: (article: Article) -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit,
    dimReadArticles: Boolean,
    contentPadding: PaddingValues,
) {
    val currentTime = rememberCurrentTime()

    LaunchedEffect(selectedArticleKey, articles.itemCount) {
        val id = selectedArticleKey ?: return@LaunchedEffect
        val index = articles.itemSnapshotList.indexOfFirst { it?.id == id }
        if (index > -1 && gridState.layoutInfo.visibleItemsInfo.none { it.index == index }) {
            gridState.animateScrollToItem(index)
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val columns = magazineColumnCount(maxWidth)

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(columns),
            state = gridState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 120.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalItemSpacing = 14.dp,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                count = articles.itemCount,
                key = articles.itemKey { it.id },
                span = { index ->
                    val article = articles.peek(index)
                    if (article != null && resolveCardSize(articles, index, article) == MagazineCardSize.FEATURE) {
                        StaggeredGridItemSpan.FullLine
                    } else {
                        StaggeredGridItemSpan.SingleLane
                    }
                },
            ) { index ->
                val article = articles[index]

                if (article == null) {
                    PlaceholderCard()
                } else {
                    MagazineCard(
                        article = article,
                        index = index,
                        size = resolveCardSize(articles, index, article),
                        selected = selectedArticleKey == article.id,
                        dim = dimReadArticles && article.read,
                        currentTime = currentTime,
                        onSelect = { onSelect(article) },
                        onMarkAllRead = onMarkAllRead,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

/**
 * Two columns in a narrow list pane or a portrait 8" tablet (~600dp), three on a
 * full landscape screen (~960dp), up to four on anything wider.
 */
fun magazineColumnCount(width: Dp): Int {
    return (width / TARGET_CARD_WIDTH).toInt().coerceIn(minimumValue = if (width >= 360.dp) 2 else 1, maximumValue = 4)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MagazineCard(
    article: Article,
    index: Int,
    size: MagazineCardSize,
    selected: Boolean,
    dim: Boolean,
    currentTime: LocalDateTime,
    onSelect: () -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val menuOpen = remember { mutableStateOf(false) }
    val labelsActions = LocalLabelsActions.current
    val articleActions = LocalArticleActions.current
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val savedForLaterMessage = stringResource(R.string.saved_for_later_success)
    val shape = RoundedCornerShape(CARD_CORNER)

    Box(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colorScheme.surfaceContainerLow)
                .then(
                    if (selected) Modifier.border(2.dp, colorScheme.primary, shape) else Modifier
                )
                .combinedClickable(
                    onClick = onSelect,
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuOpen.value = true
                    },
                    onLongClickLabel = stringResource(R.string.article_actions_open_menu),
                )
                .alpha(if (dim) READ_ALPHA else 1f)
        ) {
            when (size) {
                MagazineCardSize.FEATURE -> FeatureCardContent(article, currentTime)
                else -> StackedCardContent(article, size, currentTime)
            }
        }

        ArticleActionMenu(
            expanded = menuOpen.value,
            article = article,
            index = index,
            showLabels = labelsActions.showLabels,
            showSaveForLater = articleActions.showSaveForLater,
            onMarkAllRead = {
                menuOpen.value = false
                onMarkAllRead(it)
            },
            onOpenLabels = {
                menuOpen.value = false
                labelsActions.openSheet(article.id)
            },
            onSaveForLater = { url ->
                menuOpen.value = false
                articleActions.saveForLater(url) { result ->
                    if (result.isSuccess) {
                        scope.launch { snackbarHost.showSnackbar(savedForLaterMessage) }
                    }
                }
            },
            onDismissRequest = { menuOpen.value = false },
        )
    }
}

/** Base text style that keeps right-to-left feeds laid out correctly. */
@Composable
private fun cardTextStyle(style: TextStyle): TextStyle {
    return LocalTextStyle.current.merge(style).copy(textDirection = TextDirection.Content)
}

@Composable
private fun CardTitle(text: String, style: TextStyle, maxLines: Int) {
    if (text.isBlank()) return

    Text(
        text = text,
        style = cardTextStyle(style).copy(fontWeight = FontWeight.SemiBold),
        color = colorScheme.onSurface,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun CardSummary(text: String, maxLines: Int, fontSize: TextUnit = 15.sp) {
    if (text.isBlank() || maxLines == 0) return

    Text(
        text = text,
        style = cardTextStyle(MaterialTheme.typography.bodyMedium).copy(
            fontFamily = ReaderSerif,
            fontSize = fontSize,
            lineHeight = fontSize * 1.42f,
        ),
        color = colorScheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun FeatureCardContent(article: Article, currentTime: LocalDateTime) {
    BoxWithConstraints {
        if (maxWidth >= WIDE_FEATURE_WIDTH) {
            // Wide screens: image beside the text so the hero doesn't fill the viewport.
            Row(Modifier.height(WIDE_FEATURE_HEIGHT)) {
                CardImage(
                    url = article.imageURL.orEmpty(),
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                ) {
                    CardSourceLine(article, currentTime)
                    CardTitle(
                        text = article.title,
                        style = MaterialTheme.typography.headlineSmall.copy(lineHeight = 30.sp),
                        maxLines = 4,
                    )
                    CardSummary(text = article.summary, maxLines = 5, fontSize = 16.sp)
                }
            }
        } else {
            Column {
                CardImage(
                    url = article.imageURL.orEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    CardSourceLine(article, currentTime)
                    CardTitle(
                        text = article.title,
                        style = MaterialTheme.typography.headlineSmall.copy(lineHeight = 30.sp),
                        maxLines = 3,
                    )
                    CardSummary(text = article.summary, maxLines = 3, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun StackedCardContent(
    article: Article,
    size: MagazineCardSize,
    currentTime: LocalDateTime,
) {
    val imageURL = article.imageURL

    if (!imageURL.isNullOrBlank() && (size == MagazineCardSize.LARGE || size == MagazineCardSize.PHOTO)) {
        CardImage(
            url = imageURL,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (size == MagazineCardSize.LARGE) 4 / 3f else 16 / 10f),
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        CardSourceLine(article, currentTime)
        CardTitle(
            text = article.title,
            style = when (size) {
                MagazineCardSize.TEXT -> MaterialTheme.typography.titleLarge
                else -> MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, lineHeight = 23.sp)
            },
            maxLines = if (size == MagazineCardSize.BRIEF) 5 else 4,
        )
        CardSummary(
            text = article.summary,
            maxLines = when (size) {
                MagazineCardSize.TEXT -> 8
                MagazineCardSize.LARGE -> 3
                MagazineCardSize.BRIEF -> 2
                else -> 0
            },
        )
    }
}

@Composable
private fun CardSourceLine(article: Article, currentTime: LocalDateTime) {
    val color = colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        FaviconBadge(article.faviconURL, size = 12.dp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = article.displayFeedName(LocalContext.current).uppercase(),
            style = cardTextStyle(MaterialTheme.typography.labelSmall).copy(letterSpacing = 1.1.sp),
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Text(
            text = "  ·  " + riverTime(publishedAt = article.publishedAt, currentTime = currentTime),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
        )
        Spacer(Modifier.weight(1f))
        if (article.enclosureType == EnclosureType.AUDIO) {
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = color,
                modifier = Modifier.width(14.dp),
            )
        }
        if (article.starred) {
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.width(14.dp),
            )
        }
    }
}

@Composable
private fun CardImage(url: String, modifier: Modifier) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(colorScheme.surfaceContainerHigh),
    )
}

@Composable
private fun PlaceholderCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CARD_CORNER))
            .background(colorScheme.surfaceContainerLow)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth(0.4f)
                .height(10.dp)
                .clip(MaterialTheme.shapes.small)
                .background(colorScheme.surfaceContainerHigh)
        )
        Box(
            Modifier
                .fillMaxWidth(0.85f)
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
                .background(colorScheme.surfaceContainerHigh)
        )
        Box(
            Modifier
                .fillMaxWidth(0.6f)
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
                .background(colorScheme.surfaceContainerHigh)
        )
    }
}

private val TARGET_CARD_WIDTH = 280.dp
private val WIDE_FEATURE_WIDTH = 640.dp
private val WIDE_FEATURE_HEIGHT = 320.dp
private val CARD_CORNER = 18.dp
private const val READ_ALPHA = 0.55f
