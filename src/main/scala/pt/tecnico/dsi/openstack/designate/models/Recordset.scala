package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.effect.Sync
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.designate.DesignateClient
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

object Recordset {
  implicit val codec: Codec.AsObject[Recordset] = deriveCodec(renaming.snakeCase)

  object Create {
    implicit val codec: Codec.AsObject[Create] = deriveCodec(renaming.snakeCase)
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
  }
  case class Update(
    records: List[String],
    ttl: Option[Int] = None,
    description: Option[String] = None,
  )
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

