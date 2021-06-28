package de.geobe.weatherdata

class OwmIrradianceBuilder {

    def buildEntityList(def apiResponse) {
        def now = System.currentTimeSeconds()
        List list = apiResponse.list.collect { Map entry ->
            def rad = entry.radiation
            if(rad.dni_cs > 1.0) {
                new Irradiance(
                        retrievedAt: now,
                        forecastTime: entry.dt,
                        globalHorizontal: rad.ghi,
                        directNormal: rad.dni,
                        diffuseHorizontal: rad.dhi,
                        clearSkyGlobalHorizontal: rad.ghi_cs,
                        clearSkyDirectNormal: rad.dni_cs,
                        clearSkyDiffuseHorizontal: rad.dhi_cs
                )
            }
        }.findAll {it}
    }

    static void main(String[] args) {
        def response = new OwmRestClient().readRadiationForecast(3)
        def entities = new OwmIrradianceBuilder().buildEntityList(response)
        entities.each {println it}
    }
}
