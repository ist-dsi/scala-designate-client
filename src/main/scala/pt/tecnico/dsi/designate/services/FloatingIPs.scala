package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.models.{FloatingIP, WithId}

class FloatingIPs[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends BaseService[F](authToken) {

  override val uri: Uri = baseUri / "reverse" / "floatingips"
  import dsl._

  def list: Stream[F, WithId[FloatingIP]] = genericList[WithId[FloatingIP]]("floatingips", uri)

  def get(region: String, floatingIpId: String): F[WithId[FloatingIP]] =
    client.expect(GET(uri / s"$region:$floatingIpId", authToken))

  def set(region: String, floatingIpId: String, floatingIp: FloatingIP): F[WithId[FloatingIP]] =
    client.expect(PATCH(floatingIp, uri / (region + ":" + floatingIpId), authToken))

  def unset(region: String, floatingIP: String): F[Unit] =
    client.expect(PATCH(Map("ptrdname" -> None), uri / (region + ":" + floatingIP)))

}

