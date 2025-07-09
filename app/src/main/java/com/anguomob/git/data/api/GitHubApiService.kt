package com.anguomob.git.data.api

import com.anguomob.git.data.model.*
import retrofit2.http.*

/**
 * GitHub REST API 服务接口
 * 基于 GitHub REST API v4 文档实现
 */
interface GitHubApiService {
    
    companion object {
        const val BASE_URL = "https://api.github.com/"
        const val ACCEPT_HEADER = "application/vnd.github+json"
        const val VERSION_HEADER = "2022-11-28"
    }
    
    // ========== 搜索相关接口 ==========
    
    /**
     * 搜索仓库
     * GET /search/repositories
     */
    @GET("search/repositories")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String? = null, // stars, forks, help-wanted-issues, updated
        @Query("order") order: String? = null, // desc, asc
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): SearchResponse<Repository>
    
    /**
     * 搜索用户
     * GET /search/users
     */
    @GET("search/users")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("sort") sort: String? = null, // followers, repositories, joined
        @Query("order") order: String? = null, // desc, asc
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): SearchResponse<User>
    
    /**
     * 搜索主题
     * GET /search/topics
     */
    @GET("search/topics")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun searchTopics(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): SearchResponse<Topic>
    
    /**
     * 搜索代码
     * GET /search/code
     */
    @GET("search/code")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun searchCode(
        @Query("q") query: String,
        @Query("sort") sort: String? = null, // indexed
        @Query("order") order: String? = null, // desc, asc
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): SearchResponse<Any> // 代码搜索结果结构复杂，暂用Any
    
    /**
     * 搜索提交
     * GET /search/commits
     */
    @GET("search/commits")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun searchCommits(
        @Query("q") query: String,
        @Query("sort") sort: String? = null, // author-date, committer-date
        @Query("order") order: String? = null, // desc, asc
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): SearchResponse<Commit>
    
    // ========== 仓库相关接口 ==========
    
    /**
     * 获取热门仓库
     * 通过搜索实现，按星数排序
     */
    @GET("search/repositories")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun getTrendingRepositories(
        @Query("q") query: String = "stars:>1000", // 默认获取星数大于1000的仓库
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): SearchResponse<Repository>
    
    /**
     * 获取特定仓库信息
     * GET /repos/{owner}/{repo}
     */
    @GET("repos/{owner}/{repo}")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Repository
    
    /**
     * 获取仓库主题
     * GET /repos/{owner}/{repo}/topics
     */
    @GET("repos/{owner}/{repo}/topics")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun getRepositoryTopics(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): TopicsResponse
    
    /**
     * 获取仓库语言统计
     * GET /repos/{owner}/{repo}/languages
     */
    @GET("repos/{owner}/{repo}/languages")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun getRepositoryLanguages(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Map<String, Int>
    
    /**
     * 获取仓库贡献者
     * GET /repos/{owner}/{repo}/contributors
     */
    @GET("repos/{owner}/{repo}/contributors")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun getRepositoryContributors(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): List<User>

    /**
     * Get a repository README
     * GET /repos/{owner}/{repo}/readme
     */
    @GET("repos/{owner}/{repo}/readme")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun getReadme(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Readme
    
    // ========== 用户相关接口 ==========
    
    /**
     * 获取用户信息
     * GET /users/{username}
     */
    @GET("users/{username}")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun getUser(
        @Path("username") username: String
    ): User
    
    /**
     * 获取用户仓库
     * GET /users/{username}/repos
     */
    @GET("users/{username}/repos")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun getUserRepositories(
        @Path("username") username: String,
        @Query("type") type: String = "all", // all, owner, member
        @Query("sort") sort: String = "updated", // created, updated, pushed, full_name
        @Query("direction") direction: String = "desc", // asc, desc
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): List<Repository>
    
    // ========== 趋势和统计接口 ==========
    
    @GET("search/repositories")
    suspend fun getRepoCountForQuery(@Query("q") query: String): SearchResponse<Repository>
    
    /**
     * 按编程语言获取趋势仓库
     */
    @GET("search/repositories")
    @Headers("Accept: $ACCEPT_HEADER", "X-GitHub-Api-Version: $VERSION_HEADER")
    suspend fun getTrendingByLanguage(
        @Query("q") query: String, // 格式: "language:kotlin stars:>100"
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): SearchResponse<Repository>
}

/**
 * 仓库主题响应模型
 */
data class TopicsResponse(
    val names: List<String>
)
