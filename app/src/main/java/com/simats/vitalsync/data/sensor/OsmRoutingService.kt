package com.simats.vitalsync.data.sensor

import android.location.Location
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.util.Locale
import java.util.concurrent.TimeUnit

data class NominatimPlace(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val shortTitle: String = displayName.split(",").firstOrNull()?.trim() ?: displayName
)

data class OsmRouteResult(
    val points: List<GeoPoint>,
    val distanceMeters: Float,
    val durationSeconds: Float,
    val isFallback: Boolean = false
)

object OsmRoutingService {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    private const val USER_AGENT = "VitalSync/1.0 (Android; com.simats.vitalsync)"

    /**
     * Search places using OpenStreetMap Nominatim API with zero paid API keys.
     */
    suspend fun searchPlaces(query: String): List<NominatimPlace> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.length < 2) return@withContext emptyList()

        val url = "https://nominatim.openstreetmap.org/search?format=json&limit=5&q=" + Uri.encode(cleanQuery)
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val jsonArray = JSONArray(body)
                val places = mutableListOf<NominatimPlace>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val name = obj.optString("display_name", "")
                    val latStr = obj.optString("lat", "0")
                    val lonStr = obj.optString("lon", "0")
                    val lat = latStr.toDoubleOrNull() ?: 0.0
                    val lon = lonStr.toDoubleOrNull() ?: 0.0
                    if (lat != 0.0 && lon != 0.0 && name.isNotBlank()) {
                        places.add(NominatimPlace(displayName = name, latitude = lat, longitude = lon))
                    }
                }
                places
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches real cycling route geometry and distance/duration via public OSRM cycling router.
     * Falls back to a direct straight-line calculation if offline or unreachable.
     */
    suspend fun fetchCyclingRoute(
        startLat: Double,
        startLng: Double,
        destLat: Double,
        destLng: Double
    ): OsmRouteResult = withContext(Dispatchers.IO) {
        val url = String.format(
            Locale.US,
            "https://router.project-osrm.org/route/v1/cycling/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
            startLng, startLat, destLng, destLat
        )

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val root = JSONObject(body)
                        if (root.optString("code") == "Ok") {
                            val routes = root.optJSONArray("routes")
                            if (routes != null && routes.length() > 0) {
                                val routeObj = routes.getJSONObject(0)
                                val dist = routeObj.optDouble("distance", 0.0).toFloat()
                                val dur = routeObj.optDouble("duration", 0.0).toFloat()
                                val geom = routeObj.optJSONObject("geometry")
                                val coords = geom?.optJSONArray("coordinates")
                                val points = mutableListOf<GeoPoint>()
                                if (coords != null && coords.length() > 0) {
                                    for (i in 0 until coords.length()) {
                                        val pt = coords.getJSONArray(i)
                                        val cLng = pt.getDouble(0)
                                        val cLat = pt.getDouble(1)
                                        points.add(GeoPoint(cLat, cLng))
                                    }
                                }
                                if (points.isNotEmpty()) {
                                    return@withContext OsmRouteResult(
                                        points = points,
                                        distanceMeters = dist,
                                        durationSeconds = dur,
                                        isFallback = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback handled below
        }

        // Offline / unreachable fallback: straight line
        val distFallback = calculateHaversineDistance(startLat, startLng, destLat, destLng)
        val durFallback = distFallback / (16.0f * 1000f / 3600f) // ~16 km/h cycling speed
        OsmRouteResult(
            points = listOf(GeoPoint(startLat, startLng), GeoPoint(destLat, destLng)),
            distanceMeters = distFallback,
            durationSeconds = durFallback,
            isFallback = true
        )
    }

    /**
     * Calculates destination coordinates given a starting point, distance in meters, and bearing.
     */
    fun calculateDestinationForDistance(
        startLat: Double,
        startLng: Double,
        distanceMeters: Double,
        bearingDegrees: Double = 45.0
    ): Pair<Double, Double> {
        val earthRadius = 6371000.0 // meters
        val dByR = distanceMeters / earthRadius
        val latRad = Math.toRadians(startLat)
        val lngRad = Math.toRadians(startLng)
        val bearingRad = Math.toRadians(bearingDegrees)

        val destLatRad = Math.asin(
            Math.sin(latRad) * Math.cos(dByR) +
            Math.cos(latRad) * Math.sin(dByR) * Math.cos(bearingRad)
        )
        val destLngRad = lngRad + Math.atan2(
            Math.sin(bearingRad) * Math.sin(dByR) * Math.cos(latRad),
            Math.cos(dByR) - Math.sin(latRad) * Math.sin(destLatRad)
        )
        return Pair(Math.toDegrees(destLatRad), Math.toDegrees(destLngRad))
    }

    fun calculateHaversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}
