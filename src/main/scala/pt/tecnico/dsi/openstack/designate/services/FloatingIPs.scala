package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.FloatingIP

class FloatingIPs[F[_]: Sync: Client](baseUri: Uri, authToken: Header) extends Service[F](authToken) {
  val uri: Uri = baseUri / "reverse" / "floatingips"

  def list: Stream[F, WithId[FloatingIP]] = super.list[WithId[FloatingIP]]("floatingips", uri, Query.empty)

  def get(region: String, floatingIpId: String): F[WithId[FloatingIP]] =
    super.get(uri / s"$region:$floatingIpId", wrappedAt = None)

  def set(region: String, floatingIpId: String, floatingIp: FloatingIP): F[WithId[FloatingIP]] =
    super.patch(floatingIp, uri / s"$region:$floatingIpId", wrappedAt = None)

  def unset(region: String, floatingIP: String): F[Unit] =
    super.patch(Map("ptrdname" -> None), uri / s"$region:$floatingIP", wrappedAt = None)
}

