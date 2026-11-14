package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.LocalTimeFormats
import com.jocmp.capy.articles.relativeTime
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Age label in the style of Current ("just now", "12m ago", "3h ago", "2d ago"),
 * falling back to the regular date format after a week.
 */
@Composable
fun riverTime(publishedAt: ZonedDateTime, currentTime: LocalDateTime): String {
    val published = publishedAt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    val age = Duration.between(published, currentTime)

    return when {
        age.toMinutes() < 1 -> stringResource(R.string.river_time_now)
        age.toHours() < 1 -> stringResource(R.string.river_time_minutes, age.toMinutes())
        age.toDays() < 1 -> stringResource(R.string.river_time_hours, age.toHours())
        age.toDays() < 7 -> stringResource(R.string.river_time_days, age.toDays())
        else -> relativeTime(
            time = publishedAt,
            currentTime = currentTime,
            formats = LocalTimeFormats.current,
        )
    }
}
