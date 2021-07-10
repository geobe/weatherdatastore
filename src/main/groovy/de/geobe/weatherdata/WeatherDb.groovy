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

class WeatherDb {

    Server tcpServer
    Server webServer
    def dwdXmlClient = new DwdXmlClient()
    def weatherDb
    DaoHibernate<DwdForecast> forecastDao

    WeatherDb() {
        runServer()
        weatherDb = new DbHibernate(['de.geobe.weatherdata.DwdForecast',
                                     'de.geobe.weatherdata.DwdMosmixStation'])
        forecastDao = new DaoHibernate<DwdForecast>(DwdForecast.class, weatherDb)
    }

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