package pt.tecnico.dsi.openstack.designate.models

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Limit {
  implicit val codec: Codec.AsObject[Limit] = deriveCodec(renaming.snakeCase)
}
case class Limit(
  maxPageLimit: Int,
  maxRecordsetNameLength: Int,
  maxRecordsetRecords: Int,
  maxZoneNameLength: Int,
  maxZoneRecords: Int,
  maxZoneRecordsets: Int,
  maxZones: Int,
  minTtl: Option[Int]
)

