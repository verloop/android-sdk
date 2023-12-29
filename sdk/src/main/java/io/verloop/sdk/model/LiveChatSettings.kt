package io.verloop.sdk.model

import androidx.annotation.Keep

@Keep
class LivechatSettings {
    var Header: Header? = null
    var Theme: Theme? = null
}

@Keep
class BrandLogo {
    var URL: String? = null
}

@Keep
class Header {
    var Title: Title? = null
    var Subtitle: Subtitle? = null
    var BrandLogo: BrandLogo? = null
}

@Keep
class Theme {
    var ColorPalette: ColorPalette? = null
}

@Keep
class ColorPalette {
    var Primary: String? = null
    var Secondary: String? = null
}

@Keep
class Subtitle {
    var Heading: String? = null
    var Position: String? = null
}

@Keep
class Title {
    var Heading: String? = null
    var Position: String? = null
}