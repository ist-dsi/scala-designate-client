package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import fs2.Stream
import pt.tecnico.dsi.designate.models.{FloatingIP, Quota}

class FloatingIPs[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends BaseService[F](authToken) {

  override val uri: Uri = baseUri / "reverse" / "floatingips"
  import dsl._

  def list: Stream[F, FloatingIP] = genericList[FloatingIP]("floatingips", uri)

  def get(region: String, floatingIpId: String): F[FloatingIP] =
    client.expect[FloatingIP](GET(uri / (region + ":" + floatingIpId), authToken))

  def set(region: String, floatingIpId: String, floatingIp: FloatingIP): F[FloatingIP] =
    client.expect[FloatingIP](PATCH(floatingIp, uri / (region + ":" + floatingIpId), authToken))

  def unset(region: String, floatingIP: String): F[Unit] =
    client.expect[Unit](PATCH(Map("ptrdname" -> None), uri / (region + ":" + floatingIP)))

}

