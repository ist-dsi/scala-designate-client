package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import io.circe.Codec
import cats.syntax.flatMap._
import org.http4s.Status.{Conflict, Successful}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Header, Query, Response, Status, Uri}
import pt.tecnico.dsi.designate.models.{Nameserver, WithId, Zone, ZoneCreate, ZoneUpdate}

final class Zones[F[_]: Sync: Client](baseUri: Uri, authToken: Header)
  extends AsymmetricCRUDService[F, Zone](baseUri, "zone", authToken) {
  self =>

  import dsl._

  override type C = ZoneCreate
  override type U = ZoneUpdate

  def listGroups(id: String): Stream[F, Nameserver] =
    genericList[Nameserver]("nameservers", uri / id / "nameservers")

  override def get(id: String): F[WithId[Zone]] =
    client.expect[WithId[Zone]](GET(uri / id, authToken))

  def getByName(name: String): F[WithId[Zone]] = {
    // A domain name is globally unique across all domains.
    list(Query.fromPairs("name" -> name)).compile.lastOrError
  }

  def update(id: String, value: U): F[WithId[Zone]] =
    client.expect[WithId[Zone]](PATCH(value, uri / id, authToken))

  def recordsets(id: String): Recordsets[F] = new Recordsets(uri / id / "recordsets", authToken)

  override def createHandleConflict(value: C)(onConflict: Response[F] => F[WithId[Zone]])(implicit codec: Codec[C]): F[WithId[Zone]] =
    client.fetch(POST(value, uri, authToken)) {
      case Successful(response) => response.as[WithId[Zone]]
      case Conflict(response) => onConflict(response)
      case response => F.raiseError(UnexpectedStatus(response.status))
    }

  override def create(value: C)(implicit codec: Codec[C]): F[WithId[Zone]] = createHandleConflict(value) { _ =>
    getByName(value.name)
      .flatMap(existing => update(existing.id, ZoneUpdate(
        email = Some(value.email),
        ttl = value.ttl,
        description = value.description
      )))
  }

  object tasks {
    val uri: Uri = self.uri / "tasks"
    val transferRequests: ZoneTransferRequests[F] = new ZoneTransferRequests(uri, authToken)
    val transferAccepts: ZoneTransferAccepts[F] = new ZoneTransferAccepts(uri, authToken)
  }

}

