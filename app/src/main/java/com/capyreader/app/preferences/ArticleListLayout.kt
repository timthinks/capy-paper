package com.capyreader.app.preferences

import com.capyreader.app.R

enum class ArticleListLayout {
    LIST,
    COMPACT,
    MAGAZINE;

    val translationKey: Int
        get() = when (this) {
            LIST -> R.string.article_list_layout_list
            COMPACT -> R.string.article_list_layout_compact
            MAGAZINE -> R.string.article_list_layout_magazine
        }

    companion object {
        val default
            get() = MAGAZINE
    }
}
