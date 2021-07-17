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

class OwmWeatherBuilder {

    def buildEntityList(def apiResponse) {
        def current = apiResponse.current
        def now = current.dt
        def hourly = apiResponse.hourly
        List list = hourly.collect { Map weather ->
            new OwmWeather(
                    issuedAt: now,
                    forecastTime: weather.dt,
                    temperature: weather.temp,
                    feelsLike: weather.feels_like,
                    pressure: weather.pressure,
                    humidity: weather.humidity,
                    dewPoint: weather.dew_point,
                    uvIndex: weather.uvi,
                    clouds: weather.clouds,
                    visibility: weather.visibility,
                    windSpeed: weather.wind_speed,
                    windGust: weather.wind_gust,
                    windDegrees: weather.wind_deg,
                    precipitationProbability: weather.pop,
                    hourlyRain: weather.rain ? weather.rain['1h'] : 0.0,
                    hourlySnow: weather.snow ? weather.snow['1h'] : 0.0
            )
        }
    }

    static void main(String[] args) {
        List<OwmWeather> weatherForcasts
        def response = new OwmRestClient().readRadiationForecast(OwmRestClient.ONE_CALL)
        weatherForcasts = new OwmWeatherBuilder().buildEntityList(response)

//        DbHibernate db = new DbHibernate(['de.geobe.weatherdata.Weather', 'de.geobe.weatherdata.Irradiance'])
//        def weatherDao = new DaoHibernate<Weather>(Weather.class, db)
//
//        weatherForcasts.each{weatherDao.save(it)}
//        weatherDao.closeSession()

        for (def i = 10; i < Math.min(24, weatherForcasts.size()); i++) {
            println weatherForcasts[i]
        }
    }
}
