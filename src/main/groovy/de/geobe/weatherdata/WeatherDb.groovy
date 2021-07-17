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
import org.h2.tools.Server

import java.time.ZoneId
import java.util.concurrent.TimeUnit

import static de.geobe.weatherdata.PeriodicAcquisition.timeformat
import static de.geobe.weatherdata.PeriodicAcquisition.zoneId
import static de.geobe.weatherdata.PeriodicAcquisition.acqPeriod
import static de.geobe.weatherdata.PeriodicAcquisition.acqUnit
import static de.geobe.weatherdata.PeriodicAcquisition.dwdFullHourOffset
import static de.geobe.weatherdata.DwdXmlClient.MosmixSBaseUrl
import static de.geobe.weatherdata.DwdXmlClient.MosmixTimeFormat
import static de.geobe.weatherdata.DwdXmlClient.MosmixPostfix

import java.time.Instant

/**
 * Main data acquisition class that reads data from German weather service (DWD)
 * and stores it in an H2 database
 * @author Georg Beier
 */
class WeatherDb {

    Server tcpServer
    Server webServer
//    def dwdXmlClient = new DwdXmlClient()
    def weatherDb
    DaoHibernate<DwdForecast> forecastDao

    /**
     * Default constructor starts H2 server and binds two Entity classes for DWD weather forecast records
     * to the database using hibernate.
     */
    WeatherDb() {
        runServer()
        weatherDb = new DbHibernate(['de.geobe.weatherdata.DwdForecast',
                                     'de.geobe.weatherdata.DwdMosmixStation'])
        forecastDao = new DaoHibernate<DwdForecast>(DwdForecast.class, weatherDb)
    }

    /**
     * start H2 tcp and web server
     * @return
     */
    def runServer() {
        tcpServer = Server.createTcpServer('-baseDir', '~/H2', '-tcpAllowOthers', '-tcpDaemon')
        if (!tcpServer.isRunning(true)) {
            tcpServer.start()
            println "tcpServer startet"
        }
        webServer = Server.createWebServer('-baseDir', '~/H2', '-webAllowOthers', '-webDaemon')
        if (!webServer.isRunning(true)) {
            webServer.start()
            println "webserver startet"
        }
    }

    /**
     * Read and parse weather forecast data from XML file on DWD opendata website into DwdForecast objects
     * and store them in the database. The parsing is actually by DwdXmlClient class.
     * @param url web location of xml file. DwdXmlClient.MosmixSUrl or DwdXmlClient.MosmixLUrl are possible values.
     * @param placemark select the weather station for the forecast. E.g., '10577' is Chemnitz.
     * @return map telling if data were saved and issuedAt-timestamp of most actual record
     */
    def fetchAndStoreDwdData(def url = DwdXmlClient.MosmixSUrl, String placemark = '10577') {
        def forecasts = new DwdXmlClient().fetchWebData(url, placemark)
        def issueTime = forecasts[0].issuedAt
        def latest = forecastDao.find('select max(issuedAt) from DwdForecast')?.first()
        if(latest != issueTime) {
            forecasts.each {
                forecastDao.save it
            }
            forecastDao.closeSession()
            def now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId).format(timeformat)
            println "\ndataset saved at $now"
            [saved: true, timestamp: issueTime]
        } else {
            println '\ndataset already exists'
            [saved: false, timestamp: latest]
        }
    }

    /**
     * Task for periodic update of weather data. MosmixS data are updated hourly, MosmixL data four times a day
     */
    def dwdDataAcquisitionTask = new Runnable() {
        @Override
        void run() {
            // get the latest available dataset from dwd website
            def result = fetchAndStoreDwdData()
            def latestInDb = result.timestamp
            def savedToDb = result.saved
            def elapsed = System.currentTimeSeconds() - latestInDb
            def period = acqUnit.toSeconds(acqPeriod)
            if(! savedToDb && elapsed > period) {
                // seems we are at one of the irregular provisionings of dwd data files
                println "try again in ${acqPeriod / 2} ${acqUnit.toString().toLowerCase()}"
                PeriodicAcquisition.retry(dwdDataAcquisitionTask)
            }
        }
    }

    /**
     * Try to fill up missing datasets in the database if program was down for some time. DWD provides
     * XML files with forecasts from the most recent 48 hours.
     */
    def fillupMissingDatasets() {
        def latest = forecastDao.find('select max(issuedAt) from DwdForecast')?.first()
        def now = System.currentTimeSeconds()
        def periodSeconds = acqUnit.toSeconds(acqPeriod)
        def offset = acqUnit.toSeconds(dwdFullHourOffset)
        def missingPeriods = (now - offset - latest).intdiv(periodSeconds)
        println "missing $missingPeriods datasets:"
        for(int i = 1; i <= missingPeriods; i++) {
            def url = MosmixSBaseUrl + Instant.ofEpochSecond(latest+i*periodSeconds).
                    atZone(ZoneId.of('Z')).format(MosmixTimeFormat) + MosmixPostfix
            println "try retrieving from $url"
            try {
                fetchAndStoreDwdData(url)
                println " done"
            } catch (IOException ex) {
                ex.
                println "failed: $ex"
            }
        }

    }

    /**
     * Run program in demon mode for regular acquisition and storage of DWD weather forecasts
     * @param args no args are evaluated
     */
    static void main(String[] args) {
        def weatherDb = new WeatherDb()
        weatherDb.fillupMissingDatasets()
        def future = PeriodicAcquisition.execute(weatherDb.dwdDataAcquisitionTask)
        def nextex = future.getDelay(TimeUnit.SECONDS)
        def now = System.currentTimeSeconds()
        def toex = now + nextex
        def iToex = Instant.ofEpochSecond(toex).atZone(zoneId).format(timeformat)
        println "next execution in $nextex s at $iToex"
    }
}