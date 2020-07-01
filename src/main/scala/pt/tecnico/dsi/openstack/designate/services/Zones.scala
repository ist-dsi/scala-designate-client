package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import cats.syntax.flatMap._
import fs2.Stream
import org.http4s.Method.POST
import org.http4s.Status.{Conflict, Successful}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Header, Query, Response, Uri}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.CrudService
import pt.tecnico.dsi.openstack.designate.models._

final class Zones[F[_]: Sync: Client](baseUri: Uri, authToken: Header)
  extends CrudService[F, Zone, Zone.Create, Zone.Update](baseUri, "zone", authToken) { self =>

  import dsl._

  def listGroups(id: String): Stream[F, Nameserver] =
    list[Nameserver]("nameservers", uri / id / "nameservers", Query.empty)

  def getByName(name: String): F[WithId[Zone]] = {
    // A domain name is globally unique across all domains.
    list(Query.fromPairs("name" -> name)).compile.lastOrError
  }

  def recordsets(id: String): Recordsets[F] = new Recordsets(uri / id, authToken)

  override def createHandleConflict(value: Zone.Create)(onConflict: Response[F] => F[WithId[Zone]]): F[WithId[Zone]] =
    POST(value, uri, authToken).flatMap(client.run(_).use {
      case Successful(response) => response.as[WithId[Zone]]
      case Conflict(response) => onConflict(response)
      case response => F.raiseError(UnexpectedStatus(response.status))
    })

  override def create(value: Zone.Create): F[WithId[Zone]] = createHandleConflict(value) { _ =>
    getByName(value.name)
      .flatMap(existing => update(existing.id, Zone.Update(
        email = Some(value.email),
        ttl = value.ttl,
        description = value.description
      )))
  }

  object tasks {
    val uri: Uri = self.uri / "tasks"
    val transferRequests: ZoneTransferRequests[F] = new ZoneTransferRequests(uri, authToken, self.uri / _ / "tasks")
    val transferAccepts: ZoneTransferAccepts[F] = new ZoneTransferAccepts(uri, authToken)
  }

}

