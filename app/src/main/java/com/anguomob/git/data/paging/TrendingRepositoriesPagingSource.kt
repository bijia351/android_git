package com.anguomob.git.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anguomob.git.data.api.GitHubApiService
import com.anguomob.git.data.model.Repository

/**
 * 热门仓库分页数据源
 */
class TrendingRepositoriesPagingSource(
    private val gitHubApiService: GitHubApiService
) : PagingSource<Int, Repository>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repository> {
        return try {
            val page = params.key ?: 1
            val response = gitHubApiService.getTrendingRepositories(
                perPage = params.loadSize,
                page = page
            )
            
            LoadResult.Page(
                data = response.items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.items.isEmpty()) null else page + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, Repository>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
} 