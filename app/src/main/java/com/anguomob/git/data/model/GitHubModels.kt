package com.anguomob.git.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * GitHub 仓库模型
 */
@Entity(tableName = "repositories")
data class Repository(
    @PrimaryKey 
    val id: Long,
    val name: String,
    @SerializedName("full_name")
    val fullName: String,
    val description: String?,
    @SerializedName("private")
    val isPrivate: Boolean,
    @SerializedName("html_url")
    val htmlUrl: String,
    val language: String?,
    @SerializedName("stargazers_count")
    val stargazersCount: Int,
    @SerializedName("watchers_count")
    val watchersCount: Int,
    @SerializedName("forks_count")
    val forksCount: Int,
    @SerializedName("open_issues_count")
    val openIssuesCount: Int,
    @SerializedName("default_branch")
    val defaultBranch: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("pushed_at")
    val pushedAt: String?,
    val size: Int,
    val topics: List<String>?,
    val license: License?,
    val owner: User,
    val score: Float? = null // 用于搜索结果排序
)

/**
 * GitHub 用户模型
 */
data class User(
    val id: Long,
    val login: String,
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    val type: String,
    @SerializedName("site_admin")
    val siteAdmin: Boolean,
    val name: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val company: String? = null,
    val location: String? = null,
    @SerializedName("public_repos")
    val publicRepos: Int? = null,
    val followers: Int? = null,
    val following: Int? = null
)

/**
 * 许可证信息
 */
data class License(
    val key: String,
    val name: String,
    @SerializedName("spdx_id")
    val spdxId: String?,
    val url: String?
)

/**
 * GitHub 主题模型
 */
@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey
    val name: String,
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("short_description")
    val shortDescription: String?,
    val description: String?,
    @SerializedName("created_by")
    val createdBy: String?,
    val released: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    val featured: Boolean,
    val curated: Boolean,
    val score: Float? = null
)

/**
 * GitHub 提交模型
 */
data class Commit(
    val sha: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    val commit: CommitInfo,
    val author: User?,
    val committer: User?,
    val repository: Repository?
)

/**
 * 提交详细信息
 */
data class CommitInfo(
    val author: GitSignature,
    val committer: GitSignature,
    val message: String,
    @SerializedName("comment_count")
    val commentCount: Int
)

/**
 * Git 签名信息
 */
data class GitSignature(
    val name: String,
    val email: String,
    val date: String
)

/**
 * 搜索响应基础模型
 */
data class SearchResponse<T>(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("incomplete_results")
    val incompleteResults: Boolean,
    val items: List<T>
)

/**
 * 搜索历史记录
 */
@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val type: SearchType,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 搜索类型枚举
 */
enum class SearchType {
    REPOSITORIES,
    TOPICS
}

/**
 * 收藏记录
 */
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: String, // 可以是仓库ID、用户名等
    val itemType: FavoriteType,
    val title: String,
    val description: String?,
    val avatarUrl: String?,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 收藏类型枚举
 */
enum class FavoriteType {
    REPOSITORY,
    USER,
    TOPIC
}

/**
 * README file model
 */
data class Readme(
    val name: String,
    val path: String,
    val sha: String,
    val size: Int,
    val url: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("git_url")
    val gitUrl: String,
    @SerializedName("download_url")
    val downloadUrl: String,
    val type: String,
    val content: String,
    val encoding: String
)
