package com.example.fypdeadreckoning.helpers.extra

data class LatLon(val lat: Double, val lon: Double) {
    override fun toString(): String {
        return "($lat, $lon)"
    }
}