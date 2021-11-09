package io.verloop.sdk

import android.os.Parcel
import android.os.Parcelable
import java.util.*


data class VerloopConfig(var clientId: String,
                         var userId: String? = UUID.randomUUID().toString(),
                         var fcmToken: String?,
                         var userName: String?,
                         var userEmail: String?,
                         var userPhone: String?,
                         var recipeId: String?,
                         var isStaging: Boolean = false,
                         var fields: ArrayList<CustomFieldV2> = ArrayList()) : Parcelable {


    constructor(clientId: String) : this(clientId, UUID.randomUUID().toString(), null, null, null, null, null, false, ArrayList())

    constructor(clientId: String, userId: String) : this(clientId) {
        this.userId = userId
    }

    constructor(source: Parcel) : this(source.readString().toString()) {
        this.userId = source.readString()
        this.fcmToken = source.readString()
        this.userName = source.readString()
        this.userEmail = source.readString()
        this.userPhone = source.readString()
        this.recipeId = source.readString()
        this.isStaging = source.readInt() == 1
        this.fields = source.readArrayList(CustomFieldV2::class.java.classLoader) as ArrayList<CustomFieldV2>
    }

    var buttonOnClickListener: LiveChatButtonClickListener? = null
    var urlClickListener: LiveChatUrlClickListener? = null

    fun putCustomField(key: String, value: String, scope: Scope) {
        fields.add(CustomFieldV2(key, value, scope))
    }

    fun putCustomField(key: String, value: String) {
        fields.add(CustomFieldV2(key, value, null))
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(this.clientId)
        dest?.writeString(this.userId)
        dest?.writeString(this.fcmToken)
        dest?.writeString(this.userName)
        dest?.writeString(this.userEmail)
        dest?.writeString(this.userPhone)
        dest?.writeString(this.recipeId)
        dest?.writeByte((if (this.isStaging) 1 else 0).toByte())
        dest?.writeList(this.fields)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<VerloopConfig> {
            override fun createFromParcel(parcel: Parcel) = VerloopConfig(parcel)
            override fun newArray(size: Int) = arrayOfNulls<VerloopConfig>(size)
        }
    }

    enum class Scope {
        USER, ROOM
    }

    class CustomFieldV2(val key: String, var value: String, var scope: Scope?)
}