package de.geobe.weatherdata

import de.geobe.architecture.persist.DaoHibernate
import de.geobe.architecture.persist.DbHibernate
import org.h2.server.web.WebServer
import org.h2.tools.Server

import java.util.concurrent.TimeUnit

import static de.geobe.weatherdata.PeriodicAcquisition.timeformat
import static de.geobe.weatherdata.PeriodicAcquisition.zoneId

import java.time.Instant

class WeatherDb {

    Server tcpServer
    Server webServer
    def dwdXmlClient = new DwdXmlClient()
    def weatherDb
    DaoHibernate<DwdForecast> forecastDao
    def startupAcquisition = true

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
        def newest = forecastDao.find('select max(issuedAt) from DwdForecast')
        if(newest && newest[0] != issueTime) {
            forecasts.each {
                forecastDao.save it
            }
            forecastDao.closeSession()
            def now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId).format(timeformat)
            println "\ndataset saved at $now"
            true
        } else {
            println '\ndataset already exists'
            false
        }
    }

    def dwdDataAcquisitionTask = new Runnable() {
        @Override
        void run() {
            def success = fetchAndStoreDwdData()
            if(!startupAcquisition && !success) {
                PeriodicAcquisition.retry(dwdDataAcquisitionTask)
                println "try again in ${PeriodicAcquisition.period / 2} ${PeriodicAcquisition.unit.toString().toLowerCase()}"
            }
            startupAcquisition = false
        }
    }



    static void main(String[] args) {
        def weatherDb = new WeatherDb()
        def future = PeriodicAcquisition.execute(weatherDb.dwdDataAcquisitionTask)
        def nextex = future.getDelay(TimeUnit.SECONDS)
        def now = System.currentTimeSeconds()
        def toex = now + nextex
        def iToex = Instant.ofEpochSecond(toex).atZone(zoneId).format(timeformat)
        println "next execution in ${nextex} s at $iToex"
    }
}