package de.geobe.weatherdata

import javax.persistence.Entity

@Entity
class Weather extends TrackedData {
    float temperature
    float pressure
    float humidity
    float dewPoint
    float uvIndex
    float clouds
    float visibility
    float precipitationProbability
}
