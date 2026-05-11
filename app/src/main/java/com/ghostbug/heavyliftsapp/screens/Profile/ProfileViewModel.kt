package com.ghostbug.heavyliftsapp.screens.profile

import HeightUnit
import WeightUnit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.data.domain.Gender
import com.ghostbug.heavyliftsapp.data.domain.UserProfileEntity
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock


class ProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProfileUiEvent>()
    val uiEvent: SharedFlow<ProfileUiEvent> = _uiEvent.asSharedFlow()

    private var checkNameJob: Job? = null

    init {
        onEvent(ProfileEvent.LoadProfile)
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LoadProfile -> loadProfile()
            ProfileEvent.StartEditing -> _state.update { it.copy(isEditing = true) }
            ProfileEvent.CancelEditing -> {
                // revert unsaved changes by reloading
                _state.update { it.copy(isEditing = false) }
                loadProfile()
            }
            ProfileEvent.SaveProfile -> saveProfile()
            ProfileEvent.ShowLogoutDialog -> _state.update { it.copy(showLogoutDialog = true) }
            ProfileEvent.HideLogoutDialog -> _state.update { it.copy(showLogoutDialog = false) }
            ProfileEvent.Logout -> logout()
            ProfileEvent.ShowDeleteDialog -> _state.update { it.copy(showDeleteDialog = true) }
            ProfileEvent.HideDeleteDialog -> _state.update { it.copy(showDeleteDialog = false) }
            ProfileEvent.DeleteAccount -> deleteAccount()
            is ProfileEvent.OnUserNameChanged -> handleUserNameChanged(event.value)
            is ProfileEvent.OnAgeChanged -> handleAgeChanged(event.value)
            is ProfileEvent.OnHeightChanged -> handleHeightChanged(event.value)
            is ProfileEvent.OnUserWeightChanged -> handleWeightChanged(event.value)
            is ProfileEvent.OnGenderChanged -> _state.update { it.copy(gender = event.value) }
            is ProfileEvent.OnCityChanged -> _state.update { it.copy(city = event.value) }
            is ProfileEvent.OnCountryChanged -> _state.update { it.copy(country = event.value) }
            is ProfileEvent.OnHeightUnitChanged -> handleHeightUnitChanged(event.value)
            is ProfileEvent.OnWeightUnitChanged -> handleWeightUnitChanged(event.value)
            is ProfileEvent.OnProfilePicChanged -> _state.update { it.copy(profilePic = event.value) }
            is ProfileEvent.OnDobChanged -> { /* handle if needed */ }
            is ProfileEvent.message -> sendSnackbar(event.value)
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val profile = userProfileRepository.getOwnProfile()
                val email = authRepository.getCurrentUserEmail() ?: ""
                if (profile != null) {
                    val heightText = profile.height?.let {
                        if (_state.value.heightUnit == HeightUnit.CM)
                            "%.1f".format(it)
                        else {
                            val totalInches = (it / 2.54f).toInt()
                            "${totalInches / 12}'${totalInches % 12}"
                        }
                    } ?: ""

                    val weightText = profile.userWeight?.let {
                        if (_state.value.weightUnit == WeightUnit.KG)
                            "%.1f".format(it)
                        else "%.1f".format(it * 2.20462f)
                    } ?: ""

                    _state.update {
                        it.copy(
                            userId = profile.userId,
                            userName = profile.userName,
                            userNameError = null,
                            email = email,
                            profilePic = profile.profilePic,
                            age = profile.age,
                            ageInputText = profile.age?.toString() ?: "",
                            height = profile.height,
                            heightInputText = heightText,
                            userWeight = profile.userWeight,
                            weightInputText = weightText,
                            gender = profile.gender,
                            city = profile.city,
                            country = profile.country,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(email = email, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun handleUserNameChanged(userName: String) {
        _state.update {
            it.copy(
                userName = userName,
                userNameError = when {
                    userName.isBlank() -> null
                    userName.length < 3 -> "Must be at least 3 characters"
                    userName.length > 20 -> "Must be less than 20 characters"
                    else -> null
                }
            )
        }
        if (userName.length in 3..20) {
            checkNameJob?.cancel()
            checkNameJob = viewModelScope.launch {
                delay(350)
                val isTaken = userProfileRepository.isUserNameTaken(userName)
                _state.update {
                    it.copy(userNameError = if (isTaken) "Username already taken" else null)
                }
            }
        }
    }

    private fun handleAgeChanged(ageStr: String) {
        val age = ageStr.toIntOrNull()
        val validAge = when {
            ageStr.isBlank() -> null
            age == null || age < 12 || age > 120 -> null
            else -> age
        }
        _state.update { it.copy(ageInputText = ageStr, age = validAge) }
    }

    private fun handleHeightChanged(input: String) {
        val unit = _state.value.heightUnit
        if (unit == HeightUnit.CM) {
            val heightInCm = input.toFloatOrNull()
            val error = when {
                input.isBlank() -> null
                heightInCm == null -> "Enter a valid height"
                heightInCm < 91.44f -> "Must be more than 3 feet"
                heightInCm > 272f -> "Enter a valid height"
                else -> null
            }
            _state.update {
                it.copy(
                    heightInputText = input,
                    height = if (error == null) heightInCm else it.height
                )
            }
            return
        }

        // feet mode — auto format
        val digitsOnly = input.filter { it.isDigit() }
        val formatted = when {
            digitsOnly.isEmpty() -> ""
            digitsOnly.length == 1 -> digitsOnly
            else -> {
                val feet = digitsOnly.take(1)
                val inches = digitsOnly.drop(1).take(2)
                "$feet'$inches"
            }
        }
        val heightInCm = if (digitsOnly.length >= 2) {
            val feet = digitsOnly.take(1).toFloatOrNull() ?: 0f
            val inches = digitsOnly.drop(1).take(2).toFloatOrNull() ?: 0f
            if (inches >= 12f) null else ((feet * 12f) + inches) * 2.54f
        } else null

        _state.update {
            it.copy(
                heightInputText = formatted,
                height = heightInCm ?: it.height
            )
        }
    }

    private fun handleWeightChanged(input: String) {
        val value = input.toFloatOrNull()
        val unit = _state.value.weightUnit
        val weightInKg = if (value != null) {
            if (unit == WeightUnit.LBS) value * 0.453592f else value
        } else null
        _state.update {
            it.copy(
                weightInputText = input,
                userWeight = weightInKg ?: it.userWeight
            )
        }
    }

    private fun handleHeightUnitChanged(unit: HeightUnit) {
        val currentCm = _state.value.height
        val convertedInput = when (unit) {
            HeightUnit.FEET -> {
                if (currentCm != null && currentCm > 0f) {
                    val totalInches = (currentCm / 2.54f).toInt()
                    "${totalInches / 12}${totalInches % 12}"
                } else ""
            }
            HeightUnit.CM -> {
                if (currentCm != null && currentCm > 0f)
                    "%.1f".format(currentCm)
                else ""
            }
        }
        _state.update { it.copy(heightUnit = unit) }
        if (convertedInput.isNotBlank()) handleHeightChanged(convertedInput)
    }

    private fun handleWeightUnitChanged(unit: WeightUnit) {
        val currentKg = _state.value.userWeight
        val convertedInput = if (currentKg != null && currentKg > 0f) {
            when (unit) {
                WeightUnit.LBS -> "%.1f".format(currentKg * 2.20462f)
                WeightUnit.KG -> "%.1f".format(currentKg)
            }
        } else ""
        _state.update { it.copy(weightUnit = unit) }
        if (convertedInput.isNotBlank()) handleWeightChanged(convertedInput)
    }

    private fun saveProfile() {
        val s = _state.value
        if (s.userName.isBlank() || s.userNameError != null) {
            sendSnackbar("Fix username before saving")
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val existing = userProfileRepository.getOwnProfile()
                val now = Clock.System.now()
                val profile = UserProfileEntity(
                    userId = s.userId,
                    userName = s.userName,
                    age = s.age,
                    height = s.height,
                    userWeight = s.userWeight,
                    gender = s.gender,
                    city = s.city,
                    country = s.country,
                    profilePic = s.profilePic,
                    dob = null,
                    accountCreated = existing?.accountCreated ?: now,
                    accountLastUpdated = now,
                )
                userProfileRepository.upsertProfile(profile)
                _state.update { it.copy(isSaving = false, isEditing = false) }
                _uiEvent.emit(ProfileUiEvent.ProfileSaved)
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                sendSnackbar("Save error: ${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(showLogoutDialog = false) }
            try {
                authRepository.signOut()
                _uiEvent.emit(ProfileUiEvent.NavigateToSignIn)
            } catch (_: Exception) {
                sendSnackbar("Failed to sign out")
            }
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, showDeleteDialog = false) }
            try {
                authRepository.deleteAccount()
                _uiEvent.emit(ProfileUiEvent.AccountDeleted)
            } catch (_: Exception) {
                _state.update { it.copy(isDeleting = false) }
                sendSnackbar("Failed to delete account")
            }
        }
    }

    private fun sendSnackbar(message: String) {
        viewModelScope.launch { _uiEvent.emit(ProfileUiEvent.ShowSnackbar(message)) }
    }
}