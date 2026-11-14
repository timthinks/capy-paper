package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capyreader.app.R
import com.capyreader.app.ui.LocalTimeFormats
import com.capyreader.app.ui.articles.detail.byline
import com.capyreader.app.ui.articles.displayFeedName
import com.jocmp.capy.Article

private val bylineTextStyle = TextStyle(
    fontSize = 15.sp,
    lineHeight = 15.sp * 1.4f,
)

@Composable
fun ArticleHeader(
    article: Article,
    onOpenLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val formats = LocalTimeFormats.current
    val readerStyle = LocalReaderStyle.current
    val feedName = article.displayFeedName(context)
    val showPlaceholderTitle = article.title.isBlank()
    val title = if (showPlaceholderTitle) {
        feedName
    } else {
        article.title
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (!showPlaceholderTitle) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = feedName.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        BidiLayoutDirection(paragraph = title) {
            Text(
                text = title,
                style = readerStyle.titleTextStyle.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() }
                    .clickable(
                        onClickLabel = stringResource(R.string.reader_open_in_browser),
                        onClick = onOpenLink,
                    )
                    .padding(bottom = 12.dp),
            )
        }
        Text(
            text = article.byline(context = context, formats = formats),
            style = bylineTextStyle.copy(fontFamily = readerStyle.fontFamily),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
