package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import cats.syntax.flatMap._
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.services.CrudService
import pt.tecnico.dsi.openstack.designate.models._
import pt.tecnico.dsi.openstack.keystone.models.Session

final class Zones[F[_]: Sync: Client](baseUri: Uri, session: Session)
  extends CrudService[F, Zone, Zone.Create, Zone.Update](baseUri, "zone", session.authToken, wrapped = false) { self =>

  def getByName(name: String, extraHeaders: Header*): F[Zone] =
    list(Query.fromPairs("name" -> name), extraHeaders:_*).compile.lastOrError

  override def create(value: Zone.Create, extraHeaders: Header*): F[Zone] = createHandleConflict(value) {
    getByName(value.name).flatMap { existing =>
      val updated = Zone.Update(Some(value.email), value.ttl, value.description)
      update(existing.id, updated)
    }
  }

  /**
   * Show the nameservers for a zone with `id`.
   *
   * @param id ID for the zone
   * @param extraHeaders extra headers to include in the request.
   */
  def nameservers(id: String, extraHeaders: Header*): Stream[F, Nameserver] =
    list[Nameserver]("nameservers", uri / id / "nameservers", Query.empty, extraHeaders:_*)

  /** @return the Recordsets service class capable of iteracting with the recordsets of the zone with `id`. */
  def recordsets(id: String): Recordsets[F] = new Recordsets(uri / id, session)

  object tasks {
    val uri: Uri = self.uri / "tasks"
    val transferRequests: ZoneTransferRequests[F] = new ZoneTransferRequests(uri, session, self.uri / _ / "tasks")
    val transferAccepts: ZoneTransferAccepts[F] = new ZoneTransferAccepts(uri, session)
  }
}
