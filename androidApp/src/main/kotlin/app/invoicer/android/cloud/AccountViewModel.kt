package app.invoicer.android.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.invoicer.android.R
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountState(
    val configured: Boolean = false,
    val loading: Boolean = true,
    val email: String? = null,
    val username: String? = null,
    val busy: Boolean = false,
    val errorRes: Int? = null,
)

class AccountViewModel : ViewModel() {
    private val client = Cloud.client
    private val _state = MutableStateFlow(AccountState(configured = client != null, loading = client != null))
    val state: StateFlow<AccountState> = _state.asStateFlow()

    init {
        client?.let { c ->
            viewModelScope.launch {
                c.auth.sessionStatus.collect { status ->
                    _state.update {
                        when (status) {
                            is SessionStatus.Authenticated -> it.copy(loading = false, email = status.session.user?.email, username = status.session.user?.displayName())
                            is SessionStatus.NotAuthenticated -> it.copy(loading = false, email = null, username = null)
                            // Offline or a failed token refresh: keep showing the cached user instead of a blank section.
                            is SessionStatus.RefreshFailure -> c.auth.currentUserOrNull().let { user ->
                                it.copy(loading = false, email = user?.email, username = user?.displayName())
                            }
                            else -> it
                        }
                    }
                }
            }
        }
    }

    fun setUsername(name: String) {
        val clean = name.trim()
        if (clean.length !in 2..30) return fail(R.string.account_error_username)
        viewModelScope.launch {
            val ok = runCatching {
                client!!.auth.updateUser { data = buildJsonObject { put(USERNAME_KEY, clean) } }
            }.isSuccess
            _state.update { if (ok) it.copy(username = clean, errorRes = null) else it.copy(errorRes = R.string.account_error_generic) }
        }
    }

    fun signIn(email: String, password: String) = run(email, password) {
        client!!.auth.signInWith(Email) { this.email = email.trim(); this.password = password }
    }

    fun signUp(email: String, password: String, username: String) = run(email, password) {
        client!!.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            username.trim().takeIf { it.isNotEmpty() }?.let { name -> data = buildJsonObject { put(USERNAME_KEY, name) } }
        }
    }

    fun signOut() {
        viewModelScope.launch { runCatching { client?.auth?.signOut() } }
    }

    private fun run(email: String, password: String, action: suspend () -> Unit) {
        if (client == null) return
        if (!email.trim().contains('@') || email.trim().length < 5) return fail(R.string.account_error_email)
        if (password.length < MIN_PASSWORD) return fail(R.string.account_error_password)
        _state.update { it.copy(busy = true, errorRes = null) }
        viewModelScope.launch {
            val result = runCatching { action() }
            _state.update { it.copy(busy = false, errorRes = result.exceptionOrNull()?.let(::errorFor)) }
        }
    }

    private fun fail(res: Int) = _state.update { it.copy(errorRes = res) }

    private fun errorFor(e: Throwable): Int {
        val message = e.message.orEmpty().lowercase()
        return when {
            "invalid login" in message || "invalid_credentials" in message -> R.string.account_error_credentials
            "already" in message && "registered" in message -> R.string.account_error_exists
            "network" in message || "unable to resolve" in message || "timeout" in message || "connect" in message ->
                R.string.account_error_network
            else -> R.string.account_error_generic
        }
    }

    private fun UserInfo.displayName(): String =
        (userMetadata?.get(USERNAME_KEY) as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: email.orEmpty().substringBefore('@')

    private companion object {
        const val USERNAME_KEY = "username"
        const val MIN_PASSWORD = 6
    }
}
