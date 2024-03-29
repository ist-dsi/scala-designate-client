package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Concurrent
import cats.syntax.flatMap.*
import fs2.Stream
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import org.log4s.getLogger
import pt.tecnico.dsi.openstack.common.services.{PartialCrudService, ListOperations, ReadOperations}
import pt.tecnico.dsi.openstack.designate.models.{Status, ZoneTransferAccept}
import pt.tecnico.dsi.openstack.keystone.models.Session

final class ZoneTransferAccepts[F[_]: Concurrent: Client](baseUri: Uri, session: Session)
  extends PartialCrudService[F](baseUri, "transfer_accept", session.authToken, wrapped = false)
    with ListOperations[F, ZoneTransferAccept]
    with ReadOperations[F, ZoneTransferAccept]:
  
  override given modelDecoder: Decoder[ZoneTransferAccept] = ZoneTransferAccept.derived$ConfiguredCodec
  
  def stream(status: Status, extraHeaders: Header.ToRaw*): Stream[F, ZoneTransferAccept] =
    stream(Query.fromPairs("status" -> status.toString.toLowerCase), extraHeaders*)
  
  def list(status: Status, extraHeaders: Header.ToRaw*): F[List[ZoneTransferAccept]] =
    list(Query.fromPairs("status" -> status.toString), extraHeaders*)
  
  def create(key: String, zoneTransferRequestId: String, extraHeaders: Header.ToRaw*): F[ZoneTransferAccept] =
    super.post(wrappedAt, Map("key" -> key, "zone_transfer_request_id" -> zoneTransferRequestId), uri, extraHeaders*)
  
  /**
   * A sort of idempotent create. If a Conflict is received and the zone transfer accept already exists return it appending the key first.
   * Otherwise `F` will have an error.
   *
   * Its impossible to implement an idempotent create because the API does not expose an update endpoint.
   *
   * @param key key of the zone transfer request
   * @param zoneTransferRequestId id of the zone transfer request
   * @param extraHeaders extra headers to be used. The `authToken` header is always added.
   */
  def createWithDeduplication(key: String, zoneTransferRequestId: String, extraHeaders: Header.ToRaw*): F[ZoneTransferAccept] =
    // Once a zone transfer is accepted it is not possible to accept it again. This means this method is not idempotent.
    // However we can make it idempotent: if an accept for `zoneTransferRequestId` already exists we return it, being careful
    // to ensure the key is set to the correct value. Otherwise we create the accept.
    stream(Status.Complete, extraHeaders*).filter(_.zoneTransferRequestId == zoneTransferRequestId).compile.last.flatMap:
      case Some(accept) =>
        getLogger.info(s"createOrUpdate: found unique zone transfer_accepts (id: ${accept.id}) with the correct status and zoneTransferRequestId.")
        F.pure(accept.copy(key = Some(key)))
      case None =>
        create(key, zoneTransferRequestId, extraHeaders*)
