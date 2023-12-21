package com.serko.ivocabo.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.util.Log
import androidx.collection.emptyIntFloatMap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.serko.ivocabo.Helper
import com.serko.ivocabo.IvocaboApplication
import com.serko.ivocabo.R
import com.serko.ivocabo.api.IApiService
import com.serko.ivocabo.bluetooth.BluetoothActivity
import com.serko.ivocabo.bluetooth.ScanningDeviceItem
import com.serko.ivocabo.remote.device.addupdate.DeviceAddUpdateRequest
import com.serko.ivocabo.remote.membership.EventResult
import com.serko.ivocabo.remote.membership.EventResultFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "registerdate")
    val registerdate: Date,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "token")
    var token: String?,
    @ColumnInfo(name = "devices")
    var devices: String?,
)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg users: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT COUNT(*) FROM users")
    fun count(): Int

    @Query("SELECT * FROM users WHERE username= :username AND email= :email LIMIT 1")
    fun getUser(username: String, email: String): User


    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    fun fetchUser(): User

    @Query("SELECT devices FROM users ORDER BY id DESC LIMIT 1")
    fun getDevices(): String?
}

class UserRepository @Inject constructor(private val userDao: UserDao) {
    var user = MutableStateFlow<User>(userDao.fetchUser())

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    fun insertUser(user: User) {
        coroutineScope.launch {
            userDao.insert(user)
        }
    }

    fun updateUser(user: User) {
        coroutineScope.launch {
            userDao.update(user)
        }
    }

    fun deleteUser(user: User) {
        coroutineScope.launch {
            userDao.delete(user)
        }
    }

    fun getUser(username: String, email: String) {
        coroutineScope.launch {
            user.value = userDao.getUser(username, email)
        }
    }

    fun fetchUser(): User {
        return userDao.fetchUser()
    }

    fun getCountOfUser(): Int {
        return userDao.count()
    }

    fun getDevices(): String? {
        return userDao.getDevices()
    }

}

