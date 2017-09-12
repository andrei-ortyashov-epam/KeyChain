package ru.andrort.keychain

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	lateinit var obfuscator: Obfuscator

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		obfuscator = ObfuscatorFactory.getObfuscator(this)
		generate.setOnClickListener {

			val initialText = initialText.text.toString()
			if (!initialText.isNullOrEmpty()) {
				hideKeyboard()

				val obsuscated = obfuscator.obfuscate(initialText)
				encryptedText.setText(obsuscated)

				val deobsuctated = obfuscator.unobfuscate(obsuscated)
				decryptedText.setText(deobsuctated)
			}
		}
	}

	fun hideKeyboard() {
		val view = this.currentFocus
		if (view != null) {
			val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm.hideSoftInputFromWindow(view.windowToken, 0)
		}
	}
}
