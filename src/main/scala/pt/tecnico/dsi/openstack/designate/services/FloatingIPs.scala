package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import fs2.Stream
import io.circe.Encoder
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.FloatingIP

class FloatingIPs[F[_]: Sync: Client](baseUri: Uri, authToken: Header) extends Service[F](authToken) {
  val uri: Uri = baseUri / "reverse" / "floatingips"

  def list(extraHeaders: Header*): Stream[F, WithId[FloatingIP]] =
    super.list[WithId[FloatingIP]]("floatingips", uri, Query.empty, extraHeaders:_*)

  def get(region: String, floatingIpId: String, extraHeaders: Header*): F[WithId[FloatingIP]] =
    super.get(wrappedAt = None, uri / s"$region:$floatingIpId", extraHeaders:_*)

  def set(region: String, floatingIpId: String, floatingIp: FloatingIP.Create, extraHeaders: Header*)
         (implicit encoder: Encoder[FloatingIP.Create]): F[WithId[FloatingIP]] =
    super.patch(wrappedAt = None, floatingIp, uri / s"$region:$floatingIpId", extraHeaders:_*)

  def unset(region: String, floatingIP: String, extraHeaders: Header*): F[Unit] =
    super.patch(wrappedAt = None, Map("ptrdname" -> None), uri / s"$region:$floatingIP", extraHeaders:_*)
}

