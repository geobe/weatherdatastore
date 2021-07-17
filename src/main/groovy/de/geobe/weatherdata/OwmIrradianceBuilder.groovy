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

import de.geobe.architecture.persist.DaoHibernate
import de.geobe.architecture.persist.DbHibernate

class OwmIrradianceBuilder {

    def buildEntityList(def apiResponse) {
        def now = System.currentTimeSeconds()
        List list = apiResponse.list.collect { Map entry ->
            def rad = entry.radiation
            if (rad.dni_cs > 1.0 || rad.dhi_cs > 1.0) {
                new OwmIrradiance(
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
        List<OwmIrradiance> irradiances
        def response = new OwmRestClient().readRadiationForecast(OwmRestClient.IRRADIANCE_FORECAST)
        irradiances = new OwmIrradianceBuilder().buildEntityList(response)

        DbHibernate db = new DbHibernate(['de.geobe.weatherdata.Weather', 'de.geobe.weatherdata.Irradiance'])

        def irrDao = new DaoHibernate<OwmIrradiance>(OwmIrradiance.class, db)
        irradiances.each { irrDao.save(it) }
        irrDao.closeSession()
        irradiances.each { println it }
    }
}
