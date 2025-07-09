# API 文档

本应用使用了以下 API 来获取数据和实现 AI 功能。

## GitHub REST API

所有 GitHub API 请求都需要一个 personal access token。

### 1. 搜索

*   **端点**: `GET /search/repositories`
*   **描述**: 搜索仓库。
*   **参数**:
    *   `q`: 搜索查询字符串。
    *   `sort`: 排序字段 (e.g., `stars`, `forks`, `updated`)。
    *   `order`: 排序顺序 (`desc`, `asc`)。
    *   `per_page`: 每页结果数。
    *   `page`: 页码。
*   **示例**: `https://api.github.com/search/repositories?q=android&sort=stars`

*   **端点**: `GET /search/users`
*   **描述**: 搜索用户。
*   **参数**:
    *   `q`: 搜索查询字符串。
*   **示例**: `https://api.github.com/search/users?q=torvalds`

*   **端点**: `GET /search/topics`
*   **描述**: 搜索主题。
*   **参数**:
    *   `q`: 搜索查询字符串。
*   **示例**: `https://api.github.com/search/topics?q=android`

### 2. 仓库

*   **端点**: `GET /repos/{owner}/{repo}`
*   **描述**: 获取指定仓库的详细信息。
*   **示例**: `https://api.github.com/repos/torvalds/linux`

*   **端点**: `GET /repos/{owner}/{repo}/contributors`
*   **描述**: 获取仓库的贡献者列表。
*   **示例**: `https://api.github.com/repos/torvalds/linux/contributors`

*   **端点**: `GET /repos/{owner}/{repo}/languages`
*   **描述**: 获取仓库使用的语言统计。
*   **示例**: `https://api.github.com/repos/torvalds/linux/languages`

*   **端点**: `GET /repos/{owner}/{repo}/readme`
*   **描述**: 获取仓库的 README 文件。
*   **示例**: `https://api.github.com/repos/torvalds/linux/readme`

### 3. 用户

*   **端点**: `GET /users/{username}`
*   **描述**: 获取指定用户的详细信息。
*   **示例**: `https://api.github.com/users/torvalds`

*   **端点**: `GET /users/{username}/repos`
*   **描述**: 获取指定用户的仓库列表。
*   **示例**: `https://api.github.com/users/torvalds/repos`

## OpenRouter API

所有 OpenRouter API 请求都需要一个 API key。

### 1. Chat Completions

*   **端点**: `POST /api/v1/chat/completions`
*   **描述**: 用于与 AI 模型进行交互，以实现 README 摘要、技术趋势分析和个人技术雷达等功能。
*   **请求体**:
    ```json
    {
      "model": "qwen/qwen3-30b-a3b:free",
      "messages": [
        {
          "role": "system",
          "content": "You are an expert in summarizing technical documents..."
        },
        {
          "role": "user",
          "content": "..."
        }
      ]
    }
