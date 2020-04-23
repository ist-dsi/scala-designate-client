package pt.tecnico.dsi.designate.models

import java.time.OffsetDateTime

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object ZoneTransferAccept {
  implicit val codec: Codec.AsObject[ZoneTransferAccept] = deriveCodec[ZoneTransferAccept](renaming.snakeCase, false, None)
}

case class ZoneTransferAccept (
  key: String,
  status: Status,
  projectId: String,
  zoneId: String,
  createdAt: OffsetDateTime,
  updatedAt: Option[OffsetDateTime],
  zoneTransferRequestId: String
)
