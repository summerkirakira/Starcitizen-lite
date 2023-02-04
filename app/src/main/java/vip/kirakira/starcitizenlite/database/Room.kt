package vip.kirakira.starcitizenlite.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Dao
interface ShipDao {
    @Query("SELECT * FROM hangar_ship")
    fun getAll(): LiveData<List<HangarShip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(hangarShips: List<HangarShip>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(hangarShip: HangarShip)

    @Query("Delete FROM hangar_ship")
    fun deleteAll()

    @Query("SELECT * FROM hangar_ship where id = :id")
    fun getById(id: Int): LiveData<HangarShip>
}

@Dao
interface HangarLogDao {
    @Query("SELECT * FROM hangar_log ORDER BY time DESC")
    fun getAll(): LiveData<List<HangarLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(hangarLogs: List<HangarLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(hangarLog: HangarLog)

    @Query("Delete FROM hangar_log")
    fun deleteAll()

    @Query("SELECT * FROM hangar_log where id = :id")
    fun getById(id: String): LiveData<HangarLog>

    @Query("SELECT * FROM hangar_log where target = :target")
    fun getByTarget(target: String): LiveData<List<HangarLog>>

    @Query("SELECT count(*) FROM hangar_log")
    fun getCount(): LiveData<Int>

    @Query("SELECT * FROM hangar_log where target = :target ORDER BY time DESC")
    fun getByTargetDesc(target: String): LiveData<List<HangarLog>>
}

@Dao
interface ShipUpgradeDao {
    @Query("SELECT * FROM ship_upgrade")
    fun getAll(): LiveData<List<ShipUpgrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(shipUpgrades: List<ShipUpgrade>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(shipUpgrade: ShipUpgrade)

    @Query("Delete FROM ship_upgrade")
    fun deleteAll()

    @Query("SELECT * FROM ship_upgrade where skuId = :id")
    fun getById(id: Int): LiveData<ShipUpgrade>

    @Query("UPDATE ship_upgrade SET isAvailable = :isAvailable WHERE skuId = :id")
    fun updateIsAvailable(id: Int, isAvailable: Boolean)

    @Query("SELECT * FROM ship_upgrade where name = :name and edition = :edition")
    fun getByName(name: String, edition: String="Standard Edition"): ShipUpgrade?

    @Query("SELECT * FROM ship_upgrade where name like '%' || :name || '%' and edition = :edition limit 1")
    fun getByNameLike(name: String, edition: String="Standard Edition"): ShipUpgrade?
}

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translation")
    fun getAll(): LiveData<List<Translation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(translations: List<Translation>)

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM translation WHERE product_id = :product_id) THEN 1 ELSE 0 END")
    fun isProductExist(product_id: Int): Boolean

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM translation WHERE type='upgrade' AND from_ship=:from_ship AND to_ship=:to_ship) THEN 1 ELSE 0 END")
    fun isUpgradeExist(from_ship: String, to_ship: String): Boolean

    @Query("SELECT * FROM translation WHERE product_id = :product_id")
    fun getByProductId(product_id: Int): Translation?

    @Query("DELETE FROM translation")
    fun deleteAll()

    @Query("SELECT * FROM translation WHERE english_title = :english_title limit 1")
    fun getByEnglishTitle(english_title: String): Translation?

    @Query("SELECT * FROM translation WHERE type = 'product' AND english_title LIKE :english_title limit 1")
    fun getByEnglishTitleLike(english_title: String): Translation?
//
//    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM translation WHERE type='buyback' AND english_title=:english_title) THEN 1 ELSE 0 END")
//    fun isBuybackExist(english_title: String): Boolean
}

