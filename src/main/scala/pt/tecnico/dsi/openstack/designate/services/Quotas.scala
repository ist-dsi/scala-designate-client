package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.Quota
import pt.tecnico.dsi.openstack.keystone.models.Session

class Quotas[F[_]: Sync: Client](baseUri: Uri, session: Session) extends Service[F](session.authToken) {
  val uri: Uri = baseUri / "quotas"

  /**
   * Get quotas of `projectId`.
   * @param projectId ID for the project.
   */
  def get(projectId: String, extraHeaders: Header*): F[Quota] =
    super.get(wrappedAt = None, uri / projectId, extraHeaders:_*)

  /** Get the quotas for the current project. */
  def get(extraHeaders: Header*): F[Quota] = super.get(wrappedAt = None, uri, extraHeaders:_*)

  /**
   * Set the quotas of `projectId`
   *
   * @param projectId ID for the project.
   * @param quota the new quotas.
   */
  def update(projectId: String, quota: Quota, extraHeaders: Header*): F[Quota] =
    super.patch(wrappedAt = None, quota, uri / projectId, extraHeaders:_*)

  /**
   * Reset all quotas for `projectId` to default.
   * @param projectId ID for the project.
   */
  def reset(projectId: String, extraHeaders: Header*): F[Unit] =
    super.delete(uri / projectId, extraHeaders:_*)
}
