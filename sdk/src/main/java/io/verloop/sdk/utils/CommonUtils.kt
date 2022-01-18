package io.verloop.sdk.utils

class CommonUtils {

    companion object {

        // Convert a 3 digit hex code into 6 digit hex code
        fun getExpandedColorHex(color: String?): String {
            if(color === null) return "#FFFFFF"
            if (color.length == 4) {
                return color?.replace(
                    "#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])".toRegex(), "#$1$1$2$2$3$3"
                )
            }
            return color
        }
    }
}