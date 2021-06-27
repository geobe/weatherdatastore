package de.geobe.weatherdata

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class TrackedData {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id
//    @Temporal(TemporalType.TIMESTAMP)
    Long timestamp
    Integer version
}
