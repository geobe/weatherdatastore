/*
 * MIT License
 *
 * Copyright (c) 2021  Georg Beier
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
 */

package de.geobe.weatherdata

import de.geobe.configuration.Configurator
import groovy.json.JsonSlurper

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RestClient {

    def readRadiationForecast(){
        def cfg = Configurator.readConfig('config.json')
        def lat = cfg.location.lat
        def lon = cfg.location.lon
        def baseurl = cfg.weather.baseurl
        def verbs = cfg.weather.verbs
        def appid = cfg.weather.apikey

        def zoneId = ZoneId.systemDefault()
        def timeformat = DateTimeFormatter.ofPattern("dd.MM.yy  HH.mm")

        def slurper = new JsonSlurper()
        def query = {String verb ->
            def uri = "$baseurl$verb?lat=$lat&lon=$lon&appid=$appid".toString()
            def connection = new URL(uri).openConnection()
            assert connection.responseCode == HttpURLConnection.HTTP_OK
            def reader = connection.inputStream.newReader()
            slurper.parse(reader)
        }

        def response = query verbs[3]
        List list = response.list.each{ Map entry ->
            def time = entry.dt
            def rad = entry.radiation
            def pot = Instant.ofEpochSecond(time).atZone(zoneId).format(timeformat)
            println "At $pot: $rad"
        }

    }

    static void main(String[] args) {
        new RestClient().readRadiationForecast()
    }
}


