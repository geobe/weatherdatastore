package de.geobe.weatherdata


import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

class DwdXmlClient {
    static final namespaces = [
            dwd : "https://opendata.dwd.de/weather/lib/pointforecast_dwd_extension_V1_0.xsd",
            gx  : "http://www.google.com/kml/ext/2.2",
            xal : "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0",
            kml : "http://www.opengis.net/kml/2.2",
            atom: "http://www.w3.org/2005/Atom"
    ]

    static final MosmixSUrl =
            'https://opendata.dwd.de/weather/local_forecasts/mos/MOSMIX_S/all_stations/kml/MOSMIX_S_LATEST_240.kmz'
    static final MosmixLUrl =
            'https://opendata.dwd.de/weather/local_forecasts/mos/MOSMIX_L/single_stations/10577/kml/MOSMIX_L_LATEST_10577.kmz'
    static final MosmixSBaseUrl =
            'https://opendata.dwd.de/weather/local_forecasts/mos/MOSMIX_S/all_stations/kml/MOSMIX_S_'
    static final MosmixTimeFormat = DateTimeFormatter.ofPattern('yyyyMMddHH')
    static final MosmixPostfix = '_240.kmz'

    static final String ISSUE_TIME = 'IssueTime'
    static final String FORECAST_TIME_STEPS = 'ForecastTimeSteps'
    static final String PLACEMARK = 'Placemark'
    static final String TIME_STEP = 'TimeStep'
    static final int MAX_TIME_STEPS = 120
    static final String EXTENDED_DATA = 'ExtendedData'
    static final String FORECAST = 'Forecast'

//    def slurper = new XmlSlurper()

    def xmlInputFactory = XMLInputFactory.newInstance()

    def fetchSampleData(def filename = 'samples/MOSMIX_L_2021070215_10577.kml', String placemark = '10577') {
        URL xmlUrl = DwdXmlClient.classLoader.getResource(filename)
        def inStream = xmlUrl.openStream()
        def eventReader = xmlInputFactory.createXMLEventReader(inStream)
        def result = staxDwdMosMix(eventReader, placemark)
        inStream.close()
        result
    }

    def fetchWebData(def url = MosmixLUrl, String placemark = '10577') {
        URL webUrl = new URL(url)
        def rawStream = webUrl.openStream()
        parseZippedStream(rawStream, placemark)
    }

    def fetchZippedSampleData(def filename = 'samples/MOSMIX_L_LATEST_10577.kmz', String placemarkName = '10577') {
        URL zipUrl = DwdXmlClient.classLoader.getResource(filename)
        def rawStream = zipUrl.newInputStream()
        parseZippedStream(rawStream, placemarkName)
    }

    def parseZippedStream(InputStream rawStream, String placemarkName) {
        def zipInStream = new ZipInputStream(rawStream)
        if (zipInStream.getNextEntry()) {
            def eventReader = xmlInputFactory.createXMLEventReader(zipInStream)
            def result = staxDwdMosMix(eventReader, placemarkName)
            zipInStream.close()
            result
        }
    }

    List<DwdForecast> staxDwdMosMix(XMLEventReader reader, String placemark) {
        List<DwdForecast> forecasts
        Long issueTime
        def hourglass = ['/', '-', '\\', '|']
        def cnt = 0
        while (reader.hasNext()) {
            def element = reader.nextEvent()
            if (element.isStartElement()) {
                def e = element.asStartElement()
                def n = e.name
                if (n.prefix == 'dwd' && n.localPart == ISSUE_TIME) {
                    // handle issue time
                    print "\b${hourglass[++cnt%4]}"
                    issueTime = handleIssueTime(reader)
                } else if (n.prefix == 'dwd' && n.localPart == FORECAST_TIME_STEPS) {
                    // handle time steps
                    print "\b${hourglass[++cnt%4]}"
                    forecasts = handleForecastTimeSteps(reader, issueTime)
                } else if (n.prefix == 'kml' && n.localPart == PLACEMARK) {
                    // step into placemark
                    print "\b${hourglass[++cnt%4]}"
                    handlePlacemarks(reader, placemark, forecasts)
                }
            }
        }
        forecasts
    }

