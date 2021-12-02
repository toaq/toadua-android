package town.robin.toadua

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ToaduaService {
    @POST("/api")
    suspend fun search(@Body body: SearchRequest): SearchResponse

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