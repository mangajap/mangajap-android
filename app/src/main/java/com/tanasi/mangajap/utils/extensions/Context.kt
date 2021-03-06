package com.tanasi.mangajap.utils.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.tanasi.mangajap.utils.preferences.SettingsPreference
import java.util.*

fun Context.setNightMode(theme: SettingsPreference.Theme = SettingsPreference(this).theme) {
    val mode = when (theme) {
        SettingsPreference.Theme.device -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        SettingsPreference.Theme.light -> AppCompatDelegate.MODE_NIGHT_NO
        SettingsPreference.Theme.dark -> AppCompatDelegate.MODE_NIGHT_YES
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

fun Context.locale(): Locale = Locale(SettingsPreference(this).language.name)

fun Context.setLocale(lang: String? = null) {
    val locale = if (lang == null) this.locale() else Locale(lang)
    Locale.setDefault(locale)
    this.resources.updateConfiguration(Configuration().also {
        it.setLocale(locale)
    }, this.resources.displayMetrics)
}

fun Context.toActivity(): AppCompatActivity? = this as? AppCompatActivity


fun Context.isStoragePermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    } else { //permission is automatically granted on sdk<23 upon installation
        true
    }
}

fun Context.getAttrColor(resId: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(resId, typedValue, true)
    return typedValue.data
}

fun Context.getCountries(): Map<String, String> {
    val countries: MutableMap<String, String> = mutableMapOf()
    val locales = Locale.getAvailableLocales()
    for (locale in locales) {
        val countryName = locale.getDisplayCountry(this.locale())
        val countryCode = locale.country
        if (countryName.isNotEmpty() && !countries.containsValue(countryName)) {
            countries[countryCode] = countryName
        }
    }
    return countries
}

fun Context.getAppVersionName(): String {
    return try {
        this.packageManager.getPackageInfo(this.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        ""
    }
}

fun Context.getAppVersionCode(): Int {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.packageManager.getPackageInfo(this.packageName, 0).longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            this.packageManager.getPackageInfo(this.packageName, 0).versionCode
        }
    } catch (e: PackageManager.NameNotFoundException) {
        0
    }
}