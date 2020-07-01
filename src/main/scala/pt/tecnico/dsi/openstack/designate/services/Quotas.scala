package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.Quota

class Quotas[F[_]: Sync: Client](baseUri: Uri, authToken: Header) extends Service[F](authToken) {
  val uri: Uri = baseUri / "quotas"

  def get(projectId: String): F[Quota] = super.get(uri / projectId, wrappedAt = None)

  def get: F[Quota] = super.get(uri, wrappedAt = None)

  def update(projectId: String, quota: Quota): F[Quota] = super.patch(quota, uri / projectId, wrappedAt = None)

  def reset(projectId: String): F[Unit] = super.delete(uri / projectId)
}
