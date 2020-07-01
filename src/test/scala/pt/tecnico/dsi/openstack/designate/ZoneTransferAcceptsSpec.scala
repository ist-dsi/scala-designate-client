package pt.tecnico.dsi.openstack.designate

import pt.tecnico.dsi.openstack.designate.models.{Zone, ZoneTransferRequestCreate}

class ZoneTransferAcceptsSpec extends Utils {
  "Zone Transfer Accept service" should {

    val dummyZoneCreate = Zone.Create(
      name = "example.org.",
      email = "joe@example.org"
    )

    val dummyZoneTransferRequestCreate = ZoneTransferRequestCreate(
      description = Some("This is a zone transfer request."),
      targetProjectId = None
    )

    "create zone transfer accept" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        req <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        acc <- client.zones.tasks.transferAccepts.create(req.key, req.id)
      } yield acc.zoneId shouldEqual zone.id
    }

    "list zone transfer accepts" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        req <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        acc <- client.zones.tasks.transferAccepts.create(req.key, req.id)
        got <- client.zones.tasks.transferAccepts.list.filter(_.id == acc.id).head.compile.lastOrError
      } yield
        assert {
          /* For some reason `key` is None on `list` but not on `create`
             The docs say: Key that is used as part of the zone transfer accept process.
             This is only shown to the creator, and must be communicated out of band.
           */
          (got.id == acc.id) && (got.model.copy(`key` = None) == acc.model.copy(`key` = None))
        }
    }

    "get zone transfer accept" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        req <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        expected <- client.zones.tasks.transferAccepts.create(req.key, req.id)
        actual <- client.zones.tasks.transferAccepts.get(expected.id)
      } yield assert {
        expected.id == actual.id &&
        expected.model.copy(`key` = None) == actual.model.copy(`key` = None)
      }
    }

  }
}
