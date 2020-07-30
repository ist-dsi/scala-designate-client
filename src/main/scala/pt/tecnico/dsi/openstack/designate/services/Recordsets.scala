package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import cats.syntax.flatMap._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.services.CrudService
import pt.tecnico.dsi.openstack.designate.models.Recordset

final class Recordsets[F[_]: Sync: Client](baseUri: Uri, authToken: Header)
  extends CrudService[F, Recordset, Recordset.Create, Recordset.Update](baseUri, "recordset", authToken, wrapped = false) {

  def getByName(name: String, extraHeaders: Header*): F[Recordset] =
    list(Query.fromPairs("name" -> name), extraHeaders:_*).compile.lastOrError

  override def update(id: String, value: Recordset.Update, extraHeaders: Header*): F[Recordset] =
    super.put(wrappedAt, value, uri / id, extraHeaders:_*)

  override def create(value: Recordset.Create, extraHeaders: Header*): F[Recordset] = createHandleConflict(value, extraHeaders:_*) {
    getByName(value.name, extraHeaders:_*).flatMap { existing =>
      val updated = Recordset.Update(value.records, value.ttl, value.description)
      update(existing.id, updated, extraHeaders:_*)
    }
  }
}