@Dao
interface ShipDetailDao {
    @Query("SELECT * FROM ship_detail")
    fun getAll(): LiveData<List<ShipDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(shipDetails: List<ShipDetail>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(shipDetail: ShipDetail)

    @Query("Delete FROM ship_detail")
    fun deleteAll()

    @Query("SELECT * FROM ship_detail where id = :id")
    fun getById(id: String): LiveData<ShipDetail>

    @Query("SELECT * FROM ship_detail where rsiName like '%' || :rsiName || '%' limit 1")
    fun getByName(rsiName: String): ShipDetail?
}

//@Database(entities = [ShopItem::class, HangerItem::class, HangerPackage::class, BuybackItem::class, User::class, BannerImage::class], version = 1)
//abstract class ShopItemDatabase0: RoomDatabase() {
//    abstract val shopItemDao: ShopItemDao
//    abstract val hangerItemDao: HangerItemDao
//    abstract val buybackItemDao: BuybackItemDao
//    abstract val userDao: UserDao
//    abstract val bannerDao: BannerDao
//}


val MIGRATION_2_5 = object : Migration(2, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
                ALTER TABLE hanger_packages ADD COLUMN currentPrice Integer DEFAULT 0 NOT NULL;
            """.trimIndent()
        )
        database.execSQL(
            """
                CREATE TABLE `hangar_log` 
                (`id` TEXT NOT NULL,
                `time` INTEGER NOT NULL, 
                `type` TEXT NOT NULL, 
                `name` TEXT NOT NULL,
                `chineseName` TEXT DEFAULT NULL,
                `price` INTEGER DEFAULT NULL, 
                `source` TEXT DEFAULT NULL,
                `target` TEXT DEFAULT NULL, 
                `operator` TEXT DEFAULT NULL,
                `reason` TEXT DEFAULT NULL,
                `order` TEXT DEFAULT NULL,
                `insert_time` INTEGER NOT NULL,
                PRIMARY KEY(`id`))
            """.trimIndent()
        )
        database.execSQL(
            """
                CREATE TABLE `hangar_ship` 
                (`id` INTEGER NOT NULL,
                `name` TEXT NOT NULL, 
                `shipId` INTEGER DEFAULT NULL, 
                `packageId` INTEGER NOT NULL,
                `packageTitle` TEXT NOT NULL,
                `imageUrl` TEXT NOT NULL,
                `chinesePackageTitle` TEXT DEFAULT NULL,
                `image` TEXT NOT NULL,
                `price` INTEGER NOT NULL,
                `current_price` INTEGER NOT NULL,
                `is_upgrade` INTEGER NOT NULL,
                `insurance` INTEGER NOT NULL,
                `date` INTEGER NOT NULL,
                `isGiftable` INTEGER NOT NULL,
                `packagePrice` INTEGER NOT NULL,
                `isReclaimable` INTEGER NOT NULL,
                `receiveTime` INTEGER NOT NULL,
                `insert_time` INTEGER NOT NULL,
                PRIMARY KEY(`id`))
            """.trimIndent()
        )
        database.execSQL(
            """
                CREATE TABLE `ship_upgrade` 
                (`skuId` INTEGER NOT NULL,
                `shipId` INTEGER NOT NULL, 
                `name` TEXT NOT NULL,
                `isFlyable` INTEGER NOT NULL,
                `focus` TEXT NOT NULL,
                `link` TEXT NOT NULL,
                `manufacturer` TEXT NOT NULL,
                `productThumbMediumAndSmall` TEXT NOT NULL,
                `slideShow` TEXT NOT NULL,
                `price` INTEGER NOT NULL,
                `edition` TEXT NOT NULL,
                `isAvailable` INTEGER NOT NULL,
                PRIMARY KEY(`skuId`))
            """.trimIndent()
        )
        database.execSQL(
            """
                CREATE TABLE `ship_detail` 
                (`id` TEXT NOT NULL,
                `storeUrl` TEXT NOT NULL,
                `description` TEXT DEFAULT NULL,
                `beam` REAL NOT NULL,
                `classification` TEXT NOT NULL,
                `hasModules` INTEGER NOT NULL,
                'name' TEXT NOT NULL,
                `maxCrew` INTEGER DEFAULT NULL,
                `price` REAL DEFAULT NULL,
                `rsiName` TEXT NOT NULL,
                `rsiSlug` TEXT NOT NULL,
                `lastPledgePrice` REAL DEFAULT NULL,
                `length` REAL NOT NULL,
                `height` REAL NOT NULL,
                `focus` TEXT NOT NULL,
                `mass` REAL NOT NULL,
                `size` TEXT DEFAULT NULL,
                `storeImageSmall` TEXT NOT NULL,
                `storeImageMedium` TEXT NOT NULL,
                `storeImageLarge` TEXT NOT NULL,
                PRIMARY KEY(`id`))
            """.trimIndent()
        )
    }
}

@Database(
    entities = [
        ShopItem::class,
        HangerItem::class,
        HangerPackage::class,
        BuybackItem::class,
        User::class,
        BannerImage::class,
        Translation::class,
        HangarShip::class,
        HangarLog::class,
        ShipUpgrade::class,
        ShipDetail::class
               ],
//    autoMigrations = [
//        AutoMigration (
//            from = 2,
//            to = 5,
//            spec = ShopItemDatabase.MyAutoMigration::class
//        )
//    ],
    exportSchema = true,
    version = 5)
abstract class ShopItemDatabase: RoomDatabase() {
    abstract val shopItemDao: ShopItemDao
    abstract val hangerItemDao: HangerItemDao
    abstract val buybackItemDao: BuybackItemDao
    abstract val userDao: UserDao
    abstract val bannerDao: BannerDao
    abstract val translationDao: TranslationDao
    abstract val hangarLogDao: HangarLogDao
    abstract val shipUpgradeDao: ShipUpgradeDao
    abstract val shipDetailDao: ShipDetailDao

    class MyAutoMigration : AutoMigrationSpec

}

private lateinit var INSTANCE: ShopItemDatabase

fun getDatabase(context: Context): ShopItemDatabase {
    synchronized(ShopItemDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                ShopItemDatabase::class.java,
                "shops")
                .addMigrations(MIGRATION_2_5)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}


