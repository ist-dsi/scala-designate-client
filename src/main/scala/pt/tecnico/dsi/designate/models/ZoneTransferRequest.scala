package pt.tecnico.dsi.designate.models

import java.time.OffsetDateTime

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object ZoneTransferRequest {
  implicit val codec: Codec.AsObject[ZoneTransferRequest] = deriveCodec[ZoneTransferRequest](renaming.snakeCase, false, None)
}

case class ZoneTransferRequest (
  key: String,
  status: Status,
  projectId: String,
  zoneId: String,
  description: String,
  zoneName: String,
  createdAt: OffsetDateTime,
  updatedAt: Option[OffsetDateTime],
  targetProjectId: Option[String],
  version: Integer,
  zoneTransferRequestId: String
)
