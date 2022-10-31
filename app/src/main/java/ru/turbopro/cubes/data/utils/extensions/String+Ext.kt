package ru.turbopro.cubes.data.utils.extensions

fun String.cutEmailFromNickname(): String {
    return substringBeforeLast("@")
}

fun String.addEmailToNickname(): String {
    return "$this@gmail.com"
}