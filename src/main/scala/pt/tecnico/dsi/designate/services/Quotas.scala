package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import org.http4s.{Header, Uri}
import org.http4s.client.Client
import pt.tecnico.dsi.designate.models.Quota

class Quotas[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends BaseService[F](authToken) {
  override val uri: Uri = baseUri / "quotas"
  import dsl._

  def get(projectId: String): F[Quota] =
    client.expect[Quota](GET(uri / projectId, authToken))

  def update(projectId: String, quota: Quota): F[Quota] =
    client.expect[Quota](PATCH(uri / projectId, authToken))

  def get: F[Quota] = client.expect[Quota](GET(uri, authToken))

  def reset(projectId: String): F[Unit] =
    client.expect[Unit](DELETE(uri / projectId, authToken))
}
