package pt.tecnico.dsi.openstack.designate

import cats.effect.IO
import pt.tecnico.dsi.openstack.designate.models.Zone

class ZonesSpec extends CrudSpec[Zone, Zone.Create, Zone.Update]("zone", _.zones) {
  override def stub: IO[Zone.Create] = IO.pure(Zone.Create("example.org.", "joe@example.org"))

  /*
  val withStubZone: IO[(DesignateClient[IO], String)] =
    for {
      designate <- client
      dummyZone <- designate.zones.create(Zone.Create("example.org.", "joe@example.org"))
    } yield (designate, dummyZone.id)

  "The Zones service" should {

    val dummyZoneCreate = Zone.Create(
      name = "example.org.",
      email = "joe@example.org"
    )

    val dummyZoneUpdate = Zone.Update(
      email = Some("afonso@example.org"),
      ttl = Some(600),
      description = Some("new description")
    )

    "list zones" in {
      for {
        client <- client
        expected <- client.zones.create(dummyZoneCreate)
        isIdempotent <- client.zones.list().compile.toList.idempotently { list =>
          list.exists(_.id == expected.id) shouldBe true
        }
      } yield isIdempotent
    }

    "create zones" in {
      for {
        client <- client
        actual <- client.zones.create(dummyZoneCreate).idempotently { actual =>
          actual.email shouldBe dummyZoneCreate.email
          actual.name shouldBe dummyZoneCreate.name
          actual.description shouldBe dummyZoneCreate.description
        }
      } yield actual
    }

    "update zone" in {
      for {
        client <- client
        expected <- client.zones.create(dummyZoneCreate)
        isIdempotent <- client.zones.update(expected.id, dummyZoneUpdate).idempotently { actual =>
          dummyZoneUpdate.email.forall(_ == actual.model.email) shouldBe true
          dummyZoneUpdate.description shouldBe actual.model.description
          dummyZoneUpdate.ttl shouldBe actual.model.ttl
        }
      } yield isIdempotent
    }

    "get zone" in {
      for {
        client <- client
        expected <- client.zones.create(dummyZoneCreate)
        actual <- client.zones.get(expected.id)
      } yield expected shouldBe actual
    }

    "list groups" in {
      for {
        client <- client
        expected <- client.zones.create(dummyZoneCreate)
        _ <- client.zones.listGroups(expected.id).compile.toList
      } yield assert { true }
    }

    "delete zone" in {
      for {
        client <- client
        expected <- client.zones.create(dummyZoneCreate)
        isIdempotent <- client.zones.delete(expected.id).idempotently(_ shouldBe ())
      } yield isIdempotent
    }
  }
  */

}
