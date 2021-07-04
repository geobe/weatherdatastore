package de.geobe.weatherdata

import de.geobe.architecture.persist.DaoHibernate
import de.geobe.architecture.persist.DbHibernate

class OwmIrradianceBuilder {

    def buildEntityList(def apiResponse) {
        def now = System.currentTimeSeconds()
        List list = apiResponse.list.collect { Map entry ->
            def rad = entry.radiation
            if (rad.dni_cs > 1.0 || rad.dhi_cs > 1.0) {
                new Irradiance(
                        issuedAt: now,
                        forecastTime: entry.dt,
                        globalHorizontal: rad.ghi,
                        directNormal: rad.dni,
                        diffuseHorizontal: rad.dhi,
                        clearSkyGlobalHorizontal: rad.ghi_cs,
                        clearSkyDirectNormal: rad.dni_cs,
                        clearSkyDiffuseHorizontal: rad.dhi_cs
                )
            }
        }.findAll { it }
    }

    static void main(String[] args) {
        List<Irradiance> irradiances
        def response = new OwmRestClient().readRadiationForecast(OwmRestClient.IRRADIANCE_FORECAST)
        irradiances = new OwmIrradianceBuilder().buildEntityList(response)

        DbHibernate db = new DbHibernate(['de.geobe.weatherdata.Weather', 'de.geobe.weatherdata.Irradiance'])

        def irrDao = new DaoHibernate<Irradiance>(Irradiance.class, db)
        irradiances.each { irrDao.save(it) }
        irrDao.closeSession()
        irradiances.each { println it }
    }
}
