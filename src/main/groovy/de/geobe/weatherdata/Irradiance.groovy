package de.geobe.weatherdata

import javax.persistence.Entity

@Entity
class Irradiance extends TrackedData {
    Float globalHorizontal
    Float directNormal
    Float diffuseHorizontal
    Float clearSkyGlobalHorizontal
    Float clearSkyDirectNormal
    Float clearSkyDiffuseHorizontal

    @Override
    String toString() {
        return """${super.toString()} 
\tghi: $globalHorizontal, dni: $directNormal, dhi: $diffuseHorizontal
\tghi_cs: $clearSkyGlobalHorizontal dni_cs: $clearSkyDirectNormal dhi_cs: $clearSkyDiffuseHorizontal""".toString()
    }
}
