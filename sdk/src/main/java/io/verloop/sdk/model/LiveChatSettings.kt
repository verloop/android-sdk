package io.verloop.sdk.model

import androidx.annotation.Keep

@Keep
class LivechatSettings {
    var Header: Header? = null
    var Theme: Theme? = null
}

class BrandLogo {
    var URL: String? = null
}

class Header {
    var Title: Title? = null
    var Subtitle: Subtitle? = null
    var BrandLogo: BrandLogo? = null
}

class Theme {
    var ColorPalette: ColorPalette? = null
}

class ColorPalette {
    var Primary: String? = null
    var Secondary: String? = null
}

class Subtitle {
    var Heading: String? = null
    var Position: String? = null
}

class Title {
    var Heading: String? = null
    var Position: String? = null
}