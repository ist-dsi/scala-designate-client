package pt.tecnico.dsi.designate.models

import java.time.OffsetDateTime

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Recordset {
  implicit val codec: Codec.AsObject[Recordset] = deriveCodec[Recordset](renaming.snakeCase, false, None)
}

case class Recordset(
  projectId: String,
  name: String,
  TTL: String,
  status: Status,
  action: Action,
  zoneId: String,
  zoneName: String,
  description: String,
  `type`: String,
  version: Integer,
  createdAt: OffsetDateTime,
  updatedAt: OffsetDateTime,
  records: Seq[String]
)

