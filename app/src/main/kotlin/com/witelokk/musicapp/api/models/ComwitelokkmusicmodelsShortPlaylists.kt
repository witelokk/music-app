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

import com.witelokk.musicapp.api.models.ComwitelokkmusicmodelsShortPlaylist

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Contextual

/**
 * 
 *
 * @param count 
 * @param playlists 
 */
@Serializable

data class ComwitelokkmusicmodelsShortPlaylists (

    @SerialName(value = "count")
    val count: kotlin.Int,

    @SerialName(value = "playlists")
    val playlists: kotlin.collections.List<ComwitelokkmusicmodelsShortPlaylist>

) {


}

