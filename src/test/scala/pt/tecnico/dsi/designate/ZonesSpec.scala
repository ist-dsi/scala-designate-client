package pt.tecnico.dsi.designate

import pt.tecnico.dsi.designate.models.{Zone, ZoneCreate, ZoneUpdate}

class ZonesSpec extends Utils {
  "The Zones service" should {

    val dummyZoneCreate = ZoneCreate(
      name = "xxx.org.",
      email = "joe@example.org"
    )

    val dummyZoneUpdate = ZoneUpdate(
      ttl = Some(600),
      description = Some("new description")
    )

    "list zones" in {
      for {
        client <- designateClient
        _ <- client.zones.list().compile.toList
      } yield assert(true)
    }

    "create zones" in {
      for {
        client <- designateClient
        // TODO: Do we want `create` to fail sometimes?
        //  (if it already exists and lots of non-updatable parameters are passed)
        actual <- client.zones.create(dummyZoneCreate)
      } yield assert {
        // TODO: Find better way
        actual.model.email == dummyZoneCreate.email &&
        actual.model.name == dummyZoneCreate.name &&
        actual.model.description == dummyZoneCreate.description
      }
    }

    "get zone" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        _ <- client.zones.get(expected.id)
      } yield assert(true)
    }

    "delete zone" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        _ <- client.zones.delete(expected.id)
      } yield assert(true)
    }

    "update zone" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        // TODO: Getting 400 here
        _ <- client.zones.update(expected.id, dummyZoneUpdate)
      } yield assert(true)
    }

    "list groups" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        _ <- client.zones.listGroups(expected.id).compile.toList
      } yield assert(true)
    }

  }
}
