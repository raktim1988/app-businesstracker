package app.invoicer.android

import android.app.Application
import app.invoicer.data.AppContainer

class InvoicerApp : Application() {
    val container = AppContainer()
}
