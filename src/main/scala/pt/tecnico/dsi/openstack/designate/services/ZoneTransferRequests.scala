package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Concurrent
import cats.syntax.flatMap.*
import io.circe.{Decoder, Encoder}
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import org.log4s.getLogger
import pt.tecnico.dsi.openstack.common.services.*
import pt.tecnico.dsi.openstack.designate.models.ZoneTransferRequest
import pt.tecnico.dsi.openstack.keystone.models.Session

// This class does not extend CrudService because `create` receives an extra zoneId parameter.

final class ZoneTransferRequests[F[_]: Concurrent: Client](baseUri: Uri, session: Session, createUri: String => Uri)
  extends PartialCrudService[F](baseUri, "transfer_request", session.authToken, wrapped = false)
    with UpdateOperations[F, ZoneTransferRequest, ZoneTransferRequest.Update]
    with ListOperations[F, ZoneTransferRequest]
    with ReadOperations[F, ZoneTransferRequest]
    with DeleteOperations[F, ZoneTransferRequest]:
  
  override given modelDecoder: Decoder[ZoneTransferRequest] = ZoneTransferRequest.derived$ConfiguredCodec
  override given updateEncoder: Encoder[ZoneTransferRequest.Update] = ZoneTransferRequest.Update.derived$ConfiguredCodec
  
  def create(zoneId: String, value: ZoneTransferRequest.Create, extraHeaders: Header.ToRaw*): F[ZoneTransferRequest] =
    super.post(wrappedAt, value, createUri(zoneId) / pluralName, extraHeaders*)
  
  /**
   * Default implementation to resolves the conflict that arises when implementing the createOrUpdate.
   * In other words implements the idempotency logic of the create.
   * @param existing the existing model
   * @param create the create settings
   * @param keepExistingElements whether to keep existing settings. See `createOrUpdate` for a more detailed explanation.
   * @param extraHeaders extra headers to be used. The `authToken` header is always added.
   * @return the existing model if no modifications are required, otherwise the updated model.
   */
  def defaultResolveConflict(existing: ZoneTransferRequest, create: ZoneTransferRequest.Create, keepExistingElements: Boolean,
    extraHeaders: Seq[Header.ToRaw])
  : F[ZoneTransferRequest] =
    val updated = ZoneTransferRequest.Update(
      if create.description != existing.description then create.description else None,
      if create.targetProjectId != existing.targetProjectId then create.targetProjectId else None,
    )
    if updated.needsUpdate then update(existing.id, updated, extraHeaders*)
    else Concurrent[F].pure(existing)
  /**
   * An idempotent create. If the model that is to be created already exists then it will be updated, or simply returned if no modifications
   * are necessary. The definition on what is considered already existing is left to the implementation as it is specific to the `Model`
   * in question.
   *
   * This function simply calls the overloaded version passing in `keepExistingElements` set to `true`.
   *
   * @param create the create settings.
   * @param extraHeaders extra headers to be used. The `authToken` header is always added.
   * @return the created model, or if it already exists the existing or updated model.
   */
  def createOrUpdate(zoneId: String, create: ZoneTransferRequest.Create, extraHeaders: Header.ToRaw*): F[ZoneTransferRequest] =
    createOrUpdate(zoneId, create, keepExistingElements = true, extraHeaders*)
  /**
   * An idempotent create. If the model that is to be created already exists then it will be updated, or simply returned if no modifications
   * are necessary. The definition on what is considered already existing is left to the implementation as it is specific to the `Model`
   * in question. [[defaultResolveConflict]] will be invoked to resolve the conflict that will arise when the create is being performed
   * on a model that already exists.
   *
   * @param create the create settings.
   * @param extraHeaders extra headers to be used. The `authToken` header is always added.
   * @param keepExistingElements the create operation can be interpreted with two possible meanings:
   *
   *   1. Create a `Model` with the settings in `Create`. Extra settings that may already exist (when the model is being updated)
   *      will be preserved as much as possible.
   *   1. Create a `Model` with <b>exactly</b> the settings in `Create`. Extra settings that may already exist are removed.
   *      This is done in a best effort approach, since its not achievable in the general case, as some settings are not updatable
   *      after creating the Model (the create and the update are asymmetric).
   *
   *   Setting `keepExistingElements` to `true` will follow the logic in point 1.
   *   Setting it to `false` will follow point 2, thus making the update more stringent.
   * @return the created model, or if it already exists the existing or updated model.
   */
  def createOrUpdate(zoneId: String, create: ZoneTransferRequest.Create, keepExistingElements: Boolean, extraHeaders: Header.ToRaw*): F[ZoneTransferRequest] =
    createOrUpdate(zoneId, create, keepExistingElements, extraHeaders)()
  
  /**
   * An idempotent create. If the model that is to be created already exists then it will be updated, or simply returned if no modifications
   * are necessary. The definition on what is considered already existing is left to the implementation as it is specific to the `Model`
   * in question.
   * @param create the create settings.
   * @param extraHeaders extra headers to be used. The `authToken` header is always added.
   * @param keepExistingElements the create operation can be interpreted with two possible meanings:
   *
   *   1. Create a `Model` with the settings in `Create`. Extra settings that may already exist (when the model is being updated)
   *      will be preserved as much as possible.
   *   1. Create a `Model` with <b>exactly</b> the settings in `Create`. Extra settings that may already exist are removed.
   *      This is done in a best effort approach, since its not achievable in the general case, as some settings are not updatable
   *      after creating the Model (the create and the update are asymmetric).
   *
   *   Setting `keepExistingElements` to `true` will follow the logic in point 1.
   *   Setting it to `false` will follow the logic in point 2, thus making the update more stringent.
   *   In most cases removing extra settings will break things, so this flag is set to `true` by default.
   * @param resolveConflict the function used to resolve the conflict that will arise when the create is being performed
   *                        on a model that already exists. By default it just invokes [[defaultResolveConflict]].
   * @return the created model, or if it already exists the existing or updated model.
   */
  def createOrUpdate(zoneId: String, create: ZoneTransferRequest.Create, keepExistingElements: Boolean = true, extraHeaders: Seq[Header.ToRaw] = Seq.empty)
    (resolveConflict: (ZoneTransferRequest, ZoneTransferRequest.Create) => F[ZoneTransferRequest] = defaultResolveConflict(_, _, keepExistingElements,
      extraHeaders)): F[ZoneTransferRequest] =
    super.postHandleConflict(wrappedAt, create, createUri(zoneId) / "transfer_requests", extraHeaders):
      stream(Query.empty, extraHeaders*).filter(_.zoneId == zoneId).head.compile.lastOrError.flatMap { existing =>
        getLogger.info(s"createOrUpdate: found unique zone transfer_request (id: ${existing.id}) with the correct zoneId.")
        resolveConflict(existing, create)
      }
  
  override def update(id: String, value: ZoneTransferRequest.Update, extraHeaders: Header.ToRaw*): F[ZoneTransferRequest] =
    super.patch(wrappedAt, value, uri / id, extraHeaders*)
