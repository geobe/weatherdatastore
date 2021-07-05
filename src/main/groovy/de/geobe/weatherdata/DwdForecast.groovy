package de.geobe.weatherdata

import javax.persistence.Entity

@Entity
class DwdForecast extends TrackedData {

    static xmlMapping = [
            TTT  : 'temperature',
            PPPP : 'pressure',
            FF   : 'windSpeed',
            FX1  : 'windGust',
            DD   : 'windDegrees',
            VV   : 'visibility',
            N    : 'totalCloudCover',
            SunD1: 'sunshineDuration',
            RR1c : 'hourlyPrecipitation',
            Rad1h: 'globalIrradiance'
    ]

    Float temperature
    Float pressure
    Float humidity
    Float visibility
    Float totalCloudCover
    Float sunshineDuration
    Float windSpeed
    Float windGust
    Float windDegrees
    Float hourlyPrecipitation
    Float globalIrradiance

    @Override
    String toString() {
        return """${super.toString()}
\ttemp ${temperature ? String.format('% 5.1f', temperature - 273.15) : '???'} °C, p ${pressure ? pressure / 100.0 : '???'} hPa \
rain $hourlyPrecipitation mm, clouds $totalCloudCover %, sun $sunshineDuration s,  irradiance $globalIrradiance kJ/m²\
"""
    }
}
