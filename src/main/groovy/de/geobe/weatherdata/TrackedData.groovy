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
        return "${longAsTime(forecastTime)} forecast, ${hoursAhead()} hours ahead, " +
                "issued at ${longAsTime(issuedAt)}"
    }

    def hoursAhead() {
        String.format('% 5.1f', (forecastTime - issuedAt) / 3600.0)
    }

    def longAsTime(long timestamp) {
        Instant.ofEpochSecond(timestamp).atZone(zoneId).format(timeformat)
    }
}
