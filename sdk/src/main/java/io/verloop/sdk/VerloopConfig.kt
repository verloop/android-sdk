package io.verloop.sdk

import android.os.Parcelable
import io.verloop.sdk.model.HeaderConfig
import io.verloop.sdk.model.LogLevel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
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
    var overrideHeaderLayout: Boolean = false,
    var openMenuWidgetOnStart: Boolean = false,
    var headerConfig: HeaderConfig? = null,
    var fields: ArrayList<CustomField> = ArrayList(),
    var allowFileDownload: Boolean = false
) : Parcelable {

    var logLevel: LogLevel = LogLevel.WARNING

    var buttonOnClickListener: LiveChatButtonClickListener? = null
    var chatUrlClickListener: LiveChatUrlClickListener? = null

    var logEventListener: LiveLogEventListener? = null
        private set

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
        false,
        false,
        null,
        ArrayList(),
        false
    )

    @Deprecated("Use builder instead")
    constructor(clientId: String, userId: String) : this(clientId) {
        this.userId = userId
    }

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

    /**
     * Callback for log events from within the chat
     * @param logEventListener
     */
    fun setLogEventListener(
        logEventListener: LiveLogEventListener,
        logLevel: LogLevel
    ) {
        this.logEventListener = logEventListener
        this.logLevel = logLevel
    }

    enum class Scope {
        USER, ROOM
    }

    @Parcelize
    class CustomField(var key: String?, var value: String?, var scope: Scope?) : Parcelable {

        constructor() : this(
            null, null, Scope.USER
        )

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
        var overrideHeaderLayout: Boolean = false,
        var openMenuWidgetOnStart: Boolean = false,
        var headerConfig: HeaderConfig? = null,
        var fields: ArrayList<CustomField> = ArrayList(),
        var allowFileDownload: Boolean = false,
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

        @Deprecated("Not required anymore", ReplaceWith(""))
        fun isStaging(isStaging: Boolean) = apply { this.isStaging = isStaging }

        fun closeExistingChat(closeExistingChat: Boolean) =
            apply { this.closeExistingChat = closeExistingChat }

        fun overrideUrlClick(overrideUrlClick: Boolean) =
            apply { this.overrideUrlClick = overrideUrlClick }

        /**
         * Override the toolbar configuration received from server with toolbar_verloop.xml defined in main app.
         * Can't use along with headerConfig. Either use headerConfig or overrideHeaderLayout
         * @param overrideHeaderLayout
         */
        fun overrideHeaderLayout(overrideHeaderLayout: Boolean) =
            apply { this.overrideHeaderLayout = overrideHeaderLayout }

        fun allowFileDownload(allowFileDownload: Boolean) =
            apply { this.allowFileDownload = allowFileDownload }

        fun openMenuWidgetOnStart(openMenuWidgetOnStart: Boolean) =
            apply { this.openMenuWidgetOnStart = openMenuWidgetOnStart }

        /**
         * Override the toolbar configuration received from server with provided header configuration.
         * Can't use along with overrideHeaderLayout. Either use overrideHeaderLayout or headerConfig
         * @param headerConfig
         */
        fun headerConfig(headerConfig: HeaderConfig?) = apply { this.headerConfig = headerConfig }

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
                overrideHeaderLayout,
                openMenuWidgetOnStart,
                headerConfig,
                fields,
                allowFileDownload
            )
        }
    }
}