package de.geobe.weatherdata

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Periodically get weather data from an internet site and save it to the database.
 * Data access should be shortly before a typical update time, e.g. 2 minutes before every hour.
 */
class PeriodicAcquisition {

    static long period = 2000
    static TimeUnit unit = TimeUnit.MILLISECONDS
    static timeformat = DateTimeFormatter.ofPattern("dd.MM.yy  HH.mm.ss.SSS")
    static zoneId = ZoneId.systemDefault()

    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1)
    ScheduledFuture future

    def testTask = new Runnable() {
        @Override
        void run() {
            def time = System.currentTimeMillis()
            def pot = Instant.ofEpochMilli(time).atZone(zoneId).format(timeformat)
            println "running at $pot"
        }
    }

    def execute() {
        // calculate initialDelay
        def unitModulo = 1000 // milliseconds
        def now = System.currentTimeMillis()
        def delay = unitModulo - (now % unitModulo)
        future = executor.scheduleAtFixedRate(testTask, delay, period, unit)
    }

    static void main(String[] args) {
        def time = System.currentTimeMillis()
        def future = new PeriodicAcquisition().execute()
        def pot = Instant.ofEpochMilli(time).atZone(zoneId).format(timeformat)
        println "starting at $pot"
    }
}
