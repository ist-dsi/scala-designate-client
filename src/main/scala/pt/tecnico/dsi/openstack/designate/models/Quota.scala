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
  ) {
    lazy val needsUpdate: Boolean = {
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(zones, zoneRecords, zoneRecordsets, recordsetRecords, apiExportSize).exists(_.isDefined)
    }
  }
  
  implicit val codec: Codec.AsObject[Quota] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[Quota] = derived.semiauto.showPretty
}

/**
 * A value of -1 means no limit.
 * @param zones number of zones allowed for each project.
 * @param zoneRecords number of records allowed per zone.
 * @param zoneRecordsets number of recordsets allowed per zone.
 * @param recordsetRecords number of records allowed per recordset.
 * @param apiExportSize number of recordsets allowed in a zone export.
 */
case class Quota(
  zones: Int,
  zoneRecords: Int,
  zoneRecordsets: Int,
  recordsetRecords: Int,
  apiExportSize: Int,
)
