package app.invoicer.android

import android.app.Application
import android.content.pm.ApplicationInfo
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.invoicer.android.cloud.SyncManager
import app.invoicer.data.AppContainer
import app.invoicer.data.createDatabase
import app.invoicer.data.db.InvoicerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class InvoicerApp : Application() {
    val container by lazy {
        val driver = AndroidSqliteDriver(InvoicerDatabase.Schema, this, "invoicer.db")
        AppContainer(createDatabase(driver))
    }

    val sync by lazy { SyncManager(container.syncStore!!) }

    override fun onCreate() {
        super.onCreate()
        sync.start(
            combine(
                container.invoiceRepository.observeAll(),
                container.clientRepository.observeAll(),
                container.profileRepository.observe(),
            ) { a, b, c -> Triple(a, b, c) },
        )
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            CoroutineScope(Dispatchers.Default).launch { DemoData.seed(container) }
        }
    }
}
