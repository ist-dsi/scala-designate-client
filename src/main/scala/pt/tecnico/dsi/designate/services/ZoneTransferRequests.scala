package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import io.circe.{Codec, Decoder, Encoder}
import io.circe.syntax._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.designate.models.{Status, WithId, ZoneTransferRequest, ZoneTransferRequestCreate}

final class ZoneTransferRequests[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends BaseService[F](authToken) {

  // We cannot extend crud service directly because `create` does not belong to uri
  def crudService: AsymmetricCRUDService[F, ZoneTransferRequest] =
    new AsymmetricCRUDService[F, ZoneTransferRequest](baseUri, name = "transfer_request", authToken) {
      override type Update = ZoneTransferRequestCreate
  }

  def get(id: String): F[WithId[ZoneTransferRequest]] = crudService.get(id)

  def delete(id: String): F[Unit] = delete(uri / id)

  def list(query: Query): Stream[F, WithId[ZoneTransferRequest]] = crudService.list(query)
  def list(): Stream[F, WithId[ZoneTransferRequest]] = crudService.list()

  def list(status: Option[Status]): Stream[F, WithId[ZoneTransferRequest]] =
    crudService.list(Query.fromPairs("status" -> status.asJson.toString()))

  def update(id: String, value: ZoneTransferRequestCreate)(implicit c: Codec[ZoneTransferRequestCreate]): F[WithId[ZoneTransferRequest]]
    = crudService.update(id, value)

  override val uri: Uri = baseUri / "transfer_requests"
}
