package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import cats.syntax.flatMap._
import fs2.Stream
import io.circe.Encoder
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.{Status, ZoneTransferRequest}

// This class does not extend CrudService because `create` receives an extra zoneId parameter.

final class ZoneTransferRequests[F[_]: Sync: Client](baseUri: Uri, authToken: Header, createUri: String => Uri) extends Service[F](authToken) {
  val uri: Uri = baseUri / "transfer_requests"

  def list(status: Status, extraHeaders: Header*): Stream[F, WithId[ZoneTransferRequest]] =
    list(Query.fromPairs("status" -> status.toString), extraHeaders:_*)

  def list(extraHeaders: Header*): Stream[F, WithId[ZoneTransferRequest]] = list(Query.empty, extraHeaders:_*)

  def list(query: Query, extraHeaders: Header*): Stream[F, WithId[ZoneTransferRequest]] =
    super.list[WithId[ZoneTransferRequest]]("transfer_requests", uri, query, extraHeaders:_*)

  def create(zoneId: String, value: ZoneTransferRequest.Create, extraHeaders: Header*): F[WithId[ZoneTransferRequest]] =
    super.postHandleConflict(wrappedAt = None, value, createUri(zoneId) / "transfer_requests", extraHeaders:_*) {
      list(Query.empty, extraHeaders:_*).filter(_.zoneId == zoneId).head.compile.lastOrError.flatMap { existing =>
        val updated = ZoneTransferRequest.Update(value.description, value.targetProjectId)
        update(existing.id, updated, extraHeaders:_*)
      }
    }

  def get(id: String, extraHeaders: Header*): F[WithId[ZoneTransferRequest]] =
    super.get(wrappedAt = None, uri / id, extraHeaders:_*)

  def update(id: String, value: ZoneTransferRequest.Update, extraHeaders: Header*)(implicit c: Encoder[ZoneTransferRequest.Update]): F[WithId[ZoneTransferRequest]] =
    super.patch(wrappedAt = None, value, uri / id, extraHeaders:_*)

  def delete(value: WithId[ZoneTransferRequest], extraHeaders: Header*): F[Unit] = delete(value.id, extraHeaders:_*)
  def delete(id: String, extraHeaders: Header*): F[Unit] = super.delete(uri / id, extraHeaders:_*)
}
