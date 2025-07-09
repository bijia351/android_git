package com.anguomob.git.util

object Constants {
    
    // API配置
    const val GITHUB_BASE_URL = "https://api.github.com/"
    const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"
    
    // 分页配置
    const val PAGE_SIZE = 30
    const val PREFETCH_DISTANCE = 5
    
    // 网络超时配置
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    // OpenRouter API配置
    const val APP_NAME = "GitHub Tech Radar"
    const val APP_URL = "https://github.com/yourproject/github-tech-radar"
    
    // 数据库配置
    const val DATABASE_NAME = "git_radar_database"
    const val DATABASE_VERSION = 1
    
    // 搜索配置
    const val SEARCH_HISTORY_LIMIT = 20
    const val TRENDING_REPOS_LIMIT = 50
    const val FEATURED_TOPICS_LIMIT = 30
    
    // GitHub API默认查询
    object GitHubQueries {
        const val TRENDING_REPOS = "stars:>1000 pushed:>2023-01-01"
        const val FEATURED_TOPICS = "is:featured"
        const val ANDROID_REPOS = "language:kotlin android stars:>100"
        const val WEB_REPOS = "language:javascript OR language:typescript stars:>500"
        const val AI_REPOS = "topic:artificial-intelligence OR topic:machine-learning stars:>100"
    }
    
    // UI配置
    const val CARD_ELEVATION = 4
    const val CARD_CORNER_RADIUS = 8
    const val IMAGE_SIZE = 48
    const val LARGE_IMAGE_SIZE = 96
}
