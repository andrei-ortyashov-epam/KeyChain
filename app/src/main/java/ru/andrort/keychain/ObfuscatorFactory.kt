package ru.andrort.keychain

import android.content.Context
import android.os.Build

class ObfuscatorFactory {

	companion object {

		fun getObfuscator(context: Context): Obfuscator {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				return AndroidMObfuscator.INSTANCE
			else
				return AndroidBeforeMObfuscator.getInstance(context)
		}
	}
}
