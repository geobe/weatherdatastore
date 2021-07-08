package de.geobe.weatherdata

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Periodically get weather data from an internet site and save it to the database.
 * Data access should be short after a typical update time, e.g. 2 minutes after every hour.<br>
 * DWD (German weater service - Deutscher Wetterdienst) seems to update their hourly short forecast around
 * 20 minutes after every full hour.
 */
class PeriodicAcquisition {

    static long period = 60
    static long dwdFullHourOffset = 25
    static TimeUnit unit = TimeUnit.MINUTES
    static timeformat = DateTimeFormatter.ofPattern("dd.MM.yy  HH.mm.ss.SSS")
    static zoneId = ZoneId.systemDefault()

    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1)
    ScheduledFuture future

    static testTask = new Runnable() {
        @Override
        void run() {
            def time = System.currentTimeMillis()
            def pot = Instant.ofEpochMilli(time).atZone(zoneId).format(timeformat)
            println "running at $pot"
        }
    }

    static dwdDataAcquisitionTask = new Runnable() {
        @Override
        void run() {

        }
    }

    def execute(Runnable task) {
        // calculate initialDelay in milliseconds
        def unitModulo =  unit.toMillis(period)
        def now = System.currentTimeMillis()
        def delay = (unitModulo - (now % unitModulo) + unit.toMillis(dwdFullHourOffset))
        executor.execute(task)
        future = executor.scheduleAtFixedRate(task, delay, period, unit)
    }

    def

    static void main(String[] args) {
        def now = System.currentTimeMillis()
        def unitModulo =  unit.toMillis(period)
        def future = new PeriodicAcquisition().execute(testTask)
        def pot = Instant.ofEpochMilli(now).atZone(zoneId).format(timeformat)
        def delay = (unitModulo - (now % unitModulo) + unit.toMillis(dwdFullHourOffset))
        def start = Instant.ofEpochMilli(now + delay).atZone(zoneId).format(timeformat)
        println "now: $pot delay: $delay is $start"
    }
}
