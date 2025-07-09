package com.anguomob.git.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.anguomob.git.data.model.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Repository 数据访问对象
 */
@Dao
interface RepositoryDao {
    
    /**
     * 插入仓库列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositories(repositories: List<Repository>)
    
    /**
     * 插入单个仓库
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepository(repository: Repository)
    
    /**
     * 删除所有仓库
     */
    @Query("DELETE FROM repositories")
    suspend fun clearAll()
    
    /**
     * 根据ID获取仓库
     */
    @Query("SELECT * FROM repositories WHERE id = :id")
    suspend fun getRepositoryById(id: Long): Repository?
    
    /**
     * 获取所有仓库（分页）
     */
    @Query("SELECT * FROM repositories ORDER BY stargazersCount DESC")
    fun getAllRepositories(): PagingSource<Int, Repository>
    
    /**
     * 获取仓库流（用于实时更新UI）
     */
    @Query("SELECT * FROM repositories ORDER BY stargazersCount DESC LIMIT :limit")
    fun getRepositoriesFlow(limit: Int = 50): Flow<List<Repository>>
    
    /**
     * 按语言搜索仓库
     */
    @Query("SELECT * FROM repositories WHERE language = :language ORDER BY stargazersCount DESC")
    fun getRepositoriesByLanguage(language: String): PagingSource<Int, Repository>
    
    /**
     * 搜索仓库（按名称或描述）
     */
    @Query("""
        SELECT * FROM repositories 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        OR fullName LIKE '%' || :query || '%'
        ORDER BY stargazersCount DESC
    """)
    fun searchRepositories(query: String): PagingSource<Int, Repository>
    
    /**
     * 获取热门仓库（星数排序）
     */
    @Query("SELECT * FROM repositories ORDER BY stargazersCount DESC LIMIT :limit")
    suspend fun getTrendingRepositories(limit: Int = 30): List<Repository>
    
    /**
     * 获取最近更新的仓库
     */
    @Query("SELECT * FROM repositories ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentlyUpdatedRepositories(limit: Int = 30): List<Repository>
    
    /**
     * 获取特定用户的仓库
     */
    @Query("SELECT * FROM repositories WHERE owner LIKE '%\"login\":\"' || :username || '\"%' ORDER BY stargazersCount DESC")
    suspend fun getRepositoriesByOwner(username: String): List<Repository>
    
    /**
     * 获取包含特定主题的仓库
     */
    @Query("SELECT * FROM repositories WHERE topics LIKE '%' || :topic || '%' ORDER BY stargazersCount DESC")
    suspend fun getRepositoriesByTopic(topic: String): List<Repository>
    
    /**
     * 按星数范围获取仓库
     */
    @Query("SELECT * FROM repositories WHERE stargazersCount BETWEEN :minStars AND :maxStars ORDER BY stargazersCount DESC")
    suspend fun getRepositoriesByStarRange(minStars: Int, maxStars: Int): List<Repository>
    
    /**
     * 获取仓库总数
     */
    @Query("SELECT COUNT(*) FROM repositories")
    suspend fun getRepositoryCount(): Int
    
    /**
     * 删除特定仓库
     */
    @Query("DELETE FROM repositories WHERE id = :id")
    suspend fun deleteRepository(id: Long)
    
    /**
     * 更新仓库
     */
    @Update
    suspend fun updateRepository(repository: Repository)
} 