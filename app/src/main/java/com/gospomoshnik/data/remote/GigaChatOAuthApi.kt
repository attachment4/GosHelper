package com.gospomoshnik.data.remote

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface GigaChatOAuthApi {

    @FormUrlEncoded
    @POST("api/v2/oauth")
    suspend fun getToken(
        @Header("Authorization") basicAuth: String,
        @Header("RqUID") rqUid: String,
        @Field("scope") scope: String = "GIGACHAT_API_PERS"
    ): OAuthResponse
}

data class OAuthResponse(
    val access_token: String,
    val expires_at: Long
)
