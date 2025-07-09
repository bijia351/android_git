package com.anguomob.git.data.local.dao

import androidx.room.*
import com.anguomob.git.data.model.SearchHistory
import com.anguomob.git.data.model.SearchType
import kotlinx.coroutines.flow.Flow

/**
 * 搜索历史数据访问对象
 */
@Dao
interface SearchHistoryDao {
    
    /**
     * 插入搜索记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(searchHistory: SearchHistory)
    
    /**
     * 获取所有搜索历史
     */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getAllSearchHistory(): Flow<List<SearchHistory>>
    
    /**
     * 按类型获取搜索历史
     */
    @Query("SELECT * FROM search_history WHERE type = :type ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getSearchHistoryByType(type: SearchType, limit: Int = 20): List<SearchHistory>
    
    /**
     * 获取最近搜索历史
     */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSearchHistory(limit: Int = 10): List<SearchHistory>
    
    /**
     * 搜索历史记录
     */
    @Query("SELECT * FROM search_history WHERE query LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchHistory(query: String): List<SearchHistory>
    
    /**
     * 删除特定搜索记录
     */
    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchHistory(id: Long)
    
    /**
     * 删除特定查询的所有记录
     */
    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearchHistoryByQuery(query: String)
    
    /**
     * 清空所有搜索历史
     */
    @Query("DELETE FROM search_history")
    suspend fun clearAllSearchHistory()
    
    /**
     * 删除指定天数之前的搜索历史
     */
    @Query("DELETE FROM search_history WHERE timestamp < :timestamp")
    suspend fun deleteOldSearchHistory(timestamp: Long)
    
    /**
     * 获取搜索历史总数
     */
    @Query("SELECT COUNT(*) FROM search_history")
    suspend fun getSearchHistoryCount(): Int
} 