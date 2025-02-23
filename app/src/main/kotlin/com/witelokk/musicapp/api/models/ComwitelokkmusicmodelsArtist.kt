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


import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Contextual

/**
 * 
 *
 * @param id 
 * @param name 
 * @param followers 
 * @param following 
 * @param avatarUrl 
 * @param coverUrl 
 */
@Serializable

data class ComwitelokkmusicmodelsArtist (

    @Contextual @SerialName(value = "id")
    val id: java.util.UUID,

    @SerialName(value = "name")
    val name: kotlin.String,

    @SerialName(value = "followers")
    val followers: kotlin.Int,

    @SerialName(value = "following")
    val following: kotlin.Boolean,

    @SerialName(value = "avatar_url")
    val avatarUrl: kotlin.String? = null,

    @SerialName(value = "cover_url")
    val coverUrl: kotlin.String? = null

) {


}

