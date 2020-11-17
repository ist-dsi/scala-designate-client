package pt.tecnico.dsi.openstack.designate

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import cats.Show

package object models {
  // Show we instead use https://github.com/ChristopherDavenport/cats-time?
  implicit val showOffsetDateTime: Show[LocalDateTime] = Show.show(_.format(ISO_LOCAL_DATE_TIME))
}
