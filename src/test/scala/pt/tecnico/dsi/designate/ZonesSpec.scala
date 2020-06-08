package pt.tecnico.dsi.designate

import pt.tecnico.dsi.designate.models.{ZoneCreate, ZoneUpdate}

class ZonesSpec extends Utils {
  "The Zones service" should {

    val dummyZoneCreate = ZoneCreate(
      name = "xxx.org.",
      email = "joe@example.org"
    )

    val dummyZoneUpdate = ZoneUpdate(
      email = Some("afonso@example.org"),
      ttl = Some(600),
      description = Some("new description")
    )

    "list zones" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        isIdempotent <- client.zones.list().compile.toList.idempotently { lst =>
          assert (lst.exists(_.id == expected.id))
        }
      } yield isIdempotent
    }

    "create zones" in {
      for {
        client <- designateClient
        actual <- client.zones.create(dummyZoneCreate).idempotently { actual =>
          assert {
            actual.model.email == dummyZoneCreate.email &&
            actual.model.name == dummyZoneCreate.name &&
            actual.model.description == dummyZoneCreate.description
          }
        }
      } yield actual
    }

    "update zone" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        isIdempotent <- client.zones.update(expected.id, dummyZoneUpdate).idempotently { actual =>
          val equalEmail = dummyZoneUpdate.email.forall(_ == actual.model.email)
          val equalDesc = {
            for (expect <- dummyZoneUpdate.description; actual <- actual.model.description)
              yield expect == actual
          }.getOrElse(true)
          val equalTtl = {
            for (expect <- dummyZoneUpdate.ttl; actual <- actual.model.ttl)
              yield expect == actual
          }.getOrElse(true)
          assert (equalEmail && equalDesc && equalTtl)
        }
      } yield isIdempotent
    }

    "get zone" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        isIdempotent <- client.zones.get(expected.id).idempotently(_.model shouldEqual expected.model)
      } yield isIdempotent
    }

    "list groups" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        _ <- client.zones.listGroups(expected.id).compile.toList
      } yield assert { true }
    }

    "delete zone" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        isIdempotent <- client.zones.delete(expected.id).valueShouldIdempotentlyBe(())
      } yield isIdempotent
    }
  }
}
