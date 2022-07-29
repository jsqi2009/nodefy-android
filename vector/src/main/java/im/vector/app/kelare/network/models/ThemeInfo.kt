package im.vector.app.kelare.network.models

import java.io.Serializable


class ThemeInfo:Serializable {

    var id: Int? = 0
    var theme_type: String? = null
    var is_default: Boolean? = false
    var color: ColorInfo? = null

    override fun toString(): String {
        return "ThemeInfo(id=$id, theme_type=$theme_type, is_default=$is_default, color=$color)"
    }

    class ColorInfo:Serializable {

        var theme: String? = null
        var columnBg: String? = null
        var columnBgFont: String? = null
        var listBg: String? = null
        var activeItem: String? = null
        var activeItemGradient: String? = null
        var activeItemFont: String? = null
        var hoverItem: String? = null
        var hoverItemGradient: String? = null
        var title: String? = null
        var titleOpacity: String? = null
        var border: String? = null
        var columnButtonBorder: String? = null
        var columnHighlightBorder: String? = null
        var listSearchBg: String? = null
        var blackHighlight: String? = null
        override fun toString(): String {
            return "ColorInfo(theme=$theme, columnBg=$columnBg, columnBgFont=$columnBgFont, listBg=$listBg, activeItem=$activeItem, activeItemGradient=$activeItemGradient, activeItemFont=$activeItemFont, hoverItem=$hoverItem, hoverItemGradient=$hoverItemGradient, title=$title, titleOpacity=$titleOpacity, border=$border, columnButtonBorder=$columnButtonBorder, columnHighlightBorder=$columnHighlightBorder, listSearchBg=$listSearchBg, blackHighlight=$blackHighlight)"
        }
    }


}
