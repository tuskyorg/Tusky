/* Copyright 2019 Joel Pyska
 *
 * This file is a part of Tusky.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky; if not,
 * see <http://www.gnu.org/licenses>. */

package com.keylesspalace.tusky.components.search.adapter

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.keylesspalace.tusky.components.search.SearchType
import com.keylesspalace.tusky.entity.SearchResult
import com.keylesspalace.tusky.network.MastodonApi
import com.keylesspalace.tusky.util.NetworkState
import kotlinx.coroutines.rx3.await
import java.util.concurrent.Executor

class SearchPagingSource<T: Any>(
    private val mastodonApi: MastodonApi,
    private val searchType: SearchType,
    private val searchRequest: String,
    private val retryExecutor: Executor,
    private val initialItems: List<T>?,
    private val parser: (SearchResult?) -> List<T>) : PagingSource<Int, T>() {

    val networkState = MutableLiveData<NetworkState>()

    private var retry: (() -> Any)? = null

    val initialLoad = MutableLiveData<NetworkState>()

    fun retry() {
        retry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        if (params.key == null && !initialItems.isNullOrEmpty()) {
            return LoadResult.Page(
                data = initialItems,
                prevKey = null,
                nextKey = initialItems.size
            )
        }

        val currentKey = params.key ?: 0

        val data = mastodonApi.searchObservable(
            query = searchRequest,
            type = searchType.apiParameter,
            resolve = true,
            limit = params.loadSize,
            offset = currentKey,
            following = false
        ).await()

        // Working around Mastodon bug where exact match is returned no matter
        // which offset is requested (so if we search for a full username, it's
        // infinite)
        // see https://github.com/tootsuite/mastodon/issues/11365
        // see https://github.com/tootsuite/mastodon/issues/13083
        val res = if ((data.accounts.size == 1 && data.accounts[0].username.equals(searchRequest, ignoreCase = true))
            || (data.statuses.size == 1 && data.statuses[0].url.equals(searchRequest))) {
            listOf()
        } else {
            parser(data)
        }

        val nextKey = if (res.isEmpty()) {
            null
        } else {
            currentKey + res.size
        }

        return LoadResult.Page(
            data = res,
            prevKey = null,
            nextKey = nextKey
        )
    }
}