package de.geobe.weatherdata

import javax.persistence.Entity

@Entity
class Weather extends TrackedData {
    Float temperature
    Float pressure
    Float humidity
    Float dewPoint
    Float uvIndex
    Float clouds
    Float visibility
    Float precipitationProbability
}
