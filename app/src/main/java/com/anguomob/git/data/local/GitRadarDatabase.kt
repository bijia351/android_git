package com.anguomob.git.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.anguomob.git.data.local.dao.*
import com.anguomob.git.data.model.*

/**
 * GitHub技术雷达本地数据库
 */
@Database(
    entities = [
        Repository::class,
        Topic::class,
        SearchHistory::class,
        Favorite::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GitRadarDatabase : RoomDatabase() {
    
    abstract fun repositoryDao(): RepositoryDao
    abstract fun topicDao(): TopicDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    
    companion object {
        @Volatile
        private var INSTANCE: GitRadarDatabase? = null
        
        private const val DATABASE_NAME = "git_radar_database"
        
        fun getDatabase(context: Context): GitRadarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GitRadarDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // 在开发阶段使用，生产环境需要正确的迁移策略
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 清空所有数据
         */
        suspend fun clearAllData(database: GitRadarDatabase) {
            database.repositoryDao().clearAll()
            database.topicDao().clearAll()
            database.searchHistoryDao().clearAllSearchHistory()
            database.favoriteDao().clearAllFavorites()
        }
    }
}
