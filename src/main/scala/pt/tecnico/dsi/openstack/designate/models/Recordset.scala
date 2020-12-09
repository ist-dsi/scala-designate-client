package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.derived
import cats.derived.ShowPretty
import cats.effect.Sync
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import io.chrisdavenport.cats.time.localdatetimeInstances
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.designate.DesignateClient
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

object Recordset {
  object Create {
    implicit val codec: Codec.AsObject[Create] = deriveCodec(renaming.snakeCase)
    implicit val show: ShowPretty[Create] = derived.semiauto.showPretty
  }
  case class Create(
    name: String,
    `type`: String,
    records: List[String],
    ttl: Option[Int] = None,
    description: Option[String] = None,
  )
  
  object Update {
    implicit val codec: Codec.AsObject[Update] = deriveCodec(renaming.snakeCase)
    implicit val show: ShowPretty[Update] = derived.semiauto.showPretty
  }
  case class Update(
    records: Option[List[String]] = None,
    ttl: Option[Int] = None,
    description: Option[String] = None,
  ) {
    lazy val needsUpdate: Boolean = {
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(records, ttl, description).exists(_.isDefined)
    }
  }
  
  implicit val codec: Codec.AsObject[Recordset] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[Recordset] = derived.semiauto.showPretty
}
case class Recordset(
  id: String,
  name: String,
  `type`: String,
  records: List[String],
  projectId: String,
  ttl: Option[Int],
  status: Status,
  action: Action,
  zoneId: String,
  zoneName: String,
  description: Option[String],
  version: Int,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  links: List[Link] = List.empty,
) extends Identifiable {
  def project[F[_]: Sync](implicit keystone: KeystoneClient[F]): F[Project] = keystone.projects(projectId)
  def zone[F[_]: Sync](implicit designate: DesignateClient[F]): F[Zone] = designate.zones(zoneId)
}

