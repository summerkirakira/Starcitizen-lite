package vip.kirakira.starcitizenlite.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import vip.kirakira.starcitizenlite.ui.main.Banner

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

    @Query("DELETE FROM shop_items")
    fun deleteAll()
}

@Dao
interface HangerItemDao {
    @Transaction
    @Query("SELECT * FROM hanger_packages ORDER BY date DESC")
    fun getAll(): LiveData<List<HangerPackageWithItems>>

    @Query("Delete FROM hanger_packages where insert_time < :time")
    fun deleteAllOldPackage(time: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllPackages(hangerPackages: List<HangerPackage>)

    @Query("SELECT * FROM hanger_items")
    fun getAllItems(): LiveData<List<HangerItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllItems(hangerItems: List<HangerItem>)

    @Query("Delete FROM hanger_items where insert_time < :time")
    fun deleteAllOldItems(time: Long)

}

@Dao
interface BuybackItemDao {
    @Query("SELECT * FROM buyback ORDER BY date DESC")
    fun getAll(): LiveData<List<BuybackItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(buybackItems: List<BuybackItem>)

    @Query("Delete FROM buyback where insert_time < :time")
    fun deleteAllOldItems(time: Long)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): LiveData<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Query("Delete FROM user where id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM user where id = :id")
    fun getById(id: Int): LiveData<User>

    @Query("SELECT * FROM user where handle = :handle")
    fun getByHandle(handle: String): LiveData<User>
}

@Dao
interface BannerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(Banners: List<BannerImage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(Banner: BannerImage)

    @Query("SELECT * FROM banner_image")
    fun getAll(): LiveData<List<BannerImage>>
}

@Database(entities = [ShopItem::class, HangerItem::class, HangerPackage::class, BuybackItem::class, User::class, BannerImage::class], version = 1)
abstract class ShopItemDatabase: RoomDatabase() {
    abstract val shopItemDao: ShopItemDao
    abstract val hangerItemDao: HangerItemDao
    abstract val buybackItemDao: BuybackItemDao
    abstract val userDao: UserDao
    abstract val bannerDao: BannerDao
}

private lateinit var INSTANCE: ShopItemDatabase

fun getDatabase(context: Context): ShopItemDatabase {
    synchronized(ShopItemDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                ShopItemDatabase::class.java,
                "shops").build()
        }
    }
    return INSTANCE
}

