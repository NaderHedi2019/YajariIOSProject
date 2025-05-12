package com.app.yajari.utils

interface OnLoadMoreListener {
    fun onLoadMore()
    fun onScrollChange(dy: Int, scrollState: Int)
}