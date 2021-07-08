package de.geobe.weatherdata

import javax.persistence.Entity

//@Entity
class OwmWeather extends TrackedData {
    Float temperature
    Float feelsLike
    Float pressure
    Float humidity
    Float dewPoint
    Float uvIndex
    Float clouds
    Float visibility
    Float windSpeed
    Float windGust
    Float windDegrees
    Float hourlyRain
    Float hourlySnow
    Float precipitationProbability

    @Override
    String toString() {
        return """${super.toString()}
\ttemp $temperature° feels $feelsLike° p $pressure hPa \
rain ${Math.round(precipitationProbability * 100)}% $hourlyRain mm
\thumidity $humidity% dew pt $dewPoint° clouds $clouds% uvi $uvIndex
\twind $windSpeed m/s gust $windGust direction $windDegrees"""
    }
}
