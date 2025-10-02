package com.sawwere.makeitso.data.model

import androidx.compose.ui.graphics.Color
import com.sawwere.makeitso.ui.theme.LightGreen
import com.sawwere.makeitso.ui.theme.LightOrange
import com.sawwere.makeitso.ui.theme.LightRed

enum class Priority(val title: String, val selectedColor: Color, val value: Int) {
    NONE("None", Color.White, 0),
    LOW("Low", LightGreen, 1),
    MEDIUM("Medium", LightOrange, 2),
    HIGH("High", LightRed, 3)
}