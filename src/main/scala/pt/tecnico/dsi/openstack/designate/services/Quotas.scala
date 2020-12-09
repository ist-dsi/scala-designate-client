package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.openstack.common.services.{BaseCrudService, UpdateOperations}
import pt.tecnico.dsi.openstack.designate.models.Quota
import pt.tecnico.dsi.openstack.keystone.models.Session

class Quotas[F[_]: Sync: Client](baseUri: Uri, session: Session)
  extends BaseCrudService[F](baseUri, "quota", session.authToken, wrapped = false)
    with UpdateOperations[F, Quota, Quota.Update] {
  override implicit val modelDecoder: Decoder[Quota] = Quota.codec
  override implicit val updateEncoder: Encoder[Quota.Update] = Quota.Update.codec
  
  /**
   * Get quotas for a project.
   * Designate always returns a Quota even if the project does not exist. That is why there is no method called `get`.
   * @param projectId The UUID of the project.
   */
  def apply(projectId: String, extraHeaders: Header*): F[Quota] =
    super.get(wrappedAt = None, uri / projectId, extraHeaders:_*)
  
  /**
   * Set the quotas of `projectId`
   *
   * @param projectId ID for the project.
   * @param quota the new quotas.
   */
  override def update(projectId: String, quota: Quota.Update, extraHeaders: Header*): F[Quota] =
    super.patch(wrappedAt = None, quota, uri / projectId, extraHeaders:_*)
  
  /**
   * Reset all quotas for `projectId` to default.
   * @param projectId ID for the project.
   */
  def reset(projectId: String, extraHeaders: Header*): F[Unit] = delete(uri / projectId, extraHeaders:_*)
}
