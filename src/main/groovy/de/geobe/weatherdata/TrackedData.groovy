package de.geobe.weatherdata

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class TrackedData {

    static zoneId = ZoneId.systemDefault()
    static timeformat = DateTimeFormatter.ofPattern("dd.MM.yy  HH.mm")

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id
//    @Temporal(TemporalType.TIMESTAMP)
    Long issuedAt
    Long forecastTime

    @Override
    String toString() {
        return "retrieved ${longAsTime(issuedAt)}, " +
                "${hoursAhead()} hours forecast for ${longAsTime(forecastTime)}"
    }

    def hoursAhead() {
        String.format('% 5.1f', (forecastTime - issuedAt) / 3600.0)
    }

    def longAsTime(long timestamp) {
        Instant.ofEpochSecond(timestamp).atZone(zoneId).format(timeformat)
    }
}