    def handleIssueTime(XMLEventReader reader) {
        Long issued
        while (reader.hasNext()) {
            def element = reader.nextEvent()
            if (element.isEndElement()) {
                def n = element.asEndElement().name
                assert n.prefix == 'dwd' && n.localPart == ISSUE_TIME
                break
            } else if (element.isCharacters()) {
                def chars = element.asCharacters().data
                issued = utc2long(chars)
            }
        }
        issued
    }

    def handleForecastTimeSteps(XMLEventReader reader, Long issueTime) {
        List<DwdForecast> forecasts = new ArrayList<>()
        List<Long> steps = new ArrayList<>()
        while (reader.hasNext()) {
            def element = reader.nextEvent()
            if (element.isEndElement()) {
                def n = element.asEndElement().name
                if (n.prefix == 'dwd' && n.localPart == FORECAST_TIME_STEPS) {
                    break
                } else {
                    assert n.prefix == 'dwd' && n.localPart == TIME_STEP
                }
            } else if (element.isStartElement()) {
                def n = element.asStartElement().name
                assert n.prefix == 'dwd' && n.localPart == TIME_STEP
            } else if (element.isCharacters()) {
                def chars = element.asCharacters().data
                if (!chars.matches('\\s*') && steps.size() < MAX_TIME_STEPS) {
                    steps << utc2long(chars)
                }
            }
        }
        steps.each {
            forecasts << new DwdForecast(issuedAt: issueTime, forecastTime: it)
        }
        forecasts
    }

    def handlePlacemarks(XMLEventReader reader, String placemark, List<DwdForecast> forecasts) {
        boolean foundPlace = false
        while (reader.hasNext()) {
            def element = reader.nextEvent()
            if (element.isEndElement()) {
                def n = element.asEndElement().name
                if (n.prefix == 'kml' && n.localPart == PLACEMARK) {
                    break
                }
            } else if (element.isStartElement()) {
                def n = element.asStartElement().name
                if (n.prefix == 'kml' && n.localPart == 'name') {
                    def pm = reader.nextEvent()
                    assert pm.isCharacters()
                    if (placemark == pm.asCharacters().data) {
                        foundPlace = true
                    }
                } else if (foundPlace && n.prefix == 'kml' && n.localPart == EXTENDED_DATA) {
                    handleForecasts(reader, forecasts)
                    foundPlace = false
                }
            }
        }
    }

    def handleForecasts(XMLEventReader reader, List<DwdForecast> forecasts) {
        while (reader.hasNext()) {
            def element = reader.nextEvent()
            if (element.isEndElement()) {
                def n = element.asEndElement().name
                if (n.prefix == 'kml' && n.localPart == EXTENDED_DATA) {
                    break
                }
            } else if (element.isStartElement()) {
                def n = element.asStartElement().name
                if (n.prefix == 'dwd' && n.localPart == FORECAST) {
                    def attributeValue = element.asStartElement().attributes[0].value
                    if (attributeValue in DwdForecast.xmlMapping.keySet()) {
                        parseValue(reader, attributeValue, forecasts)
                    }
                }
            }
        }
    }

    def parseValue(XMLEventReader reader, String attributeKey, List<DwdForecast> dwdForecasts) {
        while (reader.hasNext()) {
            def element = reader.nextEvent()
            if (element.isEndElement()) {
                def n = element.asEndElement().name
                if (n.prefix == 'dwd' && n.localPart == FORECAST) {
                    break
                }
            } else if (element.isStartElement()) {
                def n = element.asStartElement().name
                if (n.prefix == 'dwd' && n.localPart == 'value') {
                    def data = reader.nextEvent().asCharacters().data
                    def values = data.split()
                    def attribute = DwdForecast.xmlMapping.(attributeKey)
                    dwdForecasts.eachWithIndex { DwdForecast forecast, int i ->
                        forecast.(attribute) = (values[i] == '-' ? null : Float.parseFloat(values[i]))
                    }
                }
            }
        }

    }

    long utc2long(issued) {
        Instant.parse(issued).getEpochSecond()
    }

    static void main(String[] args) {
//        def forecasts = new DwdXmlClient().useZippedSampleData('samples/MOSMIX_S_LATEST_240.kmz')
//        def forecasts = new DwdXmlClient().useSampleData()
        def forecasts = new DwdXmlClient().fetchWebData(MosmixSUrl)
        forecasts.each { println it }
    }
}
