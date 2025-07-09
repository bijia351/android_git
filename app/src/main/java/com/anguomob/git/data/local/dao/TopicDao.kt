package com.anguomob.git.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.anguomob.git.data.model.Topic
import kotlinx.coroutines.flow.Flow

/**
 * Topic 数据访问对象
 */
@Dao
interface TopicDao {
    
    /**
     * 插入主题列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<Topic>)
    
    /**
     * 插入单个主题
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic)
    
    /**
     * 删除所有主题
     */
    @Query("DELETE FROM topics")
    suspend fun clearAll()
    
    /**
     * 根据名称获取主题
     */
    @Query("SELECT * FROM topics WHERE name = :name")
    suspend fun getTopicByName(name: String): Topic?
    
    /**
     * 获取所有主题（分页）
     */
    @Query("SELECT * FROM topics ORDER BY featured DESC, curated DESC, name ASC")
    fun getAllTopics(): PagingSource<Int, Topic>
    
    /**
     * 获取主题流（用于实时更新UI）
     */
    @Query("SELECT * FROM topics ORDER BY featured DESC, curated DESC, name ASC LIMIT :limit")
    fun getTopicsFlow(limit: Int = 50): Flow<List<Topic>>
    
    /**
     * 搜索主题
     */
    @Query("""
        SELECT * FROM topics 
        WHERE name LIKE '%' || :query || '%' 
        OR displayName LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        OR shortDescription LIKE '%' || :query || '%'
        ORDER BY featured DESC, curated DESC, name ASC
    """)
    fun searchTopics(query: String): PagingSource<Int, Topic>
    
    /**
     * 获取精选主题
     */
    @Query("SELECT * FROM topics WHERE featured = 1 ORDER BY name ASC")
    suspend fun getFeaturedTopics(): List<Topic>
    
    /**
     * 获取策划主题
     */
    @Query("SELECT * FROM topics WHERE curated = 1 ORDER BY name ASC")
    suspend fun getCuratedTopics(): List<Topic>
    
    /**
     * 获取热门主题（按评分排序）
     */
    @Query("SELECT * FROM topics WHERE score IS NOT NULL ORDER BY score DESC LIMIT :limit")
    suspend fun getTrendingTopics(limit: Int = 20): List<Topic>
    
    /**
     * 获取主题总数
     */
    @Query("SELECT COUNT(*) FROM topics")
    suspend fun getTopicCount(): Int
    
    /**
     * 删除特定主题
     */
    @Query("DELETE FROM topics WHERE name = :name")
    suspend fun deleteTopic(name: String)
    
    /**
     * 更新主题
     */
    @Update
    suspend fun updateTopic(topic: Topic)
} 