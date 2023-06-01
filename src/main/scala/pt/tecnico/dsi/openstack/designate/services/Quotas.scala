package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Concurrent
import io.circe.{Decoder, Encoder}
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.openstack.common.services.{PartialCrudService, UpdateOperations}
import pt.tecnico.dsi.openstack.designate.models.Quota
import pt.tecnico.dsi.openstack.keystone.models.Session

class Quotas[F[_]: Concurrent: Client](baseUri: Uri, session: Session)
  extends PartialCrudService[F](baseUri, "quota", session.authToken, wrapped = false)
    with UpdateOperations[F, Quota, Quota.Update]:
  override given modelDecoder: Decoder[Quota] = Quota.derived$ConfiguredCodec
  override given updateEncoder: Encoder[Quota.Update] = Quota.Update.derived$ConfiguredCodec
  
  /**
   * Get quotas for a project.
   * Designate always returns a Quota even if the project does not exist. That is why there is no method called `get`.
   * @param projectId The UUID of the project.
   */
  def apply(projectId: String, extraHeaders: Header.ToRaw*): F[Quota] =
    super.get(wrappedAt = None, uri / projectId, extraHeaders*)
  
  /**
   * Set the quotas of `projectId`
   *
   * @param projectId ID for the project.
   * @param quota the new quotas.
   */
  override def update(projectId: String, quota: Quota.Update, extraHeaders: Header.ToRaw*): F[Quota] =
    super.patch(wrappedAt = None, quota, uri / projectId, extraHeaders*)
  
  /**
   * Reset all quotas for `projectId` to default.
   * @param projectId ID for the project.
   */
  def reset(projectId: String, extraHeaders: Header.ToRaw*): F[Unit] = delete(uri / projectId, extraHeaders*)
