package com.telekom.citykey.domain.mock

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.telekom.citykey.models.OscaResponse

/**
 * A helper utility to mock the responses as taken from JSON file stored in Assets
 *
 * @param context Context provided by Koin
 * @param gson Gson object used to parse the Asset JSON
 */
class AssetResponseMocker(
    val context: Context,
    val gson: Gson
) {

    /**
     * @param fileName Name of the JSON file to take the data from
     * @return the data of type [T], parsed from Assets JSON file with name "[fileName].json", wrapped in [OscaResponse]
     */
    inline fun <reified T> getOscaResponseOf(
        fileName: String
    ): OscaResponse<T> {

        val fileNameWithExtension = "$fileName.json"

        val oscaResponseType = TypeToken.getParameterized(OscaResponse::class.java, T::class.java)

        val response: OscaResponse<T> = gson.fromJson<OscaResponse<T>>(
            context.assets.getJsonContent(fileNameWithExtension),
            oscaResponseType.type
        )

        return response
    }

    /**
     * @param fileName Name of the JSON file to take the data from
     * @return the data of type [List] of [T], parsed from Assets JSON file with name "[fileName].json", wrapped in [OscaResponse]
     */
    inline fun <reified T> getOscaResponseListOf(
        fileName: String,
    ): OscaResponse<List<T>> {

        val fileNameWithExtension = "$fileName.json"

        val listType = TypeToken.getParameterized(List::class.java, T::class.java)

        val oscaResponseType = TypeToken.getParameterized(OscaResponse::class.java, listType.type)

        val response: OscaResponse<List<T>> = gson.fromJson<OscaResponse<List<T>>>(
            context.assets.getJsonContent(fileNameWithExtension),
            oscaResponseType.type
        )

        return response
    }

    /**
     * A quick helper method to get the JSON content from the Assets file with given [fileName]
     *
     * @param fileName Name of the JSON file to take the data from
     */
    fun AssetManager.getJsonContent(fileName: String): String = open(fileName).bufferedReader().use { it.readText() }
}
