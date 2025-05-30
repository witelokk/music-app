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
 * @param accessToken 
 * @param refreshToken 
 */
@Serializable

data class TokensResponse (

    @SerialName(value = "accessToken")
    val accessToken: kotlin.String,

    @SerialName(value = "refreshToken")
    val refreshToken: kotlin.String

) {


}

