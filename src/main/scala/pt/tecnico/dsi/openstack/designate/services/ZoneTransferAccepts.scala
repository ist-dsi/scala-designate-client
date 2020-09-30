package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import cats.syntax.flatMap._
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.{Status, ZoneTransferAccept}
import pt.tecnico.dsi.openstack.keystone.models.Session

// This class does not extend CrudService because it is not possible to update and delete a ZoneTransferAccept.

final class ZoneTransferAccepts[F[_]: Sync](baseUri: Uri, session: Session)(implicit client: Client[F]) extends Service[F](session.authToken) {
  val uri: Uri = baseUri / "transfer_accepts"

  /**
   * Accepts an offer of a zone ownership transfer.
   *
   * @param key Key that is used as part of the zone transfer accept process.
   *            This is only shown to the creator of the zone transfer request, and must be communicated out of band.
   * @param zoneTransferRequestId ID of the zone transfer request
   * @param extraHeaders extra headers to pass to the request.
   */
  def create(key: String, zoneTransferRequestId: String, extraHeaders: Header*): F[ZoneTransferAccept] = {
    // Once a zone transfer is accepted it is not possible to accept it again. This means this method is not idempotent.
    // However we can make it idempotent: if an accept for `zoneTransferRequestId` already exists we return it, being careful
    // to ensure the key is set to the correct value. Otherwise we create the accept.
    list(Status.Complete, extraHeaders:_*).filter(_.zoneTransferRequestId == zoneTransferRequestId).compile.last.flatMap { optionalAccept =>
      optionalAccept.map { accept =>
        F.pure(accept.copy(key = Some(key)))
      }.getOrElse {
        super.post(wrappedAt = None, Map("key" -> key, "zone_transfer_request_id" -> zoneTransferRequestId), uri, extraHeaders:_*)
      }
    }
  }

  def list(status: Status, extraHeaders: Header*): Stream[F, ZoneTransferAccept] =
    list(Query.fromPairs("status" -> status.toString), extraHeaders:_*)

  def list(extraHeaders: Header*): Stream[F, ZoneTransferAccept] = list(Query.empty, extraHeaders:_*)

  def list(query: Query, extraHeaders: Header*): Stream[F, ZoneTransferAccept] =
    super.list[ZoneTransferAccept]("transfer_accepts", uri, query, extraHeaders:_*)

  def get(zoneTransferAcceptId: String, extraHeaders: Header*): F[Option[ZoneTransferAccept]] =
    super.getOption(wrappedAt = None, uri / zoneTransferAcceptId, extraHeaders:_*)
  def apply(zoneTransferAcceptId: String, extraHeaders: Header*): F[ZoneTransferAccept] =
    super.get(wrappedAt = None, uri / zoneTransferAcceptId, extraHeaders:_*)

}
