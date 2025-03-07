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

package com.witelokk.musicapp.api.apis

import com.witelokk.musicapp.api.models.ComwitelokkmusicmodelsFailureResponse
import com.witelokk.musicapp.api.models.ComwitelokkmusicmodelsShortArtists
import com.witelokk.musicapp.api.models.ComwitelokkmusicmodelsStartFollowingRequest
import com.witelokk.musicapp.api.models.ComwitelokkmusicmodelsStopFollowingRequest

import com.witelokk.musicapp.api.infrastructure.*
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.forms.formData
import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.ParametersBuilder

    open class FollowingsApi(
    baseUrl: String = ApiClient.BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    ) : ApiClient(
        baseUrl,
        httpClientEngine,
        httpClientConfig,
    ) {

        /**
        * 
        * Stop following an artist
         * @param comwitelokkmusicmodelsStopFollowingRequest  (optional)
         * @return void
        */
        open suspend fun followingsDelete(comwitelokkmusicmodelsStopFollowingRequest: ComwitelokkmusicmodelsStopFollowingRequest?): HttpResponse<Unit> {

            val localVariableAuthNames = listOf<String>("Authorization")

            val localVariableBody = comwitelokkmusicmodelsStopFollowingRequest

            val localVariableQuery = mutableMapOf<String, List<String>>()

            val localVariableHeaders = mutableMapOf<String, String>()

            val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.DELETE,
            "/followings",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
            )

            return jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
            ).wrap()
            }

        /**
        * 
        * Get a list of followed artists
         * @return ComwitelokkmusicmodelsShortArtists
        */
            @Suppress("UNCHECKED_CAST")
        open suspend fun followingsGet(): HttpResponse<ComwitelokkmusicmodelsShortArtists> {

            val localVariableAuthNames = listOf<String>("Authorization")

            val localVariableBody = 
                    io.ktor.client.utils.EmptyContent

            val localVariableQuery = mutableMapOf<String, List<String>>()

            val localVariableHeaders = mutableMapOf<String, String>()

            val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/followings",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
            )

            return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
            ).wrap()
            }

        /**
        * 
        * Start following an artist
         * @param comwitelokkmusicmodelsStartFollowingRequest  (optional)
         * @return void
        */
        open suspend fun followingsPost(comwitelokkmusicmodelsStartFollowingRequest: ComwitelokkmusicmodelsStartFollowingRequest?): HttpResponse<Unit> {

            val localVariableAuthNames = listOf<String>("Authorization")

            val localVariableBody = comwitelokkmusicmodelsStartFollowingRequest

            val localVariableQuery = mutableMapOf<String, List<String>>()

            val localVariableHeaders = mutableMapOf<String, String>()

            val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.POST,
            "/followings",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
            )

            return jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
            ).wrap()
            }

        }
