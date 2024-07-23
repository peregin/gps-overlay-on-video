package peregin.gpv.gui.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import peregin.gpv.gui.gauge.GaugePainter
import peregin.gpv.util.YamlConverter

import java.io.{FileNotFoundException, InputStream}


object YamlResourceDashboardLoader {
  private val mapper = new ObjectMapper(new YAMLFactory());

  def loadDashboard(inputStream: InputStream): Dashboard = {
    val resource: DashboardResource = YamlConverter.read[DashboardResource](inputStream)
    return new DynamicResourceDashboard(resource.gauges.map(gauge => {
      GaugeSetup(
        gauge.x,
        gauge.y,
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
      throw new FileNotFoundException(clazz + " " + file)
    }
    try {
      dashboard = loadDashboard(input)
    }
    finally {
      input.close();
    }
    return dashboard
  }

  case class GaugeResource(name: String, x: Double, y: Double, width: Double, height: Double, clazz: String) {

  }

  case class DashboardResource(code: String, name: String, gauges: Seq[GaugeResource]) {
  }
}
