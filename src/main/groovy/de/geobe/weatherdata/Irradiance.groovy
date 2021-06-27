package de.geobe.weatherdata

import javax.persistence.Entity

@Entity
class Irradiance extends TrackedData {
    double globalHorizontal
    double directNormal
    double diffuseHorizontal
    double clearSkyGlobalHorizontal
    double clearSkyDirectNormal
    double clearSkyDiffuseNormal
}
