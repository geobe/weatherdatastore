package de.geobe.configuration

import groovy.json.JsonSlurper


class Configurator {

    private static configuration

    static getConfiguration() {
        return configuration
    }

    /**
     * read basic configuration parameters from a JSON file
     * @param filename
     * @return a map representing the json file content
     */
    static readConfig(String filename) {
        JsonSlurper slurper = new JsonSlurper()
        URL cfgUrl = Configurator.classLoader.getResource(filename)
        File cfgFile = new File(cfgUrl.getPath())
        configuration = slurper.parse(cfgFile)
    }

}
