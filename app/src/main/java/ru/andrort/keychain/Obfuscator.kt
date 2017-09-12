package ru.andrort.keychain

interface Obfuscator {

	fun obfuscate(original: String): String

	fun unobfuscate(obfuscated: String): String

}