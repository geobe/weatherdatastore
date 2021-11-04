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
import groovy.json.JsonSlurper

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class DwdMosmixStation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long    id
    String  stationId
    String  stationName
    Float   latitude
    Float   longitude
    Integer elevation
    String  type
//    @Transient
    String[] chickenRun

    @Override
    String toString() {
        "$stationName -> $stationId @ ${round latitude}, ${round longitude} ^ $elevation".toString()
    }

    def round(Float v) {
        String.format('%4.2f', v)
    }
}

class DwdStationReader {
    static stationFile = 'dwd-mosmix-stations-degrees.json'
    static germanStationsFile = 'german-mosmix-stations-degrees.json'

    List<DwdMosmixStation> stations = new ArrayList<>()

    def readStationFile(def filename = stationFile) {
        URL url = DwdStationReader.classLoader.getResource(filename)
        def stream = url.newInputStream()
        new JsonSlurper().parse(stream)
    }

    def buildStations(def filename = stationFile) {
        def rawData = readStationFile(filename)
        rawData.each { Map rawSt ->
            def station = new DwdMosmixStation(
                    stationId: rawSt.id,
                    stationName: rawSt.name,
                    latitude: rawSt.nb,
                    longitude: rawSt.el,
                    elevation: rawSt.elev,
                    type: rawSt.type
            )
            stations << station
        }
    }

    static void main(String[] args) {
        def r = new DwdStationReader()
        r.buildStations(germanStationsFile)

        DbHibernate db = new DbHibernate(['de.geobe.weatherdata.DwdMosmixStation'])

        DaoHibernate<DwdMosmixStation> stationDao = new DaoHibernate<>(DwdMosmixStation.class, db)

        r.stations.each {
            if (!stationDao.find('from DwdMosmixStation s where s.stationId = :sid', [sid: it.stationId]))
                stationDao.save it
        }
        stationDao.closeSession()
    }
}