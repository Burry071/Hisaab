package com.hisaab

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * HisaabApplication — Hilt entry point.
 *
 * Register in AndroidManifest.xml:
 *   android:name=".HisaabApplication"
 */
@HiltAndroidApp
class HisaabApplication : Application()
