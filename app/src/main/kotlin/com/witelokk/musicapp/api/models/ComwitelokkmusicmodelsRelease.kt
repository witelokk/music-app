/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package com.witelokk.musicapp.api.models

import com.witelokk.musicapp.api.models.ComwitelokkmusicmodelsShortArtists
import com.witelokk.musicapp.api.models.ComwitelokkmusicmodelsSongs

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Contextual

/**
 * 
 *
 * @param id 
 * @param name 
 * @param type 
 * @param releasedAt 
 * @param songs 
 * @param artists 
 * @param coverUrl 
 */
@Serializable

data class ComwitelokkmusicmodelsRelease (

    @Contextual @SerialName(value = "id")
    val id: java.util.UUID,

    @SerialName(value = "name")
    val name: kotlin.String,

    @SerialName(value = "type")
    val type: kotlin.String,

    @Contextual @SerialName(value = "released_at")
    val releasedAt: java.time.OffsetDateTime,

    @SerialName(value = "songs")
    val songs: ComwitelokkmusicmodelsSongs,

    @SerialName(value = "artists")
    val artists: ComwitelokkmusicmodelsShortArtists,

    @SerialName(value = "cover_url")
    val coverUrl: kotlin.String? = null

) {


}

