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

  override type Create = ZoneCreate
  override type Update = ZoneUpdate

  def listGroups(id: String): Stream[F, Nameserver] =
    list[Nameserver]("nameservers", uri / id / "nameservers")

  def getByName(name: String): F[WithId[Zone]] = {
    // A domain name is globally unique across all domains.
    list(Query.fromPairs("name" -> name)).compile.lastOrError
  }

  def recordsets(id: String): Recordsets[F] = new Recordsets(uri / id, authToken)

  override def createHandleConflict(value: Create)(onConflict: Response[F] => F[WithId[Zone]])(implicit codec: Codec[Create]): F[WithId[Zone]] =
    client.fetch(POST(value, uri, authToken)) {
      case Successful(response) => response.as[WithId[Zone]]
      case Conflict(response) => onConflict(response)
      case response => F.raiseError(UnexpectedStatus(response.status))
    }

  override def create(value: Create)(implicit codec: Codec[Create]): F[WithId[Zone]] = createHandleConflict(value) { _ =>
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