@HiltViewModel
class userViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: UserRepository
) : ViewModel() {
    val helper = Helper()
    val gson = Gson()
    var user: User?
    var isUserSignIn = MutableStateFlow(false)

    var devicelist = mutableListOf<Device>()


    var mutablelivedataRMEventResult =
        MutableStateFlow<RMEventResult<Boolean>>(RMEventResult(false))

    init {
        user = repo.fetchUser()
        //getDbDeviceList()
    }

    fun getCountofUser(): Int {
        return repo.getCountOfUser()
    }

    fun getUser(username: String, email: String) {
        viewModelScope.launch {
            repo.getUser(username, email)
            user = repo.user.value
        }
    }

    fun fetchUser(): User {
        val dbresult = repo.fetchUser()
        user = dbresult
        return dbresult
    }

    fun signInUser() {
        viewModelScope.launch {
            val dbUserResult = repo.fetchUser()
            if (!dbUserResult.token.isNullOrBlank())
                isUserSignIn.value = true
        }
    }

    fun insertUser(user: User) {
        viewModelScope.launch {
            repo.insertUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repo.updateUser(user)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repo.deleteUser(user)
        }
    }

    //device events
    fun getDeviceFlowList(): Flow<MutableList<Device>> = flow {
        while (true) {
            var dbdevices = repo.getDevices()
            if (dbdevices != null) {
                if (dbdevices.isNotEmpty()) {
                    emit(
                        gson.fromJson<List<Device>>(
                            dbdevices,
                            object : TypeToken<List<Device>>() {}.type
                        ).sortedBy { a -> a.name }.toMutableStateList()
                    )
                }
            }
            delay(1600)
        }
    }

    fun getScanDeviceList(): Flow<MutableList<String>> =
        flow<MutableList<String>> {
            var mList = mutableListOf<String>()
            while (true) {
                var dbdevices = repo.getDevices()
                if (dbdevices != null) {
                    if (dbdevices.isNotEmpty()) {
                        val dd = gson.fromJson<List<Device>>(
                            dbdevices,
                            object : TypeToken<List<Device>>() {}.type
                        ).toMutableStateList()
                        if (dd.isNotEmpty()) {
                            dd.forEach { f ->
                                if (f.istracking != null && f.istracking == true) {
                                    if (mList.none { a -> a == f.macaddress.uppercase() })
                                        mList.add(f.macaddress.uppercase())
                                    //BluetoothActivity.scanningDeviceList.add(ScanningDeviceItem(f.macaddress.uppercase()))
                                }
                            }
                        } else {
                            mList.clear()
                        }
                    } else
                        mList.clear()
                } else
                    mList.clear()
                emit(mList)
                delay(2000)
            }
        }//.distinctUntilChanged()


    fun getDbDeviceList() {
        try {
            var dbdevices = repo.getDevices()
            if (dbdevices != null) {
                if (dbdevices.length > 0) {
                    devicelist = gson.fromJson<List<Device>>(
                        dbdevices,
                        object : TypeToken<List<Device>>() {}.type
                    ).toMutableList()
                }
            }
        } catch (e: Exception) {

        }
    }

    fun getDeviceDetail(macaddress: String): Device? {
        try {
            val dDevicecol = repo.getDevices()
            if (dDevicecol != null) {
                if (dDevicecol.length > 0) {
                    var listOfDevice = gson.fromJson<List<Device>>(
                        dDevicecol,
                        object : TypeToken<List<Device>>() {}.type
                    )
                    if (listOfDevice.any { a -> a.macaddress == macaddress }) {
                        return listOfDevice.first { a -> a.macaddress == macaddress }
                    }
                }
            }
        } catch (e: Exception) {

        }
        return null
    }

    fun addUpdateDevice(device: Device): Flow<FormActionResult<Boolean>?> = callbackFlow {

        var funResult: FormActionResult<Boolean>? = null

        try {
            if (BluetoothAdapter.checkBluetoothAddress(helper.formatedMacAddress(device.macaddress))) {
                user = repo.fetchUser()
                if (!user!!.devices.isNullOrBlank()) {
                    var gDeviceList =
                        gson.fromJson<ArrayList<Device>>(
                            user!!.devices,
                            object : TypeToken<ArrayList<Device>>() {}.type
                        )
                    var currentDevice =
                        gDeviceList!!.find { it.macaddress == device.macaddress }
                    if (currentDevice != null) {
                        gDeviceList.remove(currentDevice)
                        gDeviceList.add(device)
                        user!!.devices = gson.toJson(gDeviceList)
                    } else {
                        gDeviceList.add(device)
                        user!!.devices = gson.toJson(gDeviceList)
                    }
                } else {
                    var dList = ArrayList<Device>()
                    dList.add(device)
                    user!!.devices = gson.toJson(dList)
                }
                repo.updateUser(user!!)

                //device to remote db
                if (IApiService.apiService == null)
                    IApiService.getInstance()
                val apiSrv = IApiService.apiService


                val dEviceRequest = DeviceAddUpdateRequest(
                    date = helper.getNowAsJsonString(),
                    description = null,
                    devicetype = device.devicetype,
                    ismissing = device.ismissing,
                    istracking = device.istracking,
                    latitude = device.latitude,
                    longitude = device.longitude,
                    macaddress = device.macaddress.uppercase(Locale.ROOT),
                    name = device.name,
                    newmacaddress = device.newmacaddress
                )
                //Log.v("MainActivity", "${gson.toJson(dEviceRequest)}")
                val call: Call<EventResult> =
                    apiSrv?.srvAddUpdateDevice("Bearer ${user?.token!!} ", dEviceRequest)!!
                call.enqueue(object : Callback<EventResult> {
                    override fun onResponse(
                        call: Call<EventResult>,
                        response: Response<EventResult>
                    ) {
                        //Log.v("MainActivity","remote Response : ${response.raw().code}")
                        if (response.isSuccessful) {
                            if (response.body()?.eventresultflag == EventResultFlags.SUCCESS.flag) {

                                funResult = FormActionResult<Boolean>(true)
                                funResult?.resultFlag = FormActionResultFlag.Success
                                trySend(funResult)
                            } else {
                                funResult = FormActionResult<Boolean>(false)
                                funResult?.error = FormActionError()
                                var eMessage = ""
                                when (funResult?.error!!.code) {
                                    "DR015" -> eMessage = context.getString(R.string.DR015)
                                    "DR015.3" -> eMessage = context.getString(R.string.DR015_3)
                                    "DR015.2" -> eMessage = context.getString(R.string.DR015_2)
                                    "DR018" -> eMessage = context.getString(R.string.DR018)
                                    "DR017" -> eMessage = context.getString(R.string.DR017)
                                    "DR013" -> eMessage = context.getString(R.string.DR013)
                                    else -> eMessage = funResult?.error!!.exception.toString()
                                }
                                funResult?.error?.code = response.body()?.error!!.code
                                funResult?.error?.exception = eMessage
                                trySend(funResult)
                            }
                        }
                    }

                    override fun onFailure(call: Call<EventResult>, t: Throwable) {
                        Log.v("MainActivity", "t : ${t.message}")
                        funResult = FormActionResult<Boolean>(false)
                        funResult?.error = FormActionError()
                        funResult?.error?.exception = "Web Api bağlantı sorunu!"
                        trySend(funResult)
                    }

                })
            } else {
                funResult = FormActionResult<Boolean>(false)
                funResult?.error = FormActionError()
                funResult?.error?.code = "mac001"
                funResult?.error?.exception = context.getString(R.string.mac001)
                trySend(funResult)
            }
        } catch (e: Exception) {
            funResult = FormActionResult<Boolean>(false)
            funResult?.error = FormActionError()
            funResult?.error?.code = "000UVM001"
            funResult?.error?.exception = e.message
            trySend(funResult)
        }

        delay(100)
        awaitClose()
    }

    fun DeleteDevice(device: Device) {
        viewModelScope.launch {
            var gDeviceList =
                gson.fromJson<ArrayList<Device>>(
                    user!!.devices,
                    object : TypeToken<ArrayList<Device>>() {}.type
                )
            if (gDeviceList.size > 0) {
                gDeviceList.remove(device)
                user!!.devices = gson.toJson(gDeviceList)
                repo.updateUser(user!!)

                if (IApiService.apiService == null)
                    IApiService.getInstance()
                val apiSrv = IApiService.apiService


                val call: Call<EventResult> =
                    apiSrv?.srvDeviceRemove(
                        "Bearer ${user?.token!!} ",
                        macaddress = device.macaddress
                    )!!
                call.enqueue(object : Callback<EventResult> {
                    override fun onResponse(
                        call: Call<EventResult>,
                        response: Response<EventResult>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.eventresultflag == EventResultFlags.SUCCESS.flag) {
                                getDbDeviceList()
                            }
                        }
                    }

                    override fun onFailure(call: Call<EventResult>, t: Throwable) {
                        viewModelScope.launch {

                        }
                    }
                })
            }
        }
    }

}

enum class RMEventStatus { Initial, Running, Complete, Exception }
class RMEventResult<T>(t: T) {
    var stateStatus: RMEventStatus = RMEventStatus.Running
    var formEventResult: FormActionResult<T>? = null
}

