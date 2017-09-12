package ru.andrort.keychain

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.security.auth.x500.X500Principal

class AndroidBeforeMObfuscator private constructor(val context: Context) : Obfuscator {

	companion object : SingletonHolder<AndroidBeforeMObfuscator, Context>(::AndroidBeforeMObfuscator) {
		const val STORE_KEY_ALIAS = "AndroidSyncplicityKeyAlias"
		const val PREF_FILE = "pref"
		const val KEY_IN_PREFERENCES = "key"

		const val PROVIDER = "AndroidKeyStore"
		const val CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
		const val ALGORITHM_AES = "AES"
		const val SYMMETRIC_KEY_SIZE = 256
	}

	val pref = context.getApplicationContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
	var keyStore: KeyStore

	init {
		keyStore = KeyStore.getInstance(PROVIDER)
		keyStore.load(null)

		generateKey()
	}

	fun generateKey() {
		if (!keyStore.containsAlias(STORE_KEY_ALIAS)) {
			val keyGen = KeyGenerator.getInstance(ALGORITHM_AES)
			keyGen.init(SYMMETRIC_KEY_SIZE)
			val secretKey = keyGen.generateKey()

			generatePrivateKey()

			val input = getCipher(Cipher.ENCRYPT_MODE)
			val encrypted = input.doFinal(secretKey.getEncoded())

			val string = Base64.encodeToString(encrypted, Base64.NO_WRAP)
			pref.edit().putString(KEY_IN_PREFERENCES, string).apply()
		}
	}

	@Suppress("DEPRECATION")
	fun generatePrivateKey() {
		val start = Calendar.getInstance()
		val end = Calendar.getInstance()
		end.add(Calendar.YEAR, 100)

		val spec = KeyPairGeneratorSpec.Builder(context)
				.setAlias(STORE_KEY_ALIAS)
				.setSubject(X500Principal("CN=$STORE_KEY_ALIAS"))
				.setSerialNumber(BigInteger.TEN)
				.setStartDate(start.getTime())
				.setEndDate(end.getTime())
				.build()

		val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, PROVIDER)
		kpg.initialize(spec)
		kpg.generateKeyPair()
	}

	fun getCipher(mode: Int): Cipher {
		val privateKeyEntry = keyStore.getEntry(STORE_KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
		val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
		//encrypt with the public key, decrypt with the private one
		val key = if (mode == Cipher.ENCRYPT_MODE) privateKeyEntry.certificate.publicKey else privateKeyEntry.privateKey
		cipher.init(mode, key)
		return cipher
	}

	override fun obfuscate(original: String): String {
		val bytes = original.toByteArray(StandardCharsets.UTF_8)

		val input = getCipher(Cipher.ENCRYPT_MODE)
		val obfuscated = input.doFinal(bytes)

		return Base64.encodeToString(obfuscated, Base64.NO_WRAP)
	}

	override fun unobfuscate(obfuscated: String): String {
		val bytes = Base64.decode(obfuscated, Base64.NO_WRAP)

		val output = getCipher(Cipher.DECRYPT_MODE)
		val original = output.doFinal(bytes)

		return String(original, StandardCharsets.UTF_8)
	}
}
