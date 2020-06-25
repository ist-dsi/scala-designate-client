package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import io.circe.Codec
import io.circe.syntax._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.designate.models.{Status, WithId, ZoneTransferRequest, ZoneTransferRequestUpdate}

final class ZoneTransferRequests[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends BaseService[F](authToken) {

  override val uri: Uri = baseUri / "transfer_requests"

  def list(query: Query = Query.empty): Stream[F, WithId[ZoneTransferRequest]] = super.list[WithId[ZoneTransferRequest]]("transfer_requests", uri, query)

  def get(id: String): F[WithId[ZoneTransferRequest]] = super.get(uri / id)

  def delete(value: WithId[ZoneTransferRequest]): F[Unit] = delete(value.id)
  def delete(id: String): F[Unit] = super.delete(uri / id)

  def list(status: Option[Status]): Stream[F, WithId[ZoneTransferRequest]] =
    list(Query.fromPairs("status" -> status.asJson.toString()))

  def update(id: String, value: ZoneTransferRequestUpdate)(implicit c: Codec[ZoneTransferRequestUpdate]): F[WithId[ZoneTransferRequest]]
    = super.update(uri / id, value)

}
