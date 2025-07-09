# 数据模型

本应用的核心数据实体（Data Models）如下，它们与 API 响应相对应。

## GitHub API 数据模型

### Repository

`Repository` 是应用的核心模型之一，代表一个 GitHub 仓库。

```kotlin
data class Repository(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val isPrivate: Boolean,
    val htmlUrl: String,
    val language: String?,
    val stargazersCount: Int,
    val watchersCount: Int,
    val forksCount: Int,
    val openIssuesCount: Int,
    val defaultBranch: String?,
    val createdAt: String,
    val updatedAt: String,
    val pushedAt: String?,
    val size: Int,
    val topics: List<String>?,
    val license: License?,
    val owner: User,
    val score: Float?
)
```

### User

`User` 模型代表一个 GitHub 用户。

```kotlin
data class User(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val type: String,
    val siteAdmin: Boolean,
    val name: String?,
    val email: String?,
    val bio: String?,
    val company: String?,
    val location: String?,
    val publicRepos: Int?,
    val followers: Int?,
    val following: Int?
)
```

### Topic

`Topic` 模型代表一个 GitHub 主题。

```kotlin
data class Topic(
    val name: String,
    val displayName: String?,
    val shortDescription: String?,
    val description: String?,
    val createdBy: String?,
    val released: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val featured: Boolean,
    val curated: Boolean,
    val score: Float?
)
```

### 其他 GitHub 模型

*   `License`: 仓库的许可证信息。
*   `Commit`: 仓库的提交信息。
*   `Readme`: 仓库的 README 文件信息。

## OpenRouter API 数据模型

### ChatRequest & ChatResponse

`ChatRequest` 和 `ChatResponse` 是与 OpenRouter API 进行交互的核心模型。

```kotlin
// 请求
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val maxTokens: Int?,
    val temperature: Float?,
    val topP: Float?,
    val stream: Boolean = false
)

// 响应
data class ChatResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: ChatUsage?
)

// 消息
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)
```

## 本地数据模型

### SearchHistory

`SearchHistory` 用于在本地数据库中存储用户的搜索历史。

```kotlin
@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val type: SearchType,
    val timestamp: Long = System.currentTimeMillis()
)
```

### Favorite

`Favorite` 用于在本地数据库中存储用户收藏的仓库和主题。

```kotlin
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: String,
    val itemType: FavoriteType,
    val title: String,
    val description: String?,
    val avatarUrl: String?,
    val timestamp: Long = System.currentTimeMillis()
)
