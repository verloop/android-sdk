package io.verloop.sdk.model

import android.os.Parcelable
import androidx.annotation.Keep
import io.verloop.sdk.enum.Position
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
class HeaderConfig(
    var brandLogo: String? = null,
    var title: String? = null,
    var titleColor: String? = null,
    var titlePosition: Position? = Position.UNDEFINED,
    var titleFontSize: Float? = 12.0f,
    var subtitle: String? = null,
    var subtitleColor: String? = null,
    var subtitlePosition: Position? = Position.UNDEFINED,
    var subtitleFontSize: Float? = 10.0f,
    var backgroundColor: String? = null,
) : Parcelable {
    data class Builder(
        var brandLogo: String? = null,
        var title: String? = null,
        var titleColor: String? = null,
        var titlePosition: Position? = Position.UNDEFINED,
        var titleFontSize: Float? = null,
        var subtitle: String? = null,
        var subtitleColor: String? = null,
        var subtitlePosition: Position? = Position.UNDEFINED,
        var subtitleFontSize: Float? = null,
        var backgroundColor: String? = null,
    ) {
        fun brandLogo(url: String) = apply { this.brandLogo = url }
        fun title(title: String) = apply { this.title = title }
        fun titleColor(titleColor: String) = apply { this.titleColor = titleColor }
        fun titlePosition(position: Position) = apply { this.titlePosition = position }
        fun titleFontSize(fontSize: Float) = apply { this.titleFontSize = fontSize }
        fun subtitle(subtitle: String) = apply { this.subtitle = subtitle }
        fun subtitleColor(subtitleColor: String) = apply { this.subtitleColor = subtitleColor }
        fun subtitlePosition(position: Position) = apply { this.subtitlePosition = position }
        fun subtitleFontSize(fontSize: Float) = apply { this.subtitleFontSize = fontSize }
        fun backgroundColor(backgroundColor: String) =
            apply { this.backgroundColor = backgroundColor }

        fun build() = HeaderConfig(
            brandLogo,
            title,
            titleColor,
            titlePosition,
            titleFontSize,
            subtitle,
            subtitleColor,
            subtitlePosition,
            subtitleFontSize,
            backgroundColor,
        )
    }

    fun overrideConfig(headerConfig: HeaderConfig?) {
        if (headerConfig != null) {
            if (this.brandLogo.isNullOrEmpty()) this.brandLogo = headerConfig.brandLogo
            if (this.title.isNullOrEmpty()) this.title = headerConfig.title
            if (this.titleColor.isNullOrEmpty()) this.titleColor = headerConfig.titleColor
            if (this.titlePosition == Position.UNDEFINED) this.titlePosition = headerConfig.titlePosition
            if (this.titleFontSize == null) this.titleFontSize = headerConfig.titleFontSize
            if (this.subtitle.isNullOrEmpty()) this.subtitle = headerConfig.subtitle
            if (this.subtitleColor.isNullOrEmpty()) this.subtitleColor = headerConfig.subtitleColor
            if (this.subtitlePosition == Position.UNDEFINED) this.subtitlePosition = headerConfig.subtitlePosition
            if (this.subtitleFontSize == null) this.subtitleFontSize = headerConfig.subtitleFontSize
            if (this.backgroundColor.isNullOrEmpty()) this.backgroundColor =
                headerConfig.backgroundColor
        }
    }
}
