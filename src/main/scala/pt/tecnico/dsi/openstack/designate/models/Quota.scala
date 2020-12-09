package pt.tecnico.dsi.openstack.designate.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Quota {
  object Update {
    implicit val codec: Codec.AsObject[Update] = deriveCodec(renaming.snakeCase)
    implicit val show: ShowPretty[Update] = derived.semiauto.showPretty
  }
  case class Update(
    zones: Option[Int] = None,
    zoneRecords: Option[Int] = None,
    zoneRecordsets: Option[Int] = None,
    recordsetRecords: Option[Int] = None,
    apiExportSize: Option[Int] = None,
  )
  
  implicit val codec: Codec.AsObject[Quota] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[Quota] = derived.semiauto.showPretty
}
case class Quota(
  zones: Int,
  zoneRecords: Int,
  zoneRecordsets: Int,
  recordsetRecords: Int,
  apiExportSize: Int,
)
