package com.tanasi.mangajap.models

import com.tanasi.jsonapi.JsonApiResource
import com.tanasi.jsonapi.JsonApiType
import com.tanasi.mangajap.R
import com.tanasi.mangajap.adapters.MangaJapAdapter
import com.tanasi.mangajap.utils.extensions.toCalendar
import java.util.*

@JsonApiType("request")
class Request(
        override var id: String = "",
        createdAt: String? = null,
        updatedAt: String? = null,
        requestType: String = "",
        var data: String = "",
        var isDone: Boolean = false,
        var userHasRead: Boolean = false,

        var user: User? = null,
) : JsonApiResource(), MangaJapAdapter.Item {

    val createdAt: Calendar? = createdAt?.toCalendar("yyyy-MM-dd HH:mm:ss")
    val updatedAt: Calendar? = updatedAt?.toCalendar("yyyy-MM-dd HH:mm:ss")
    var requestType: RequestType? = RequestType.getByName(requestType)

    enum class RequestType(val stringId: Int) {
        manga(R.string.manga),
        anime(R.string.anime);

        companion object {
            fun getByName(name: String): RequestType? = try {
                valueOf(name)
            } catch (e: Exception) {
                null
            }
        }
    }


    fun putRequestType(requestType: RequestType) = putAttribute("requestType", requestType.name)

    fun putData(data: String) = putAttribute("data", data)

    fun putUserHasRead(userHasRead: Boolean) = putAttribute("userHasRead", userHasRead)

    fun putUser(user: User) = putRelationship("user", user)

    override lateinit var typeLayout: MangaJapAdapter.Type
}