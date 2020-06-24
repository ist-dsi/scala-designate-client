package pt.tecnico.dsi.designate.models

import java.time.{LocalDateTime, OffsetDateTime}

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object ZoneTransferRequest {
  implicit val codec: Codec.AsObject[ZoneTransferRequest] = deriveCodec[ZoneTransferRequest](renaming.snakeCase)
}

object ZoneTransferRequestCreate {
  implicit val codec: Codec.AsObject[ZoneTransferRequestCreate] = deriveCodec[ZoneTransferRequestCreate](renaming.snakeCase)
}

object ZoneTransferRequestUpdate {
  implicit val codec: Codec.AsObject[ZoneTransferRequestUpdate] = deriveCodec[ZoneTransferRequestUpdate](renaming.snakeCase)
}

case class ZoneTransferRequestUpdate(
  description: Option[String],
  targetProjectId: Option[String]
)

case class ZoneTransferRequestCreate(
  description: String,
  targetProjectId: Option[String]
)

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
