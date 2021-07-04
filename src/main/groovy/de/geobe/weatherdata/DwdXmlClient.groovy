package de.geobe.weatherdata

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult

import java.time.Instant

import static java.time.format.DateTimeFormatter.ISO_INSTANT

class DwdXmlClient {
    static namespaces = [
            dwd : "https://opendata.dwd.de/weather/lib/pointforecast_dwd_extension_V1_0.xsd",
            gx  : "http://www.google.com/kml/ext/2.2",
            xal : "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0",
            kml : "http://www.opengis.net/kml/2.2",
            atom: "http://www.w3.org/2005/Atom"
    ]

    def slurper = new XmlSlurper()

    def useSampleData(def filename = 'samples/MOSMIX_L_2021070215_10577.kml') {
        List<DwdForecast> forecasts = new ArrayList<>()
        URL xmlUrl = DwdXmlClient.classLoader.getResource(filename)
        File xmlFile = new File(xmlUrl.getPath())
        def root = slurper.parse(xmlFile)
        root.declareNamespace(namespaces)
        GPathResult productDefinition = root.'kml:Document'.'kml:ExtendedData'.'dwd:ProductDefinition'
        productDefinition.declareNamespace(namespaces)
        def issued = productDefinition.'dwd:IssueTime'.text()
        def issuedAt = Instant.parse(issued).getEpochSecond()
        GPathResult timesteps = productDefinition.'dwd:ForecastTimeSteps'
        def steps = timesteps.'dwd:TimeStep'.size()
        timesteps.declareNamespace(namespaces)
        timesteps.'dwd:TimeStep'.each {
            def forecastTime = Instant.parse(it.text()).getEpochSecond()
            def forecast = new DwdForecast(issuedAt: issuedAt, forecastTime: forecastTime)
            forecasts << forecast
        }

        GPathResult placemarkData = root.'kml:Document'.'kml:Placemark'.'kml:ExtendedData'
        placemarkData.declareNamespace(namespaces)
        placemarkData.'dwd:Forecast'.findAll {
            it.'@dwd:elementName' in DwdForecast.xmlMapping.keySet()
        }.each {
            def n = it.'@dwd:elementName'
            String v = it.'dwd:value'
            def vals = v.split()
            def attribute = DwdForecast.xmlMapping[n]
            vals.eachWithIndex { String entry, int i ->
                forecasts[i].(attribute) = (entry == '-' ? null : Float.parseFloat(entry))
            }
        }
        forecasts
    }

    static void main(String[] args) {
        def forecasts = new DwdXmlClient().useSampleData()
        forecasts.each { println it }
    }
}
