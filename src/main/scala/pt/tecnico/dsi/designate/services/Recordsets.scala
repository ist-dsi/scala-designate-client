package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.models.Recordset

final class Recordsets[F[_]: Sync: Client](baseUri: Uri, authToken: Header)
  extends CRUDService[F, Recordset](baseUri, "recordset", authToken)