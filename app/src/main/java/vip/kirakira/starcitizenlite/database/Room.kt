package vip.kirakira.starcitizenlite.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ShopItemDao {
    @Query("SELECT * FROM shop_items ORDER BY price DESC")
    fun getAll(): LiveData<List<ShopItem>>

    @Query("SELECT * FROM shop_items WHERE id = :id")
    fun getById(id: Int): LiveData<ShopItem>

//    @Query("SELECT * FROM shop_items WHERE name LIKE :name")
//    fun getByName(name: String): LiveData<List<ShopItem>>
//
//    @Query("SELECT * FROM shop_items WHERE type = :type")
//    fun getByType(type: String): LiveData<List<ShopItem>>
//
//    @Query("SELECT * FROM shop_items WHERE type = :type ORDER BY price ASC")
//    fun getByTypeAsc(type: String): LiveData<List<ShopItem>>
//
//    @Query("SELECT * FROM shop_items WHERE type = :type ORDER BY price DESC")
//    fun getByTypeDesc(type: String): LiveData<List<ShopItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(shopItems: List<ShopItem>)


}

@Database(entities = [ShopItem::class], version = 1)
abstract class ShopItemDatabase: RoomDatabase() {
    abstract val shopItemDao: ShopItemDao
}

private lateinit var INSTANCE: ShopItemDatabase

fun getDatabase(context: Context): ShopItemDatabase {
    synchronized(ShopItemDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                ShopItemDatabase::class.java,
                "videos").build()
        }
    }
    return INSTANCE
}

