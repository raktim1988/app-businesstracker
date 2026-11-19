package app.invoicer.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.invoicer.data.AppContainer
import app.invoicer.domain.model.BusinessProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BusinessProfileViewModel(private val container: AppContainer) : ViewModel() {
    /** Null until the first database read, so the form does not flash empty values. */
    val profile: StateFlow<BusinessProfile?> = container.profileRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(profile: BusinessProfile) {
        viewModelScope.launch { container.profileRepository.save(profile) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory { initializer { BusinessProfileViewModel(container) } }
    }
}
