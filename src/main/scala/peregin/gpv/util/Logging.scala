package peregin.gpv.util

import org.slf4j.LoggerFactory


object Logging extends Logging

trait Logging {

  protected lazy val log = LoggerFactory.getLogger(getClass)

  // arguments evaluated by name, so when the given logging level is not enabled the string formatting is not evaluated

  def info(s: => String) = if (log.isInfoEnabled) log.info(s)
  def debug(s: => String) = if (log.isDebugEnabled) log.debug(s)
  def error(s: => String, any: Throwable) = if (log.isErrorEnabled) log.error(s, any)
}
