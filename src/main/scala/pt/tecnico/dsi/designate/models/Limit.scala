package pt.tecnico.dsi.designate.models

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Limit {
  implicit val codec: Codec.AsObject[Limit] = deriveCodec[Limit](renaming.snakeCase, false, None)
}

case class Limit(
  maxPageLimit: Integer,
  maxRecordsetNameLength: Integer,
  maxRecordsetRecords: Integer,
  maxZoneNameLength: Integer,
  maxZoneRecords: Integer,
  maxZoneRecordsets: Integer,
  maxZones: Integer,
  minTtl: Integer,
)

