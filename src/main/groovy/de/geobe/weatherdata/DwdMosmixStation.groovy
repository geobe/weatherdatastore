package de.geobe.weatherdata

import groovy.json.JsonSlurper

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class DwdMosmixStation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id
    String stationId
    String stationName
    Float latitude
    Float longitude
    Integer elevation
    String type

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

    List<DwdMosmixStation> stations = new ArrayList<>()

    def readStationFile(def filename = stationFile) {
        URL url = DwdStationReader.classLoader.getResource(filename)
        def stream = url.newInputStream()
        new JsonSlurper().parse(stream)
    }

    def buildStations() {
        def rawData = readStationFile()
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
        r.buildStations()
        r.stations.each {
            println it
        }
    }
}