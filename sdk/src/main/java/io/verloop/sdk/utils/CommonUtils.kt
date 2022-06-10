package io.verloop.sdk.utils

class CommonUtils {

    companion object {

        // Convert a 3 digit hex code into 6 digit hex code
        fun getExpandedColorHex(color: String?): String {
            var mColor = color
            if (mColor === null) return "#FFFFFF"

            if (!mColor.startsWith("#") && (mColor.length == 3 || mColor.length == 6)) {
                mColor = "#$mColor"
            }
            if (mColor.length == 4) {
                return mColor.replace(
                    "#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])".toRegex(), "#$1$1$2$2$3$3"
                )
            }
            return mColor
        }
    }
}