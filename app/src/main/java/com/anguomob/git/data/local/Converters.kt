package com.anguomob.git.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.anguomob.git.data.model.User
import com.anguomob.git.data.model.License
import com.anguomob.git.data.model.SearchType
import com.anguomob.git.data.model.FavoriteType

/**
 * Room 数据库类型转换器
 * 用于转换复杂数据类型为基础类型以便存储到SQLite数据库
 */
class Converters {
    
    private val gson = Gson()
    
    // String List 转换器
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, listType)
        }
    }
    
    // User 转换器
    @TypeConverter
    fun fromUser(user: User?): String? {
        return if (user == null) null else gson.toJson(user)
    }
    
    @TypeConverter
    fun toUser(value: String?): User? {
        return if (value == null) null else gson.fromJson(value, User::class.java)
    }
    
    // License 转换器
    @TypeConverter
    fun fromLicense(license: License?): String? {
        return if (license == null) null else gson.toJson(license)
    }
    
    @TypeConverter
    fun toLicense(value: String?): License? {
        return if (value == null) null else gson.fromJson(value, License::class.java)
    }
    
    // SearchType 枚举转换器
    @TypeConverter
    fun fromSearchType(searchType: SearchType): String {
        return searchType.name
    }
    
    @TypeConverter
    fun toSearchType(value: String): SearchType {
        return try {
            SearchType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // Handle old, invalid data by returning a default value
            SearchType.REPOSITORIES
        }
    }
    
    // FavoriteType 枚举转换器
    @TypeConverter
    fun fromFavoriteType(favoriteType: FavoriteType): String {
        return favoriteType.name
    }
    
    @TypeConverter
    fun toFavoriteType(value: String): FavoriteType {
        return FavoriteType.valueOf(value)
    }
}
