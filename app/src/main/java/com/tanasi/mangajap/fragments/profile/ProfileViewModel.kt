package com.tanasi.mangajap.fragments.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tanasi.jsonapi.JsonApiParams
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.jsonapi.bodies.JsonApiBody
import com.tanasi.jsonapi.extensions.jsonApiName
import com.tanasi.jsonapi.extensions.jsonApiType
import com.tanasi.mangajap.models.*
import com.tanasi.mangajap.services.MangaJapApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val mangaJapApiService: MangaJapApiService = MangaJapApiService.build()

    private val _state: MutableLiveData<State> = MutableLiveData(State.Loading)
    val state: LiveData<State> = _state

    sealed class State {
        object Loading: State()
        data class SuccessLoading(val user: User, val followed: Follow?, val follower: Follow?): State()
        data class FailedLoading(val error: JsonApiResponse.Error): State()

        object UpdatingFollowed: State()
        data class SuccessUpdatingFollowed(val followed: Follow?): State()
        data class FailedUpdatingFollowed(val error: JsonApiResponse.Error): State()
    }

    fun getProfile(userId: String) = viewModelScope.launch {
        _state.value = State.Loading

        _state.value = try {
            val selfId = Firebase.auth.uid!!

            val userResponseDeferred = async { mangaJapApiService.getUser(
                userId,
                JsonApiParams(
                    include = listOf(
                        "manga-library.manga",
                        "anime-library.anime",
                        "manga-favorites.manga",
                        "anime-favorites.anime"
                    ),
                    fields = mapOf(
                        "manga" to listOf("title", "coverImage", "volumeCount", "chapterCount"),
                        "anime" to listOf("title", "coverImage", "episodeCount")
                    ),
                )
            ) }
            val followedResponseDeferred = async {
                if (userId != selfId) {
                    mangaJapApiService.getFollows(
                        JsonApiParams(
                            filter = mapOf(
                                "follower" to listOf(selfId),
                                "followed" to listOf(userId)
                            )
                        )
                    )
                } else {
                    JsonApiResponse.Success(200, JsonApiBody("", listOf()))
                }
            }
            val followerResponseDeferred = async {
                if (userId != selfId) {
                    mangaJapApiService.getFollows(
                        JsonApiParams(
                            filter = mapOf(
                                "follower" to listOf(userId),
                                "followed" to listOf(selfId)
                            )
                        )
                    )
                } else {
                    JsonApiResponse.Success(200, JsonApiBody("", listOf()))
                }
            }

            val userResponse = userResponseDeferred.await()
            val followedResponse = followedResponseDeferred.await()
            val followerResponse = followerResponseDeferred.await()

            when {
                userResponse is JsonApiResponse.Success &&
                        followedResponse is JsonApiResponse.Success &&
                        followerResponse is JsonApiResponse.Success -> State.SuccessLoading(
                    userResponse.body.data!!,
                    followedResponse.body.data?.firstOrNull(),
                    followerResponse.body.data?.firstOrNull()
                )

                userResponse is JsonApiResponse.Success -> State.FailedLoading(JsonApiResponse.Error.UnknownError(Exception("Unable to load user data")))
                userResponse is JsonApiResponse.Error -> State.FailedLoading(userResponse)

                followedResponse is JsonApiResponse.Success -> State.FailedLoading(JsonApiResponse.Error.UnknownError(Exception("Unable to load followed data")))
                followedResponse is JsonApiResponse.Error -> State.FailedLoading(followedResponse)

                followerResponse is JsonApiResponse.Success -> State.FailedLoading(JsonApiResponse.Error.UnknownError(Exception("Unable to load follower data")))
                followerResponse is JsonApiResponse.Error -> State.FailedLoading(followerResponse)

                else -> State.FailedLoading(JsonApiResponse.Error.UnknownError(Exception("Unable to load data")))
            }
        } catch (e: Exception) {
            State.FailedLoading(JsonApiResponse.Error.UnknownError(e))
        }
    }

    fun follow(follow: Follow) = viewModelScope.launch {
        _state.value = State.UpdatingFollowed

        val response = mangaJapApiService.createFollow(
                follow
        )
        _state.value = try {
            when (response) {
                is JsonApiResponse.Success -> State.SuccessUpdatingFollowed(response.body.data!!)
                is JsonApiResponse.Error -> State.FailedUpdatingFollowed(response)
            }
        } catch (e: Exception) {
            State.FailedUpdatingFollowed(JsonApiResponse.Error.UnknownError(e))
        }
    }

    fun deleteFollow(follow: Follow) = viewModelScope.launch {
        _state.value = State.UpdatingFollowed

        _state.value = try {
            val response = mangaJapApiService.deleteFollow(
                follow.id!!
            )

            when (response) {
                is JsonApiResponse.Success -> State.SuccessUpdatingFollowed(null)
                is JsonApiResponse.Error -> State.FailedUpdatingFollowed(response)
            }
        } catch (e: Exception) {
            State.FailedUpdatingFollowed(JsonApiResponse.Error.UnknownError(e))
        }
    }
}