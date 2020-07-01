package pt.tecnico.dsi.openstack.designate

import pt.tecnico.dsi.openstack.designate.models.{Zone, ZoneTransferRequest}

class ZoneTransferRequestsSpec extends Utils {
  "Zone Tranfer Requests Service" should {
    val dummyZoneCreate = Zone.Create(name = "example.org.",email = "joe@example.org")
    val dummyZoneTransferRequestCreate = ZoneTransferRequest.Create(Some("a nice description"))
    val dummyZoneTransferRequestUpdate = ZoneTransferRequest.Update(Some("a newer and updated nicer description"))

    "create zone transfer request" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        req <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
      } yield req.zoneId shouldBe zone.id
    }

    "update zone transfer request" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        before <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        actual <- client.zones.tasks.transferRequests.update(before.id, dummyZoneTransferRequestUpdate)
      } yield {
        actual.description shouldBe dummyZoneTransferRequestUpdate.description.get
        actual.targetProjectId shouldBe dummyZoneTransferRequestUpdate.targetProjectId
      }
    }

    "get zone transfer request" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        expected <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        actual <- client.zones.tasks.transferRequests.get(expected.id)
      } yield actual shouldBe expected
    }

    "delete zone transfer request" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        expected <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        _ <- client.zones.tasks.transferRequests.delete(expected.id)
        found <- client.zones.tasks.transferRequests.list().filter(_.id == expected.id).head.compile.last
      } yield found shouldBe None
    }
  }
}
