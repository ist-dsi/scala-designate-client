package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.effect.Sync
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.designate.DesignateClient
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

object ZoneTransferRequest {
  implicit val codec: Codec.AsObject[ZoneTransferRequest] = deriveCodec(renaming.snakeCase)

  object Create {
    implicit val codec: Codec.AsObject[Create] = deriveCodec(renaming.snakeCase)
  }
  case class Create(
    description: Option[String] = None,
    targetProjectId: Option[String] = None,
  )

  object Update {
    implicit val codec: Codec.AsObject[Update] = deriveCodec(renaming.snakeCase)
  }
  case class Update(
    description: Option[String] = None,
    targetProjectId: Option[String] = None,
  )
}
case class ZoneTransferRequest(
  id: String,
  key: String,
  status: Status,
  projectId: String,
  zoneId: String,
  zoneName: String,
  description: Option[String] = None,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  targetProjectId: Option[String],
  links: List[Link] = List.empty,
) extends Identifiable {
  def project[F[_]: Sync](implicit keystone: KeystoneClient[F]): F[Project] = keystone.projects(projectId)
  def zone[F[_]: Sync](implicit neutron: DesignateClient[F]): F[Zone] = neutron.zones(zoneId)
  def targetProject[F[_]: Sync](implicit keystone: KeystoneClient[F]): F[Option[Project]] = targetProjectId match {
    case None => Sync[F].pure(Option.empty)
    case Some(id) => keystone.projects.get(id)
  }
}
