package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import io.circe.Codec
import org.http4s.client.Client
import org.http4s.{Header, Uri}

abstract class CRUDService[F[_]: Sync: Client, T: Codec](baseUri: Uri, name: String, authToken: Header)
  extends AsymmetricCRUDService[F, T](baseUri, name, authToken) {

  type U = T
  type C = T
}