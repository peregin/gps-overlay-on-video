package peregin.gpv.util

import org.specs2.mutable.Specification

/**
 * Created by mikwat on 09/01/15.
 */
class UnitConverterSpec extends Specification {

  /*
   * distance
   */

  "convert distance to metric" in {
    UnitConverter.distance(100.0, "Metric") === 100.0
  }

  "convert distance to standard" in {
    val output = UnitConverter.distance(100.0, "Standard")
    (output - 62.1371).abs <= 0.0001
  }

  "convert distance to nautical" in {
    val output = UnitConverter.distance(100.0, "Marine")
    (output - 53.995680).abs <= 0.0001
  }

  "distance units in metric" in {
    UnitConverter.distanceUnits("Metric") === "km"
  }

  "distance units in standard" in {
    UnitConverter.distanceUnits("Standard") === "miles"
  }

  "distance units in marine" in {
    UnitConverter.distanceUnits("Marine") === "NMs"
  }

  /*
   * speed
   */

  "convert speed to metric" in {
    UnitConverter.speed(100.0, "Metric") === 100.0
  }

  "convert speed to standard" in {
    val output = UnitConverter.speed(100.0, "Standard")
    (output - 62.1371).abs <= 0.0001
  }

  "convert speed to marine" in {
    val output = UnitConverter.speed(100.0, "Marine")
    (output - 53.995680).abs <= 0.0001
  }

  "speed units in metric" in {
    UnitConverter.speedUnits("Metric") === "km/h"
  }

  "speed units in standard" in {
    UnitConverter.speedUnits("Standard") === "mph"
  }

  "speed units in marine" in {
    UnitConverter.speedUnits("Marine") === "knots"
  }

  /*
   * elevation
   */

  "convert elevation to metric" in {
    UnitConverter.elevation(100.0, "Metric") === 100.0
  }

  "convert elevation to standard" in {
    val output = UnitConverter.elevation(100.0, "Standard")
    (output - 328.084).abs <= 0.001
  }

  "elevation units in metric" in {
    UnitConverter.elevationUnits("Metric") === "m"
  }

  "elevation units in standard" in {
    UnitConverter.elevationUnits("Standard") === "ft"
  }

  "elevation units in marine" in {
    UnitConverter.elevationUnits("Marine") === "m"
  }
}
