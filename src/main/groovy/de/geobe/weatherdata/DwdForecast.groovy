/*
 *  The MIT License (MIT)
 *
 *                            Copyright (c) 2021. Georg Beier
 *
 *                            Permission is hereby granted, free of charge, to any person obtaining a copy
 *                            of this software and associated documentation files (the "Software"), to deal
 *                            in the Software without restriction, including without limitation the rights
 *                            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *                            copies of the Software, and to permit persons to whom the Software is
 *                            furnished to do so, subject to the following conditions:
 *
 *                            The above copyright notice and this permission notice shall be included in all
 *                            copies or substantial portions of the Software.
 *
 *                            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *                            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *                            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *                            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *                            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *                            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *                            SOFTWARE.
 *
 *
 */

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
