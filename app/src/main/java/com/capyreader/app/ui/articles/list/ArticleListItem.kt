package com.capyreader.app.ui.articles.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.articles.FaviconBadge
import com.capyreader.app.ui.components.ProvideContentColorTextStyle
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun ArticleListItem(
    headlineContent: @Composable () -> Unit,
    overlineContent: @Composable() (() -> Unit)? = null,
    supportingContent: @Composable() (() -> Unit)? = null,
    leadingContent: @Composable() (() -> Unit)? = null,
    trailingContent: @Composable() (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    compact: Boolean = false,
) {
    val spacing = if (compact) CompactSpacing else VerticalSpacing
    val horizontalPadding = if (compact) CompactHorizontalPadding else VerticalPadding

    Surface(
        color = colors.containerColor,
    ) {
        val leadingSize = 16.dp

        val overlineStartPadding = if (leadingContent != null) {
            leadingSize + spacing
        } else {
            0.dp
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (compact) CompactTopPadding else TopPadding,
                    start = horizontalPadding,
                    end = spacing + 4.dp,
                    bottom = if (compact) CompactBottomPadding else BottomPadding,
                )
        ) {
            // Compact rows tuck the overline into the text column so the thumbnail sits beside it.
            overlineContent?.takeUnless { compact }?.let {
                Row(Modifier.padding(start = overlineStartPadding)) {
                    ProvideTextStyleFromToken(
                        colors.overlineContentColor,
                        typography.labelSmall,
                        it,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                leadingContent?.let {
                    Column(Modifier.size(leadingSize)) { it() }
                }
                Column(Modifier.weight(1f)) {
                    overlineContent?.takeIf { compact }?.let {
                        Row {
                            ProvideTextStyleFromToken(
                                colors.overlineContentColor,
                                typography.labelSmall,
                                it,
                            )
                        }
                    }
                    ProvideTextStyleFromToken(
                        colors.contentColor,
                        typography.bodyLarge,
                    ) {
                        headlineContent()
                    }

                    supportingContent?.let {
                        ProvideTextStyleFromToken(
                            colors.supportingContentColor,
                            typography.bodyMedium,
                            it,
                        )
                    }
                }
                trailingContent?.let {
                    Column {
                        ProvideTextStyleFromToken(
                            colors.trailingContentColor,
                            typography.labelSmall,
                            it,
                        )
                    }
                }
            }
        }
    }
}

private val VerticalSpacing = 20.dp

private val VerticalPadding = 24.dp

private val TopPadding = 16.dp

private val BottomPadding = 18.dp

private val CompactSpacing = 14.dp

private val CompactHorizontalPadding = 20.dp

private val CompactTopPadding = 10.dp

private val CompactBottomPadding = 12.dp

@Composable
private fun ProvideTextStyleFromToken(
    color: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit,
) =
    ProvideContentColorTextStyle(
        contentColor = color,
        textStyle = textStyle,
        content = content
    )

@Preview
@Composable
private fun ArticleListItemPreview() {
    CapyTheme {
        ArticleListItem(
            headlineContent = {
                Text("Headline")
            },
            overlineContent = {
                Text("Overline")
            },
            supportingContent = {
                Text("Supporting")
            },
            leadingContent = {
                FaviconBadge(url = null)
            },
            trailingContent = {
                Text("Trailing")
            },
        )
    }
}
