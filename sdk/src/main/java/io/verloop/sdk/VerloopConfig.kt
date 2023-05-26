package io.verloop.sdk

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class VerloopConfig private constructor(
    var clientId: String?,
    var userId: String?,
    var fcmToken: String?,
    var userName: String?,
    var userEmail: String?,
    var userPhone: String?,
    var recipeId: String?,
    var department: String?,
    var isStaging: Boolean = false,
    var closeExistingChat: Boolean = false,
    var overrideUrlClick: Boolean = false,
    var fields: ArrayList<CustomField> = ArrayList()
) : Parcelable {

    @Deprecated("Use builder instead")
    constructor(clientId: String) : this(
        clientId,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        ArrayList()
    )

    @Deprecated("Use builder instead")
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
        this.department = source.readString()
        this.isStaging = source.readInt() == 1
        this.closeExistingChat = source.readInt() == 1
        this.overrideUrlClick = source.readInt() == 1
        this.fields =
            source.readArrayList(CustomField::class.java.classLoader) as ArrayList<CustomField>
    }

    var buttonOnClickListener: LiveChatButtonClickListener? = null
    var chatUrlClickListener: LiveChatUrlClickListener? = null

    fun putCustomField(key: String, value: String, scope: Scope) {
        fields.add(CustomField(key, value, scope))
    }

    fun putCustomField(key: String, value: String) {
        fields.add(CustomField(key, value, null))
    }

    /**
     * Callback for button click event from within the chat
     * @param buttonOnClickListener
     */
    fun setButtonClickListener(buttonOnClickListener: LiveChatButtonClickListener) {
        this.buttonOnClickListener = buttonOnClickListener
    }

    /**
     * Callback for url click event from within the chat
     * @param urlClickListener
     */
    fun setUrlClickListener(
        urlClickListener: LiveChatUrlClickListener,
        overrideUrlClick: Boolean = false
    ) {
        this.chatUrlClickListener = urlClickListener
        this.overrideUrlClick = overrideUrlClick
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.clientId)
        dest.writeString(this.userId)
        dest.writeString(this.fcmToken)
        dest.writeString(this.userName)
        dest.writeString(this.userEmail)
        dest.writeString(this.userPhone)
        dest.writeString(this.recipeId)
        dest.writeString(this.department)
        dest.writeByte((if (this.isStaging) 1 else 0).toByte())
        dest.writeByte((if (this.closeExistingChat) 1 else 0).toByte())
        dest.writeByte((if (this.overrideUrlClick) 1 else 0).toByte())
        dest.writeList(this.fields)
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

    class CustomField(var key: String?, var value: String?, var scope: Scope?) : Parcelable {

        override fun describeContents(): Int {
            return 0
        }

        constructor() : this(
            null, null, Scope.USER
        )

        constructor(source: Parcel) : this() {
            this.key = source.readString()
            this.value = source.readString()
            this.scope = Scope.values()[source.readInt()]
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(this.key)
            dest.writeString(this.value)
            this.scope?.ordinal?.let { dest.writeInt(it) }
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<CustomField> {
                override fun createFromParcel(parcel: Parcel) = CustomField(parcel)
                override fun newArray(size: Int) = arrayOfNulls<CustomField>(size)
            }
        }

    }

    data class Builder(
        var clientId: String? = null,
        var userId: String? = null,
        var fcmToken: String? = null,
        var userName: String? = null,
        var userEmail: String? = null,
        var userPhone: String? = null,
        var recipeId: String? = null,
        var department: String? = null,
        var isStaging: Boolean = false,
        var closeExistingChat: Boolean = false,
        var overrideUrlClick: Boolean = false,
        var fields: ArrayList<CustomField> = ArrayList()
    ) {
        fun clientId(clientId: String?) = apply { this.clientId = clientId }
        fun userId(userId: String?) = apply {
            if (!userId.equals(""))
                this.userId = userId
        }

        fun fcmToken(fcmToken: String?) = apply { this.fcmToken = fcmToken }
        fun userName(userName: String?) = apply { this.userName = userName }
        fun userEmail(userEmail: String?) = apply { this.userEmail = userEmail }
        fun userPhone(userPhone: String?) = apply { this.userPhone = userPhone }
        fun recipeId(recipeId: String?) = apply { this.recipeId = recipeId }
        fun department(department: String?) = apply { this.department = department }
        fun isStaging(isStaging: Boolean) = apply { this.isStaging = isStaging }
        fun closeExistingChat(closeExistingChat: Boolean) = apply { this.closeExistingChat = closeExistingChat }
        fun overrideUrlClick(overrideUrlClick: Boolean) =
            apply { this.overrideUrlClick = overrideUrlClick }

        fun fields(fields: ArrayList<CustomField>) = apply { this.fields = fields }

        @Throws(VerloopException::class)
        fun build(): VerloopConfig {
            if (clientId.isNullOrEmpty()) throw VerloopException("Client id cannot be null or empty")
            return VerloopConfig(
                clientId,
                userId,
                fcmToken,
                userName,
                userEmail,
                userPhone,
                recipeId,
                department,
                isStaging,
                closeExistingChat,
                overrideUrlClick,
                fields
            )
        }
    }
}