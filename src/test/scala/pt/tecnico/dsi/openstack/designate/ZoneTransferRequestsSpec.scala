package pt.tecnico.dsi.openstack.designate

import pt.tecnico.dsi.openstack.designate.models.{Zone, ZoneTransferRequestCreate, ZoneTransferRequestUpdate}

class ZoneTransferRequestsSpec extends Utils {
  "Zone Tranfer Requests Service" should {

    val dummyZoneCreate = Zone.Create(
      name = "example.org.",
      email = "joe@example.org"
    )

    val dummyZoneTransferRequestCreate = ZoneTransferRequestCreate(
      description = Some("This is a zone transfer request."),
      targetProjectId = None
    )

    val dummyZoneTransferRequestUpdate = ZoneTransferRequestUpdate(
      description = Some("This is a zone transfer request after update."),
      targetProjectId = None
    )

    "create zone transfer request" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        req <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
      } yield req.zoneId shouldEqual zone.id
    }

    "update zone transfer request" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        before <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        actual <- client.zones.tasks.transferRequests.update(before.id, dummyZoneTransferRequestUpdate)
      } yield assert {
        actual.description == dummyZoneTransferRequestUpdate.description.get &&
        actual.targetProjectId == dummyZoneTransferRequestUpdate.targetProjectId
      }
    }

    "get zone transfer request" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        expected <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        actual <- client.zones.tasks.transferRequests.get(expected.id)
      } yield actual shouldEqual expected
    }

    "delete zone transfer request" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        expected <- client.zones.tasks.transferRequests.create(zone.id, dummyZoneTransferRequestCreate)
        _ <- client.zones.tasks.transferRequests.delete(expected.id)
        found <- client.zones.tasks.transferRequests.list().filter(_.id == expected.id).head.compile.last
      } yield found shouldEqual None
    }
  }
}