package pt.tecnico.dsi.designate.models

import java.time.{LocalDateTime, OffsetDateTime}

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object ZoneTransferAccept {
  implicit val codec: Codec.AsObject[ZoneTransferAccept] = deriveCodec(renaming.snakeCase)
}

case class ZoneTransferAccept (
  key: Option[String],
  status: Status,
  projectId: String,
  zoneId: String,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  zoneTransferRequestId: String
)
