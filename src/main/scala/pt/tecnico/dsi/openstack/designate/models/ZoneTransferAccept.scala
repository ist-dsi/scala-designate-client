package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

object ZoneTransferAccept {
  implicit val codec: Codec.AsObject[ZoneTransferAccept] = deriveCodec(renaming.snakeCase)
}
case class ZoneTransferAccept(
  id: String,
  key: Option[String],
  status: Status,
  projectId: String,
  zoneId: String,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  zoneTransferRequestId: String,
  links: List[Link] = List.empty,
) extends Identifiable
