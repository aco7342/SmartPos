//SmartPosApp.kt
package com.example.smartpos

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SmartPosApp : Application() {
    val appScope = CoroutineScope(SupervisorJob())
}
