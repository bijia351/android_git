package com.anguomob.git.data.local.dao

import androidx.room.*
import com.anguomob.git.data.model.Favorite
import com.anguomob.git.data.model.FavoriteType
import kotlinx.coroutines.flow.Flow

/**
 * 收藏数据访问对象
 */
@Dao
interface FavoriteDao {
    
    /**
     * 添加收藏
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)
    
    /**
     * 批量添加收藏
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<Favorite>)
    
    /**
     * 获取所有收藏
     */
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<Favorite>>
    
    /**
     * 按类型获取收藏
     */
    @Query("SELECT * FROM favorites WHERE itemType = :type ORDER BY timestamp DESC")
    fun getFavoritesByType(type: FavoriteType): Flow<List<Favorite>>
    
    /**
     * 检查是否已收藏
     */
    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE itemId = :itemId AND itemType = :type")
    suspend fun isFavorited(itemId: String, type: FavoriteType): Boolean
    
    /**
     * 根据ID和类型获取收藏
     */
    @Query("SELECT * FROM favorites WHERE itemId = :itemId AND itemType = :type")
    suspend fun getFavorite(itemId: String, type: FavoriteType): Favorite?
    
    /**
     * 搜索收藏
     */
    @Query("""
        SELECT * FROM favorites 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    suspend fun searchFavorites(query: String): List<Favorite>
    
    /**
     * 删除收藏
     */
    @Query("DELETE FROM favorites WHERE itemId = :itemId AND itemType = :type")
    suspend fun deleteFavorite(itemId: String, type: FavoriteType)
    
    /**
     * 根据ID删除收藏
     */
    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)
    
    /**
     * 清空所有收藏
     */
    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
    
    /**
     * 清空特定类型的收藏
     */
    @Query("DELETE FROM favorites WHERE itemType = :type")
    suspend fun clearFavoritesByType(type: FavoriteType)
    
    /**
     * 获取收藏总数
     */
    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
    
    /**
     * 按类型获取收藏总数
     */
    @Query("SELECT COUNT(*) FROM favorites WHERE itemType = :type")
    suspend fun getFavoriteCountByType(type: FavoriteType): Int
    
    /**
     * 更新收藏
     */
    @Update
    suspend fun updateFavorite(favorite: Favorite)
} 