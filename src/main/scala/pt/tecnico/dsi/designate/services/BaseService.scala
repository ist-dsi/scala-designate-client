package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.{Chunk, Stream}
import io.circe.{Codec, Decoder, Encoder, HCursor}
import org.http4s.Status.{NotFound, Successful}
import org.http4s.circe.decodeUri
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.dsl.impl.Methods
import org.http4s.{EntityDecoder, Header, Query, Request, Uri}
import pt.tecnico.dsi.designate.models.WithId

abstract class BaseService[F[_]](protected val authToken: Header)(implicit protected val client: Client[F], protected val F: Sync[F]) {
  val uri: Uri
  protected val dsl = new Http4sClientDsl[F] with Methods
  import dsl._

  protected def unwrap[T: Codec](request: F[Request[F]], name: String): F[WithId[T]] =
    client.expect[Map[String, WithId[T]]](request).map(_.apply(name))

  protected def wrap[T: Encoder](value: T, name: String): Map[String, T] = Map(name -> value)

  protected def genericGet[T: Codec](name: String, id: String): F[WithId[T]] =
    unwrap(GET(uri / id, authToken), name)

  protected def genericUpdate[T: Codec, R: Codec](name: String, id: String, value: R): F[WithId[T]] =
    unwrap[T](PATCH(wrap(value, name), uri / id, authToken), name)

  protected def genericList[R: Decoder](baseKey: String, uri: Uri, query: Query = Query.empty): Stream[F, R] = {
    implicit val paginatedDecoder: Decoder[(Option[Uri], List[R])] = (c: HCursor) => for {
      links <- c.downField("links").get[Option[Uri]]("next")
      objectList <- c.downField(baseKey).as[List[R]]
    } yield (links, objectList)

    Stream.unfoldChunkEval[F, Option[Uri], R](Some(uri)) {
      case Some(uri) =>
        for {
          request <- GET(uri.copy(query = uri.query ++ query.pairs), authToken)
          (next, entries) <- client.expect[(Option[Uri], List[R])](request)
        } yield Some((Chunk.iterable(entries), next))
      case None => F.pure(None)
    }
  }

  protected def genericDelete(uri: Uri): F[Unit] =
    client.fetch(DELETE(uri, authToken)) {
      case Successful(_) | NotFound(_) => F.pure(())
      case response => F.raiseError(UnexpectedStatus(response.status))
    }

}