package com.splitit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform