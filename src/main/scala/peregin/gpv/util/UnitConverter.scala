package peregin.gpv.util


object UnitConverter {

  def distance(value: Double, units: String): Double = {
    if (units == "Standard") value * 0.621371
    else if (units == "Marine") value * 0.539957
    else value
  }

  def distanceUnits(units: String): String = {
    if (units == "Standard") "miles"
    else if (units == "Marine") "NMs"
    else "km"
  }

  def speed(value: Double, units: String): Double = {
    if (units == "Standard") value * 0.621371
    else if (units == "Marine") value * 0.539957
    else value
  }

  def speedUnits(units: String): String = {
    if (units == "Standard") "mph"
    else if (units == "Marine") "knots"
    else "km/h"
  }

  def elevation(value: Double, units: String): Double = {
    if (units == "Standard") value * 3.28084
    else value
  }

  def elevationUnits(units: String): String = {
    if (units == "Standard") "ft" else "m"
  }

}
