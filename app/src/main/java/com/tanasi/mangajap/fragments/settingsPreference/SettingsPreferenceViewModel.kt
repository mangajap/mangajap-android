package com.tanasi.mangajap.fragments.settingsPreference

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanasi.jsonapi.JsonApiParams
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.jsonapi.extensions.jsonApiName
import com.tanasi.jsonapi.extensions.jsonApiType
import com.tanasi.mangajap.models.User
import com.tanasi.mangajap.services.MangaJapApiService
import kotlinx.coroutines.launch

class SettingsPreferenceViewModel : ViewModel() {

    private val mangaJapApiService: MangaJapApiService = MangaJapApiService.build()

    private val _state: MutableLiveData<State> = MutableLiveData(State.Loading)
    val state: LiveData<State> = _state

    sealed class State {
        object Loading: State()
        data class SuccessLoading(val user: User): State()
        data class FailedLoading(val error: JsonApiResponse.Error): State()

        object Updating: State()
        data class SuccessUpdating(val user: User): State()
        data class FailedUpdating(val error: JsonApiResponse.Error): State()
    }

    fun getUser(userId: String) = viewModelScope.launch {
        _state.value = State.Loading

        val response = mangaJapApiService.getUser(
            userId,
            JsonApiParams(
                fields = mapOf("users" to listOf("pseudo")),
            )
        )
        _state.value = try {
            when (response) {
                is JsonApiResponse.Success -> State.SuccessLoading(response.body.data!!)
                is JsonApiResponse.Error -> State.FailedLoading(response)
            }
        } catch (e: Exception) {
            State.FailedLoading(JsonApiResponse.Error.UnknownError(e))
        }
    }

    fun updateUser(user: User) = viewModelScope.launch {
        _state.value = State.Updating

        _state.value = try {
            val response = mangaJapApiService.updateUser(
                user.id!!,
                user
            )

            when (response) {
                is JsonApiResponse.Success -> State.SuccessUpdating(response.body.data!!)
                is JsonApiResponse.Error -> State.FailedUpdating(response)
            }
        } catch (e: Exception) {
            State.FailedUpdating(JsonApiResponse.Error.UnknownError(e))
        }
    }
}