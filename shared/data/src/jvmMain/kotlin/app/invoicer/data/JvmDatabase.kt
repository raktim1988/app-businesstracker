package app.invoicer.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.invoicer.data.db.InvoicerDatabase

fun createInMemoryDatabase(): InvoicerDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    InvoicerDatabase.Schema.create(driver)
    return createDatabase(driver)
}
