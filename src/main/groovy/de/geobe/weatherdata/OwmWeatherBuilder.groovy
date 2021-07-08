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
