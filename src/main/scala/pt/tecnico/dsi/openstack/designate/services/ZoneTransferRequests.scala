package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import fs2.Stream
import io.circe.Encoder
import io.circe.syntax._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.{Status, ZoneTransferRequest, ZoneTransferRequestUpdate}

final class ZoneTransferRequests[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends Service[F](authToken) {

  val uri: Uri = baseUri / "transfer_requests"

  def list(query: Query = Query.empty): Stream[F, WithId[ZoneTransferRequest]] = super.list[WithId[ZoneTransferRequest]]("transfer_requests", uri, query)

  def get(id: String): F[WithId[ZoneTransferRequest]] = super.get(uri / id, wrappedAt = None)

  def delete(value: WithId[ZoneTransferRequest]): F[Unit] = delete(value.id)
  def delete(id: String): F[Unit] = super.delete(uri / id)

  def list(status: Option[Status]): Stream[F, WithId[ZoneTransferRequest]] =
    list(Query.fromPairs("status" -> status.asJson.toString()))

  def update(id: String, value: ZoneTransferRequestUpdate)(implicit c: Encoder[ZoneTransferRequestUpdate]): F[WithId[ZoneTransferRequest]]
    = super.patch(value, uri / id, wrappedAt = None)

}
