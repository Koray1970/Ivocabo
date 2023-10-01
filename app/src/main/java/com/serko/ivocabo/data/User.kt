package com.serko.ivocabo.data

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.serko.ivocabo.api.IApiService
import com.serko.ivocabo.remote.device.addupdate.DeviceAddUpdateRequest
import com.serko.ivocabo.remote.device.list.DeviceListResponse
import com.serko.ivocabo.remote.membership.EventResult
import com.serko.ivocabo.remote.membership.EventResultFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
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
    var devices = MutableStateFlow<String?>(userDao.getDevices())

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
class userViewModel @Inject constructor(private val repo: UserRepository) : ViewModel() {
    val gson = Gson()
    var user: User?
    var isUserSignIn = MutableStateFlow(false)
    var devicelist = mutableListOf<Device>()

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
    fun getDbDeviceList() {
        try {
            var dbdevices = repo.getDevices()
            if (dbdevices != null) {
                if (dbdevices.length > 0) {
                    devicelist= gson.fromJson<ArrayList<Device>>(
                        dbdevices,
                        object : TypeToken<ArrayList<Device>>() {}.type
                    ).toMutableList()

                }
            }
        } catch (e: Exception) {

        }
    }

    fun addUpdateDevice(device: Device): Boolean {
        try {
            user = repo.fetchUser()
            if (!user!!.devices.isNullOrBlank()) {
                var gDeviceList =
                    gson.fromJson<ArrayList<Device>>(
                        user!!.devices,
                        object : TypeToken<ArrayList<Device>>() {}.type
                    )
                var currentDevice = gDeviceList!!.find { it.macaddress == device.macaddress }
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
                null, device.devicetype, device.ismissing,
                device.istracking, device.latitude, device.longitude,
                device.macaddress, device.name, device.newmacaddress
            )

            val call: Call<EventResult> =
                apiSrv?.srvAddUpdateDevice("Bearer ${user?.token!!} ", dEviceRequest)!!
            call!!.enqueue(object : Callback<EventResult> {
                override fun onResponse(
                    call: Call<EventResult>,
                    response: Response<EventResult>,
                ) {
                    if (response.isSuccessful) {
                        var rmResult = response.body()!!
                        if (rmResult.eventresultflag == EventResultFlags.SUCCESS.flag) {

                        }
                    }
                }

                override fun onFailure(call: Call<EventResult>, t: Throwable) {

                }
            })
            return true
        } catch (e: Exception) {
            Log.v("userViewModel.addUpdateDevice", "Exception : ${e.message}")
        }
        return false
    }
}

