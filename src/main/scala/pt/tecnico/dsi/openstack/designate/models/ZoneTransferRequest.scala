package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.{Applicative, derived}
import cats.derived.derived
import cats.derived.ShowPretty
import io.circe.derivation.ConfiguredCodec
import org.typelevel.cats.time.instances.localdatetime.given
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.designate.DesignateClient
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

object ZoneTransferRequest:
  case class Create(
    description: Option[String] = None,
    targetProjectId: Option[String] = None,
  ) derives ConfiguredCodec, ShowPretty
  
  case class Update(
    description: Option[String] = None,
    targetProjectId: Option[String] = None,
  ) derives ConfiguredCodec, ShowPretty:
    lazy val needsUpdate: Boolean =
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(description, targetProjectId).exists(_.isDefined)
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
) extends Identifiable derives ConfiguredCodec, ShowPretty:
  def project[F[_]](using keystone: KeystoneClient[F]): F[Project] = keystone.projects(projectId)
  def zone[F[_]](using neutron: DesignateClient[F]): F[Zone] = neutron.zones(zoneId)
  def targetProject[F[_]: Applicative](using keystone: KeystoneClient[F]): F[Option[Project]] = targetProjectId match
    case None => Applicative[F].pure(Option.empty)
    case Some(id) => keystone.projects.get(id)
