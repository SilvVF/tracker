package io.silv.core_ui

import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator

interface ActionTab: Tab {
    fun onReselect()
    fun onAddBottomBarClick()
}