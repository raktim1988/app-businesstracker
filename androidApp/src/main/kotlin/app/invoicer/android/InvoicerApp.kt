package app.invoicer.android

import android.app.Application
import android.content.pm.ApplicationInfo
import app.invoicer.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InvoicerApp : Application() {
    val container = AppContainer()

    override fun onCreate() {
        super.onCreate()
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            CoroutineScope(Dispatchers.Default).launch { DemoData.seed(container) }
        }
    }
}
