package pt.tecnico.dsi.openstack.designate

import pt.tecnico.dsi.openstack.designate.models.{Zone, ZoneTransferRequest}

class ZoneTransferAcceptsSpec extends Utils {
  "Zone Transfer Accept service" should {
    val dummyZoneCreate = Zone.Create(
      name = "example.org.",
      email = "joe@example.org"
    )

    val dummyZoneTransferRequestCreate = ZoneTransferRequest.Create(description = Some("a nice description"))

    "create zone transfer accept" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        req <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        acc <- client.zones.tasks.transferAccepts.create(req.key, req.id)
      } yield acc.zoneId shouldBe zone.id
    }

    "list zone transfer accepts" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        req <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        acc <- client.zones.tasks.transferAccepts.create(req.key, req.id)
        got <- client.zones.tasks.transferAccepts.list.filter(_.id == acc.id).head.compile.lastOrError
      } yield {
          got.id shouldBe acc.id
          /* For some reason `key` is None on `list` but not on `create`
             The docs say: Key that is used as part of the zone transfer accept process.
             This is only shown to the creator, and must be communicated out of band.
           */
          got.model.copy(`key` = None) shouldBe acc.model.copy(`key` = None)
        }
    }

    "get zone transfer accept" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        req <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        expected <- client.zones.tasks.transferAccepts.create(req.key, req.id)
        actual <- client.zones.tasks.transferAccepts.get(expected.id)
      } yield {
        expected.id shouldBe actual.id
        expected.model.copy(`key` = None) shouldBe actual.model.copy(`key` = None)
      }
    }
  }
}
