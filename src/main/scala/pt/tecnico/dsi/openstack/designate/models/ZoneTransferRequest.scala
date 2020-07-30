package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

object ZoneTransferRequest {
  implicit val codec: Codec.AsObject[ZoneTransferRequest] = deriveCodec(renaming.snakeCase)

  object Create {
    implicit val codec: Codec.AsObject[Create] = deriveCodec(renaming.snakeCase)
  }
  case class Create(
    description: Option[String] = None,
    targetProjectId: Option[String] = None,
  )

  object Update {
    implicit val codec: Codec.AsObject[Update] = deriveCodec(renaming.snakeCase)
  }
  case class Update(
    description: Option[String] = None,
    targetProjectId: Option[String] = None,
  )
}
case class ZoneTransferRequest(
  id: String,
  key: String,
  status: Status,
  projectId: String,
  zoneId: String,
  zoneName: String,
  description: Option[String] = None,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  targetProjectId: Option[String],
  links: List[Link] = List.empty,
) extends Identifiable
