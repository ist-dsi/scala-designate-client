package pt.tecnico.dsi.designate.models

import cats.effect.Sync
import cats.syntax.flatMap._
import fs2.Stream
import io.circe.Decoder.Result
import io.circe.syntax._
import io.circe.{Codec, Decoder, Encoder, HCursor, Json}
import org.http4s.Uri
import org.http4s.circe.decodeUri
import pt.tecnico.dsi.designate.DesignateClient

object WithId {

  implicit def decoder[T: Decoder]: Decoder[WithId[T]] = (cursor: HCursor) => for {
    id <- cursor.get[String]("id")
    link <- cursor.downField("links").get[Option[Uri]]("self")
    model <- cursor.as[T]
  } yield WithId(id, model, link)

  implicit def encoder[T: Encoder]: Encoder[WithId[T]] = (a: WithId[T]) => a.model.asJson.mapObject(_.add("id", a.id.asJson))
  implicit def codec[T: Codec]: Codec[WithId[T]] = Codec.from(decoder, encoder)

  import scala.language.implicitConversions
  implicit def toModel[T](modelWithId: WithId[T]): T = modelWithId.model
}
// All Openstack IDs are strings, 99% are random UUIDs
case class WithId[T](id: String, model: T, link: Option[Uri])

trait IdFetcher[T <: IdFetcher[T]] {
  def getWithId[F[_]: Sync](implicit client: DesignateClient[F]): F[WithId[T]]

  def withId[F[_]: Sync: DesignateClient, R](f: WithId[T] => F[R]): F[R] = getWithId.flatMap(f)
  def withId[F[_]: Sync: DesignateClient, R](f: WithId[T] => Stream[F, R]): Stream[F, R] = Stream.eval(getWithId).flatMap(f)
}