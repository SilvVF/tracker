package io.silv.tracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform