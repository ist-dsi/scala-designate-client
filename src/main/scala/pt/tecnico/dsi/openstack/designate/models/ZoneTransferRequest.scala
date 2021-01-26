package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.{Applicative, derived}
import cats.derived.ShowPretty
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import io.chrisdavenport.cats.time.localdatetimeInstances
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.designate.DesignateClient
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

object ZoneTransferRequest {
  object Create {
    implicit val codec: Codec.AsObject[Create] = deriveCodec(renaming.snakeCase)
    implicit val show: ShowPretty[Create] = derived.semiauto.showPretty
  }
  case class Create(
    description: Option[String] = None,
    targetProjectId: Option[String] = None,
  )
  
  object Update {
    implicit val codec: Codec.AsObject[Update] = deriveCodec(renaming.snakeCase)
    implicit val show: ShowPretty[Update] = derived.semiauto.showPretty
  }
  case class Update(
    description: Option[String] = None,
    targetProjectId: Option[String] = None,
  ) {
    lazy val needsUpdate: Boolean = {
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(description, targetProjectId).exists(_.isDefined)
    }
  }
  
  implicit val codec: Codec.AsObject[ZoneTransferRequest] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[ZoneTransferRequest] = derived.semiauto.showPretty
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
  def project[F[_]](implicit keystone: KeystoneClient[F]): F[Project] = keystone.projects(projectId)
  def zone[F[_]](implicit neutron: DesignateClient[F]): F[Zone] = neutron.zones(zoneId)
  def targetProject[F[_]: Applicative](implicit keystone: KeystoneClient[F]): F[Option[Project]] = targetProjectId match {
    case None => Applicative[F].pure(Option.empty)
    case Some(id) => keystone.projects.get(id)
  }
}
