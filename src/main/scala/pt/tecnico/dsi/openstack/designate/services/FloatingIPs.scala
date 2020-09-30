package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import fs2.Stream
import io.circe.Encoder
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.FloatingIP
import pt.tecnico.dsi.openstack.keystone.models.Session

class FloatingIPs[F[_]: Sync: Client](baseUri: Uri, session: Session) extends Service[F](session.authToken) {
  val uri: Uri = baseUri / "reverse" / "floatingips"

  def list(extraHeaders: Header*): Stream[F, FloatingIP] =
    super.list[FloatingIP]("floatingips", uri, Query.empty, extraHeaders:_*)

  def get(region: String, floatingIpId: String, extraHeaders: Header*): F[Option[FloatingIP]] =
    super.getOption(wrappedAt = None, uri / s"$region:$floatingIpId", extraHeaders:_*)
  def apply(region: String, floatingIpId: String, extraHeaders: Header*): F[FloatingIP] =
    super.get(wrappedAt = None, uri / s"$region:$floatingIpId", extraHeaders:_*)

  def set(region: String, floatingIpId: String, floatingIp: FloatingIP.Create, extraHeaders: Header*)
         (implicit encoder: Encoder[FloatingIP.Create]): F[FloatingIP] =
    super.patch(wrappedAt = None, floatingIp, uri / s"$region:$floatingIpId", extraHeaders:_*)

  def unset(region: String, floatingIP: String, extraHeaders: Header*): F[Unit] =
    super.patch(wrappedAt = None, Map("ptrdname" -> None), uri / s"$region:$floatingIP", extraHeaders:_*)
}

