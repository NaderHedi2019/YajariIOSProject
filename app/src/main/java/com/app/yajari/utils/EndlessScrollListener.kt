package com.app.yajari.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class EndlessScrollListener : RecyclerView.OnScrollListener {

    private var visibleThreshold = 1
    private lateinit var mOnLoadMoreListener: OnLoadMoreListener
    private var isLoading: Boolean = false
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var mLayoutManager: RecyclerView.LayoutManager

    // True if we are still waiting for the last set of data to load.

    // Sets the starting page index

    fun setLoaded() {
        isLoading = true
    }
    fun setLoaded(isLoad:Boolean)
    {
        this.isLoading=isLoad
    }

    fun getLoaded(): Boolean {
        return this.isLoading
    }

    fun setOnLoadMoreListener(mOnLoadMoreListener: OnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener
    }

    constructor(layoutManager: LinearLayoutManager) {
        this.mLayoutManager = layoutManager
    }

    constructor(layoutManager: GridLayoutManager) {
        this.mLayoutManager = layoutManager
        visibleThreshold *= layoutManager.spanCount
    }


    constructor(layoutManager: StaggeredGridLayoutManager) {
        this.mLayoutManager = layoutManager
        visibleThreshold *= layoutManager.spanCount
    }

    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        if (view.getChildAt(view.childCount - 1) != null) {
            if (dy >= view.getChildAt(view.childCount - 1).measuredHeight - view.measuredHeight) {
                totalItemCount = mLayoutManager.itemCount
                when (mLayoutManager) {
                    is StaggeredGridLayoutManager -> {
                        val lastVisibleItemPositions =
                            (mLayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(
                                null
                            )
                        // get maximum element within the list
                        lastVisibleItem = getLastVisibleItem(lastVisibleItemPositions)
                    }

                    is GridLayoutManager -> {
                        lastVisibleItem =
                            (mLayoutManager as GridLayoutManager).findLastVisibleItemPosition()
                    }

                    is LinearLayoutManager -> {
                        lastVisibleItem =
                            (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    }
                }

                if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                    mOnLoadMoreListener.onLoadMore()
                    //isLoading = true
                }
            }
            mOnLoadMoreListener.onScrollChange(dy = dy,view.scrollState)
        }

    }
}
