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
