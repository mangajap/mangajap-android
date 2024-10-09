package com.tanasi.mangajap.fragments.profileedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.mangajap.models.User
import com.tanasi.mangajap.utils.MangaJapApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ProfileEditViewModel : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: Flow<State> = _state

    sealed class State {
        data object Loading : State()
        data class SuccessLoading(val user: User) : State()
        data class FailedLoading(val error: JsonApiResponse.Error) : State()

        data object Updating : State()
        data class SuccessUpdating(val user: User) : State()
        data class FailedUpdating(val error: JsonApiResponse.Error) : State()
    }

    init {
        getUser()
    }


    private fun getUser() = viewModelScope.launch(Dispatchers.IO) {
        _state.emit(State.Loading)

        try {
            val response = MangaJapApi.Users.details(Firebase.auth.uid!!)

            when (response) {
                is JsonApiResponse.Success -> {
                    _state.emit(State.SuccessLoading(response.body.data!!))
                }

                is JsonApiResponse.Error -> {
                    _state.emit(State.FailedLoading(response))
                }
            }
        } catch (e: Exception) {
            _state.emit(State.FailedLoading(JsonApiResponse.Error.UnknownError(e)))
        }
    }

    fun updateUser(user: User) = viewModelScope.launch {
        _state.emit(State.Updating)

        try {
            val response = MangaJapApi.Users.update(
                user.id!!,
                user
            )

            when (response) {
                is JsonApiResponse.Success -> {
                    _state.emit(State.SuccessUpdating(response.body.data!!))
                }

                is JsonApiResponse.Error -> {
                    _state.emit(State.FailedUpdating(response))
                }
            }
        } catch (e: Exception) {
            _state.emit(State.FailedUpdating(JsonApiResponse.Error.UnknownError(e)))
        }
    }
}