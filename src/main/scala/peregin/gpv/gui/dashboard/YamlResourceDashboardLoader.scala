package peregin.gpv.gui.dashboard

import com.fasterxml.jackson.annotation.{JsonIdentityInfo, ObjectIdGenerators}
import peregin.gpv.gui.gauge.GaugePainter
import peregin.gpv.util.YamlConverter

import java.io.{FileNotFoundException, InputStream}


object YamlResourceDashboardLoader {

  def loadDashboard(inputStream: InputStream): Dashboard = {
    val resource: DashboardResource = YamlConverter.read[DashboardResource](inputStream)
    return new DynamicResourceDashboard(resource.gauges.map(gauge => {
      GaugeSetup(
        gauge.x,
        gauge.y,
        gauge.size,
        gauge.width,
        gauge.height,
        getClass.getClassLoader.loadClass(gauge.clazz).getConstructor().newInstance().asInstanceOf[GaugePainter]
      )
    }))
  }

  def loadCpDashboard[T](clazz: Class[T], file: String): Dashboard = {
    var dashboard: Dashboard = null
    val input = clazz.getResourceAsStream(file)
    if (input == null) {
      throw new FileNotFoundException(clazz.getName + " " + file)
    }
    try {
      dashboard = loadDashboard(input)
    }
    finally {
      input.close();
    }
    return dashboard
  }

  @JsonIdentityInfo(generator = classOf[ObjectIdGenerators.None])
  case class GaugeResource(name: String, x: Double, y: Double, size: Option[Double], width: Option[Double], height: Option[Double], clazz: String) {

  }

  @JsonIdentityInfo(generator = classOf[ObjectIdGenerators.None])
  case class DashboardResource(code: String, name: String, gauges: Seq[GaugeResource]) {
  }
}
