package town.robin.toadua.api

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ToaduaService {
    @POST("/api") suspend fun login(@Body body: LoginRequest): LoginResponse
    @POST("/api") suspend fun register(@Body body: RegisterRequest): RegisterResponse
    @POST("/api") suspend fun logout(@Body body: LogoutRequest): LogoutResponse
    @POST("/api") suspend fun welcome(@Body body: WelcomeRequest): WelcomeResponse
    @POST("/api") suspend fun search(@Body body: SearchRequest): SearchResponse
    @POST("/api") suspend fun create(@Body body: CreateRequest): CreateResponse
    @POST("/api") suspend fun vote(@Body body: VoteRequest): VoteResponse
    @POST("/api") suspend fun note(@Body body: NoteRequest): NoteResponse

    companion object {
        fun create(baseUrl: String): ToaduaService {
            val gson = GsonBuilder()
                //.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ToaduaService::class.java)
        }
    }
}