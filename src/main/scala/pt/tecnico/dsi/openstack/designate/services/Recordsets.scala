package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import cats.syntax.flatMap._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.CrudService
import pt.tecnico.dsi.openstack.designate.models.Recordset

final class Recordsets[F[_]: Sync: Client](baseUri: Uri, authToken: Header)
  extends CrudService[F, Recordset, Recordset.Create, Recordset.Update](baseUri, "recordset", authToken) {

  def getByName(name: String): F[WithId[Recordset]] = list(Query.fromPairs("name" -> name)).compile.lastOrError

  override def update(id: String, value: Recordset.Update): F[WithId[Recordset]] = super.put(value, uri / id, wrappedAt = None)

  override def create(value: Recordset.Create): F[WithId[Recordset]] = createHandleConflict(value) { _ =>
    getByName(value.name).flatMap { existing =>
      update(existing.id, Recordset.Update(value.ttl, value.description, value.records))
    }
  }
}