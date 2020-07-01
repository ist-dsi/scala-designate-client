package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import io.circe.Encoder
import io.circe.syntax._
import cats.syntax.flatMap._
import fs2.Stream
import org.http4s.Method.POST
import org.http4s.Status.{Conflict, Successful}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Header, Query, Response, Uri}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.{Status, ZoneTransferRequest, ZoneTransferRequestCreate, ZoneTransferRequestUpdate}

final class ZoneTransferRequests[F[_]: Sync](baseUri: Uri, authToken: Header, createUri: String => Uri)(implicit client: Client[F])
  extends Service[F](authToken) {

  import dsl._
  val uri: Uri = baseUri / "transfer_requests"

  def list(query: Query = Query.empty): Stream[F, WithId[ZoneTransferRequest]] = super.list[WithId[ZoneTransferRequest]]("transfer_requests", uri, query)

  def get(id: String): F[WithId[ZoneTransferRequest]] = super.get(uri / id, wrappedAt = None)

  def delete(value: WithId[ZoneTransferRequest]): F[Unit] = delete(value.id)
  def delete(id: String): F[Unit] = super.delete(uri / id)

  def list(status: Option[Status]): Stream[F, WithId[ZoneTransferRequest]] =
    list(Query.fromPairs("status" -> status.asJson.toString()))

  def update(id: String, value: ZoneTransferRequestUpdate)(implicit c: Encoder[ZoneTransferRequestUpdate]): F[WithId[ZoneTransferRequest]]
    = super.patch(value, uri / id, wrappedAt = None)

  private def createHandleConflict(uri: Uri, value: ZoneTransferRequestCreate)(onConflict: Response[F] => F[WithId[ZoneTransferRequest]]): F[WithId[ZoneTransferRequest]] =
    POST(value, uri, authToken).flatMap(client.run(_).use {
      case Successful(response) => response.as[WithId[ZoneTransferRequest]]
      case Conflict(response) => onConflict(response)
      case response => F.raiseError(UnexpectedStatus(response.status))
    })

  def create(zoneId: String, value: ZoneTransferRequestCreate): F[WithId[ZoneTransferRequest]] =
      createHandleConflict(createUri(zoneId), value) { _ =>
        list().filter(h => h.zoneId == zoneId)
          .head.compile.lastOrError
          .flatMap(existing => update(existing.id, ZoneTransferRequestUpdate(
            description = value.description,
            targetProjectId = value.targetProjectId
          )))
      }
}
