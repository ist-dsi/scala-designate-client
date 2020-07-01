package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

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
case class ZoneTransferRequest (
  key: String,
  status: Status,
  projectId: String,
  zoneId: String,
  description: String,
  zoneName: String,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  targetProjectId: Option[String],
  // Official API says `version` should appear, but does not :/
  // version: Integer,
)
