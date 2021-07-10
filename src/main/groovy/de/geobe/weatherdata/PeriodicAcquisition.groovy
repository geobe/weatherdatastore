package de.geobe.weatherdata

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Periodically get weather data from an internet site and save it to the database.
 * Data access should be short after a typical update time, e.g. 2 minutes after every hour.<br>
 * DWD (German weater service - Deutscher Wetterdienst) seems to update their hourly short forecast around
 * 20 minutes after every full hour. But sometimes 20 min later. So try twice an hour.
 */
class PeriodicAcquisition {

    static long acqPeriod = 60
    static long dwdFullHourOffset = 25
    static TimeUnit acqUnit = TimeUnit.MINUTES
    static timeformat = DateTimeFormatter.ofPattern("dd.MM.yy  HH.mm.ss.SSS")
    static zoneId = ZoneId.systemDefault()

    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5)

    static testTask = new Runnable() {
        @Override
        void run() {
            def time = System.currentTimeMillis()
            def pot = Instant.ofEpochMilli(time).atZone(zoneId).format(timeformat)
            println "running at $pot"
        }
    }

    static execute(Runnable task) {
        // calculate everything in seconds
        def unitModulo =  acqUnit.toSeconds(acqPeriod)
        def now = System.currentTimeSeconds()
        def delay = (unitModulo - (now % unitModulo) + acqUnit.toSeconds(dwdFullHourOffset))%unitModulo
        def periodSeconds = acqUnit.toSeconds(acqPeriod)
        println "period: $periodSeconds s, delay: $delay s"
        def future = executor.scheduleAtFixedRate(task, delay, periodSeconds, TimeUnit.SECONDS)
//        executor.execute(task)
        future
    }

    static retry(Runnable task) {
        executor.schedule(task, acqPeriod.intdiv(2), acqUnit)
    }

    static void main(String[] args) {
        def now = System.currentTimeMillis()
        def unitModulo =  acqUnit.toMillis(acqPeriod)
        def future = execute(testTask)
        def pot = Instant.ofEpochMilli(now).atZone(zoneId).format(timeformat)
        def delay = (unitModulo - (now % unitModulo) + acqUnit.toMillis(dwdFullHourOffset))
        def start = Instant.ofEpochMilli(now + delay).atZone(zoneId).format(timeformat)
        println "now: $pot delay: $delay is $start"
    }
}
