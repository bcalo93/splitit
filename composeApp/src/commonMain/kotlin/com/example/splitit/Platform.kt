package com.example.splitit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform