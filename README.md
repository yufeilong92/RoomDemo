# RoomDemo
```
    implementation 'androidx.fragment:fragment-ktx:1.2.5'
    implementation "androidx.activity:activity-ktx:1.1.0"
    //选择导入
    implementation "com.google.android.material:material:1.2.1"

    // 必须导入
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.2.0"
    kapt  "androidx.lifecycle:lifecycle-compiler:2.2.0"
    implementation "androidx.room:room-runtime:2.3.0-alpha03"
    kapt "androidx.room:room-compiler:2.3.0-alpha03"
    implementation "androidx.room:room-rxjava2:2.3.0-alpha03"

    // RxJava     必须导入
    implementation "io.reactivex.rxjava2:rxandroid:2.1.0"
    implementation "androidx.room:room-rxjava2:2.3.0-alpha03"
```

#User
```

@Entity(tableName = "users")
data class User(@PrimaryKey
                @ColumnInfo(name = "userid")
                val id: String = UUID.randomUUID().toString(),
                @ColumnInfo(name = "username")
                val userName: String)
```
#UserDao
```
@Dao
interface UserDao {

    /**
     * Get a user by id.

     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Users WHERE userid = :id")
    fun getUserById(id: String): Flowable<User>

    /**
     * Insert a user in the database. If the user already exists, replace it.

     * @param user the user to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Completable

    /**
     * Delete all users.
     */
    @Query("DELETE FROM Users")
    fun deleteAllUsers()
}
```
#UsersDatabase
```
@Database(entities = [User::class], version = 1)
abstract class UsersDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var INSTANCE: UsersDatabase? = null

        fun getInstance(context: Context): UsersDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                UsersDatabase::class.java, "Sample.db"
            )
                .build()
    }
}


```
#UserViewModel
```
class UserViewModel(private val dataSource: UserDao) : ViewModel() {

    /**
     * Get the user name of the user.

     * @return a [Flowable] that will emit every time the user name has been updated.
     */
    // for every emission of the user, get the user name
    fun userName(): Flowable<String> {
        return dataSource.getUserById(USER_ID)
                .map { user -> user.userName }
    }

    /**
     * Update the user name.
     * @param userName the new user name
     * *
     * @return a [Completable] that completes when the user name is updated
     */
    fun updateUserName(userName: String): Completable {
        val user = User(USER_ID, userName)
        return dataSource.insertUser(user)
    }

    companion object {
        // using a hardcoded value for simplicity
        const val USER_ID = "1"
    }
}

```

#ViewModelFactory
```
class ViewModelFactory(private val dataSource: UserDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

```
#Injection
```
object Injection {

    fun provideUserDataSource(context: Context): UserDao {
        val database = UsersDatabase.getInstance(context)
        return database.userDao()
    }

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        val dataSource = provideUserDataSource(context)
        return ViewModelFactory(dataSource)
    }
}

```

#MainActivity
```
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: UserViewModel by viewModels { viewModelFactory }

    private val disposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModelFactory = Injection.provideViewModelFactory(this)
        btn_add.setOnClickListener {
            upDataName()
        }
    }

    override fun onStart() {
        super.onStart()
        disposable.add(
                viewModel.userName()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            tv_content.text = it
                        }, {})
        )

    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    private fun upDataName() {
        val com = et_content.text.toString()
        disposable.add(
                viewModel.updateUserName(com)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({

                        }, {

                        })

        )

    }

```




