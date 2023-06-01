package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.derived.ShowPretty
import cats.Show
import cats.derived.derived
import io.circe.Codec
import io.circe.derivation.{Configuration, ConfiguredCodec, ConfiguredEnumCodec}
import org.typelevel.cats.time.instances.localdatetime.given
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

object Zone:
  object Type:
    given Codec[Type] = ConfiguredEnumCodec.derive(_.toUpperCase)
  enum Type derives Show:
    case Primary, Secondary

  case class Create(
    name: String,
    email: String,
    ttl: Option[Int] = None,
    description: Option[String] = None,
    `type`: Zone.Type = Zone.Type.Primary,
    masters: Option[List[String]] = None,
    attributes: Map[String, String] = Map.empty
  ) derives ConfiguredCodec, ShowPretty
  
  case class Update(
    email: Option[String] = None,
    ttl: Option[Int] = None,
    description: Option[String] = None
  ) derives ConfiguredCodec, ShowPretty:
    lazy val needsUpdate: Boolean =
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(email, ttl, description).exists(_.isDefined)
case class Zone(
  id: String,
  name: String,
  email: String,
  status: Status,
  action: Action,
  version: Int,
  createdAt: LocalDateTime,
  serial: Int,
  poolId: String,
  projectId: String,
  transferredAt: Option[LocalDateTime] = None,
  updatedAt: Option[LocalDateTime] = None,
  ttl: Int,
  description: Option[String] = None,
  `type`: Zone.Type = Zone.Type.Primary,
  masters: List[String] = List.empty,
  attributes: Map[String, String] = Map.empty,
  links: List[Link] = List.empty,
) extends Identifiable derives ConfiguredCodec, ShowPretty:
  def project[F[_]](using keystone: KeystoneClient[F]): F[Project] = keystone.projects(projectId)
