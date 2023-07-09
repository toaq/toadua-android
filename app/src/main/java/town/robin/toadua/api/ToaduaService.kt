package town.robin.toadua.api

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ToaduaService {
    @POST(API_ENDPOINT)
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST(API_ENDPOINT)
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST(API_ENDPOINT)
    suspend fun logout(@Body body: LogoutRequest): LogoutResponse

    @POST(API_ENDPOINT)
    suspend fun welcome(@Body body: WelcomeRequest): WelcomeResponse

    @POST(API_ENDPOINT)
    suspend fun search(@Body body: SearchRequest): SearchResponse

    @POST(API_ENDPOINT)
    suspend fun create(@Body body: CreateRequest): CreateResponse

    @POST(API_ENDPOINT)
    suspend fun remove(@Body body: RemoveRequest): RemoveResponse

    @POST(API_ENDPOINT)
    suspend fun vote(@Body body: VoteRequest): VoteResponse

    @POST(API_ENDPOINT)
    suspend fun note(@Body body: NoteRequest): NoteResponse

    companion object {
        private const val API_ENDPOINT = "/api"

        fun create(baseUrl: String): ToaduaService {
            val gson = GsonBuilder()
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