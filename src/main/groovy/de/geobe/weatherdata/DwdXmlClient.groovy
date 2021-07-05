package de.geobe.weatherdata

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult

import java.time.Instant
import java.util.zip.ZipInputStream

class DwdXmlClient {
    static namespaces = [
            dwd : "https://opendata.dwd.de/weather/lib/pointforecast_dwd_extension_V1_0.xsd",
            gx  : "http://www.google.com/kml/ext/2.2",
            xal : "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0",
            kml : "http://www.opengis.net/kml/2.2",
            atom: "http://www.w3.org/2005/Atom"
    ]

    static MosmixSUrl =
            'https://opendata.dwd.de/weather/local_forecasts/mos/MOSMIX_S/all_stations/kml/MOSMIX_S_LATEST_240.kmz'
    static MosmixLUrl =
            'https://opendata.dwd.de/weather/local_forecasts/mos/MOSMIX_L/single_stations/10577/kml/MOSMIX_L_LATEST_10577.kmz'

    def slurper = new XmlSlurper()

    def useSampleData(def filename = 'samples/MOSMIX_L_2021070215_10577.kml') {
        URL xmlUrl = DwdXmlClient.classLoader.getResource(filename)
        File xmlFile = new File(xmlUrl.getPath())
        def root = slurper.parse(xmlFile)
        parseDwdMosMix(root)
    }

    def useWebData(def url = MosmixLUrl, String placemarkName = '10577') {
        URL webUrl = new URL(url)
        def rawStream = webUrl.newInputStream()
        parseZippedStream(rawStream, placemarkName)
    }

    def useZippedSampleData(def filename = 'samples/MOSMIX_L_LATEST_10577.kmz', String placemarkName = '10577') {
        URL zipUrl = DwdXmlClient.classLoader.getResource(filename)
        def rawStream = zipUrl.newInputStream()
        parseZippedStream(rawStream, placemarkName)
    }

    def parseZippedStream(BufferedInputStream rawStream, String placemarkName) {
        def zipInStream = new ZipInputStream(rawStream)
        if (zipInStream.getNextEntry()) {
            def root = slurper.parse(zipInStream)
            def result = parseDwdMosMix(root, placemarkName)
            zipInStream.close()
            result
        }
    }

    List<DwdForecast> parseDwdMosMix(GPathResult root, String placemarkName = '10577') {
        List<DwdForecast> forecasts = new ArrayList<>()
        root.declareNamespace(namespaces)
        GPathResult productDefinition = root.'kml:Document'.'kml:ExtendedData'.'dwd:ProductDefinition'
        productDefinition.declareNamespace(namespaces)
        def issued = productDefinition.'dwd:IssueTime'.text()
        def issuedAt = Instant.parse(issued).getEpochSecond()
        GPathResult timesteps = productDefinition.'dwd:ForecastTimeSteps'
        timesteps.declareNamespace(namespaces)
        for(GPathResult timestep : timesteps.'dwd:TimeStep') {
            def forecastTime = Instant.parse(timestep.text()).getEpochSecond()
            if((forecastTime - issuedAt) > 72 * 3600) {
                break
            }
            def forecast = new DwdForecast(issuedAt: issuedAt, forecastTime: forecastTime)
            forecasts << forecast
        }

        root.'kml:Document'.'kml:Placemark'.find {
            it.'kml:name'.text() == placemarkName
        }.each { GPathResult placemark ->
            println "found ${placemark.'kml:description'}"
            placemark.'kml:ExtendedData'.'dwd:Forecast'.findAll {
                it.'@dwd:elementName' in DwdForecast.xmlMapping.keySet()
            }.each {
                def n = it.'@dwd:elementName'
                String v = it.'dwd:value'
                def vals = v.split()
                def attribute = DwdForecast.xmlMapping[n]
                for(int i = 0; forecasts[i] && i < vals.size(); i++) {
                    String entry = vals[i]
                    forecasts[i].(attribute) = (entry == '-' ? null : Float.parseFloat(entry))
                }
            }
        }
        forecasts
    }

    static void main(String[] args) {
//        def forecasts = new DwdXmlClient().useZippedSampleData('samples/MOSMIX_S_LATEST_240.kmz')
        def forecasts = new DwdXmlClient().useWebData(MosmixSUrl, 'EW020')
        forecasts.each { println it }
    }
}
