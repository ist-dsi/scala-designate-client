package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import fs2.Stream
import pt.tecnico.dsi.designate.models.{Status, WithId, ZoneTransferRequest}
import io.circe.syntax._

final class ZoneTransferRequests[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends CRUDService[F, ZoneTransferRequest](baseUri / "transfer_requests", name = "transfer_request", authToken) {

  def list(status: Status): Stream[F, WithId[ZoneTransferRequest]] =
    list(Query.fromPairs(
      "status" -> status.asJson.toString(),
    ))
}
