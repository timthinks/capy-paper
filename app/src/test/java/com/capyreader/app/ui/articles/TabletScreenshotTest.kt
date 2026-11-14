package com.capyreader.app.ui.articles

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.theme.CapyTheme
import com.github.takahirom.roborazzi.captureRoboImage
import com.jocmp.capy.Article
import kotlinx.coroutines.flow.flowOf
import org.json.JSONArray
import org.junit.Assume.assumeTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Renders the tablet layouts against a snapshot of real feed items so the design can be
 * reviewed without a device. Fixtures live in `screenshot-fixtures/` and are not committed;
 * the test skips when they are absent.
 */
@OptIn(ExperimentalCoilApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class)
class TabletScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val outputDir = System.getProperty("screenshots.dir") ?: "build/screenshots"

    @Before
    fun setUp() {
        startKoin {
            modules(module { single { AppPreferences(RuntimeEnvironment.getApplication()) } })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    @Config(qualifiers = "w600dp-h960dp-port-tvdpi")
    fun magazinePortraitDark() = capture("magazine-portrait-dark", ThemeMode.DARK) { articles ->
        MagazineFixture(articles)
    }

    @Test
    @Config(qualifiers = "w600dp-h2600dp-port-tvdpi")
    fun magazinePortraitDarkLong() = capture("magazine-portrait-dark-long", ThemeMode.DARK) { articles ->
        MagazineFixture(articles)
    }

    @Test
    @Config(qualifiers = "w960dp-h600dp-land-tvdpi")
    fun magazineLandscapeDark() = capture("magazine-landscape-dark", ThemeMode.DARK) { articles ->
        MagazineFixture(articles)
    }

    @Test
    @Config(qualifiers = "w600dp-h960dp-port-tvdpi")
    fun magazinePortraitLight() = capture("magazine-portrait-light", ThemeMode.LIGHT) { articles ->
        MagazineFixture(articles)
    }

    @Test
    @Config(qualifiers = "w600dp-h960dp-port-tvdpi")
    fun listPortraitDark() = capture("list-portrait-dark", ThemeMode.DARK) { articles ->
        ListFixture(articles)
    }

    @Test
    @Config(qualifiers = "w600dp-h960dp-port-tvdpi")
    fun listPortraitLight() = capture("list-portrait-light", ThemeMode.LIGHT) { articles ->
        ListFixture(articles)
    }

    @Test
    @Config(qualifiers = "w600dp-h960dp-port-tvdpi")
    fun compactPortraitDark() = capture("compact-portrait-dark", ThemeMode.DARK) { articles ->
        ListFixture(articles, compact = true)
    }

    @Test
    @Config(qualifiers = "w600dp-h960dp-port-tvdpi")
    fun compactPortraitLight() = capture("compact-portrait-light", ThemeMode.LIGHT) { articles ->
        ListFixture(articles, compact = true)
    }

    private fun capture(
        name: String,
        themeMode: ThemeMode,
        content: @Composable (List<Article>) -> Unit,
    ) {
        val articles = loadArticles()
        assumeTrue("screenshot fixtures missing", articles.isNotEmpty())

        composeRule.setContent {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
                LocalAsyncImagePreviewHandler provides fixtureImages,
            ) {
                CapyTheme(appTheme = AppTheme.PAPER, themeMode = themeMode) {
                    content(articles)
                }
            }
        }

        composeRule.onRoot().captureRoboImage("$outputDir/$name.png")
    }

    private val fixtureImages = AsyncImagePreviewHandler { request ->
        val name = (request.data as? String)?.removePrefix(FIXTURE_SCHEME)
        val stream = name?.let { javaClass.classLoader?.getResourceAsStream("screenshot-fixtures/img/$it") }
        val bitmap = stream?.let { BitmapFactory.decodeStream(it) }
            ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        bitmap.asImage()
    }

    private fun loadArticles(): List<Article> {
        val stream = javaClass.classLoader?.getResourceAsStream("screenshot-fixtures/articles.json")
            ?: return emptyList()
        val json = JSONArray(stream.bufferedReader().readText())
        val now = ZonedDateTime.now()
        val newest = (0 until json.length()).maxOf { json.getJSONObject(it).getLong("date") }

        return (0 until json.length()).map { i ->
            val item = json.getJSONObject(i)
            val imageFile = item.optString("imageFile").takeIf { it.isNotBlank() }
            // Shift dates so the newest item is "just now", keeping relative spacing.
            val age = newest - item.getLong("date")

            Article(
                id = item.getString("id"),
                feedID = item.getString("feed"),
                title = item.getString("title"),
                author = item.optString("author"),
                contentHTML = "",
                url = URL(item.getString("url")),
                summary = item.getString("summary").take(SUMMARY_LENGTH),
                imageURL = imageFile?.let { FIXTURE_SCHEME + it },
                updatedAt = now.minusSeconds(age + 45),
                publishedAt = now.minusSeconds(age + 45),
                read = item.getBoolean("read") && i % 3 == 0,
                starred = item.getBoolean("starred") || i == 4,
                feedName = item.getString("feed"),
            )
        }
    }

    companion object {
        private const val FIXTURE_SCHEME = "fixture://"
        private const val SUMMARY_LENGTH = 300
    }
}

@Composable
private fun MagazineFixture(articles: List<Article>) {
    val items = remember { flowOf(PagingData.from(articles, notLoading)) }.collectAsLazyPagingItems()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        MagazineGrid(
            articles = items,
            gridState = remember { LazyStaggeredGridState() },
            selectedArticleKey = null,
            onSelect = {},
            onMarkAllRead = {},
            dimReadArticles = true,
            contentPadding = PaddingValues(),
        )
    }
}

@Composable
private fun ListFixture(articles: List<Article>, compact: Boolean = false) {
    val currentTime = LocalDateTime.now(ZoneId.systemDefault())

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        articles.take(if (compact) 16 else 9).forEachIndexed { index, article ->
            ArticleRow(
                article = article,
                index = index,
                selected = index == 1,
                onSelect = {},
                currentTime = currentTime,
                options = ArticleRowOptions(compact = compact),
            )
        }
    }
}

private val notLoading = LoadStates(
    refresh = LoadState.NotLoading(endOfPaginationReached = true),
    prepend = LoadState.NotLoading(endOfPaginationReached = true),
    append = LoadState.NotLoading(endOfPaginationReached = true),
)
