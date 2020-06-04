package pt.tecnico.dsi.designate.models

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Quota {
  implicit val codec: Codec.AsObject[Quota] = deriveCodec(renaming.snakeCase)
}

case class Quota(
  apiExportSize: Integer,
  recordsetRecords: Integer,
  zoneRecords: Integer,
  zoneRecordsets: Integer,
  zones: Integer
)
