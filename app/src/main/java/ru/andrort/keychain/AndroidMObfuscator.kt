package ru.andrort.keychain

import android.annotation.TargetApi
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec

class AndroidMObfuscator private constructor() : Obfuscator {

	private object Holder {
		val INSTANCE = AndroidMObfuscator()
	}

	companion object {
		val INSTANCE: AndroidMObfuscator by lazy { Holder.INSTANCE }

		const val STORE_KEY_ALIAS = "AndroidSyncplicityKey"

		const val PROVIDER = "AndroidKeyStore"
		const val SYMMETRIC_CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding"
		const val ALGORITHM_AES = "AES"
		const val SYMMETRIC_KEY_SIZE = 256
		private val IV = byteArrayOf(16, 74, 71, -80, 32, 101, -47, 72, 117, -14, 0, -29, 70, 65, -12, 74)
	}

	var keyStore: KeyStore

	init {
		keyStore = KeyStore.getInstance(PROVIDER)
		keyStore.load(null)

		generateKey()
	}

	@TargetApi(Build.VERSION_CODES.M)
	fun generateKey() {
		if (!keyStore.containsAlias(STORE_KEY_ALIAS)) {
			val keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES, PROVIDER)
			keyGenerator.init(KeyGenParameterSpec.Builder(STORE_KEY_ALIAS,
					KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
					.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
					.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
					.setRandomizedEncryptionRequired(false)
					.setKeySize(SYMMETRIC_KEY_SIZE)
					.build())
			keyGenerator.generateKey()
		}
	}

	override fun obfuscate(original: String): String {
		val bytes = original.toByteArray(StandardCharsets.UTF_8)

		val encryptor = Cipher.getInstance(SYMMETRIC_CIPHER_ALGORITHM)
		encryptor.init(Cipher.ENCRYPT_MODE, getKey(), IvParameterSpec(IV))
		val obfuscated = encryptor.doFinal(bytes)

		return Base64.encodeToString(obfuscated, Base64.NO_WRAP)
	}

	override fun unobfuscate(obfuscated: String): String {
		val bytes = Base64.decode(obfuscated, Base64.NO_WRAP)

		val encryptor = Cipher.getInstance(SYMMETRIC_CIPHER_ALGORITHM)
		encryptor.init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(IV))

		val original = encryptor.doFinal(bytes)
		return String(original, StandardCharsets.UTF_8)
	}

	private fun getKey(): Key {
		return keyStore.getKey(STORE_KEY_ALIAS, null)
	}
}
