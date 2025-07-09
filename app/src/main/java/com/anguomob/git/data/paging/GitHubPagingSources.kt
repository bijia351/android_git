package com.anguomob.git.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anguomob.git.data.api.GitHubApiService
import com.anguomob.git.data.model.Repository
import com.anguomob.git.data.model.Topic
import com.anguomob.git.data.model.User

/**
 * 按语言分类的仓库分页数据源
 */
class LanguageRepositoriesPagingSource(
    private val gitHubApiService: GitHubApiService,
    private val language: String
) : PagingSource<Int, Repository>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repository> {
        return try {
            val page = params.key ?: 1
            val query = "language:$language stars:>10"
            val response = gitHubApiService.getTrendingByLanguage(
                query = query,
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

/**
 * 主题搜索分页数据源
 */
class TopicSearchPagingSource(
    private val gitHubApiService: GitHubApiService,
    private val query: String
) : PagingSource<Int, Topic>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Topic> {
        return try {
            val page = params.key ?: 1
            val response = gitHubApiService.searchTopics(
                query = query,
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
    
    override fun getRefreshKey(state: PagingState<Int, Topic>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

/**
 * 按主题获取仓库的分页数据源
 */
class TopicRepositoriesPagingSource(
    private val gitHubApiService: GitHubApiService,
    private val topic: String
) : PagingSource<Int, Repository>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repository> {
        return try {
            val page = params.key ?: 1
            val query = "topic:$topic"
            val response = gitHubApiService.searchRepositories(
                query = query,
                sort = "stars",
                order = "desc",
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

/**
 * 用户搜索分页数据源
 */
class UserSearchPagingSource(
    private val gitHubApiService: GitHubApiService,
    private val query: String,
    private val sort: String? = null,
    private val order: String? = null
) : PagingSource<Int, User>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return try {
            val page = params.key ?: 1
            val response = gitHubApiService.searchUsers(
                query = query,
                sort = sort,
                order = order,
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
    
    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
} 