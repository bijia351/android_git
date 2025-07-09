package com.anguomob.git.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anguomob.git.data.api.GitHubApiService
import com.anguomob.git.data.model.Repository

class DailyTrendingPagingSource(
    private val gitHubApiService: GitHubApiService,
    private val query: String
) : PagingSource<Int, Repository>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repository> {
        val page = params.key ?: 1
        return try {
            val response = gitHubApiService.searchRepositories(
                query = query,
                sort = "stars",
                order = "desc",
                page = page,
                perPage = params.loadSize
            )
            val repos = response.items
            LoadResult.Page(
                data = repos,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (repos.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Repository>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
