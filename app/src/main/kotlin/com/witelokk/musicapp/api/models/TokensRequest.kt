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
 * @param grantType 
 * @param email 
 * @param code 
 * @param googleToken 
 * @param refreshToken 
 */
@Serializable

data class TokensRequest (

    @SerialName(value = "grant_type")
    val grantType: kotlin.String,

    @SerialName(value = "email")
    val email: kotlin.String? = null,

    @SerialName(value = "code")
    val code: kotlin.String? = null,

    @SerialName(value = "google_token")
    val googleToken: kotlin.String? = null,

    @SerialName(value = "refresh_token")
    val refreshToken: kotlin.String? = null

) {


}

