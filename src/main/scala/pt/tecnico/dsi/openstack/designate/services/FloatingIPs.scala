package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Concurrent
import cats.syntax.flatMap._
import io.circe.{Decoder, Encoder}
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.openstack.common.services.{ListOperations, PartialCrudService}
import pt.tecnico.dsi.openstack.designate.models.FloatingIP
import pt.tecnico.dsi.openstack.keystone.models.Session

class FloatingIPs[F[_]: Concurrent: Client](baseUri: Uri, session: Session) extends PartialCrudService[F](baseUri, "floatingip", session.authToken)
  with ListOperations[F, FloatingIP] {
  override val uri: Uri = baseUri / "reverse" / pluralName
  
  implicit val modelDecoder: Decoder[FloatingIP] = FloatingIP.codec

  def get(region: String, floatingIpId: String, extraHeaders: Header*): F[Option[FloatingIP]] =
    super.getOption(wrappedAt = None, uri / s"$region:$floatingIpId", extraHeaders:_*)
  def apply(region: String, floatingIpId: String, extraHeaders: Header*): F[FloatingIP] =
    get(region, floatingIpId, extraHeaders:_*).flatMap {
      case Some(floatingIp) => F.pure(floatingIp)
      case None => F.raiseError(new NoSuchElementException(s"""Could not find floatingip in region "$region" with id "$floatingIpId"."""))
    }

  def set(region: String, floatingIpId: String, floatingIp: FloatingIP.Create, extraHeaders: Header*)
         (implicit encoder: Encoder[FloatingIP.Create]): F[FloatingIP] =
    super.patch(wrappedAt = None, floatingIp, uri / s"$region:$floatingIpId", extraHeaders:_*)

  def unset(region: String, floatingIP: String, extraHeaders: Header*): F[Unit] =
    super.patch(wrappedAt = None, Map("ptrdname" -> None), uri / s"$region:$floatingIP", extraHeaders:_*)
}
