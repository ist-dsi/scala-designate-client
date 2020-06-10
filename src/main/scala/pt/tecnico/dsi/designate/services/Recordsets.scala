package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import io.circe.Codec
import cats.syntax.flatMap._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.designate.models.{Recordset, RecordsetCreate, RecordsetUpdate, WithId, Zone, ZoneUpdate}

final class Recordsets[F[_]: Sync: Client](baseUri: Uri, authToken: Header)
  extends AsymmetricCRUDService[F, Recordset](baseUri, "recordset", authToken) {
  override type Create = RecordsetCreate
  override type Update = RecordsetUpdate

  import dsl._

  def getByName(name: String): F[WithId[Recordset]] =
    list(Query.fromPairs("name" -> name)).compile.lastOrError

  override def update(id: String, value: RecordsetUpdate)(implicit d: Codec[Update]): F[WithId[Recordset]] =
    unwrap[Recordset](PUT(value, uri / id, authToken))

  override def create(value: Create)(implicit codec: Codec[Create]): F[WithId[Recordset]] = createHandleConflict(value) { _ =>
    getByName(value.name)
      .flatMap(existing => update(existing.id, RecordsetUpdate(
        records = value.records,
        ttl = value.ttl,
        description = value.description
      )))
  }

}