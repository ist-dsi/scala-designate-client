package pt.tecnico.dsi.openstack.designate

import pt.tecnico.dsi.openstack.designate.models.{Recordset, Zone}

class ZonesSpec extends Utils {
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
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        isIdempotent <- client.zones.list().compile.toList.idempotently { list =>
          list.exists(_.id == expected.id) shouldBe true
        }
      } yield isIdempotent
    }

    "create zones" in {
      for {
        client <- designateClient
        actual <- client.zones.create(dummyZoneCreate).idempotently { actual =>
          actual.email shouldBe dummyZoneCreate.email
          actual.name shouldBe dummyZoneCreate.name
          actual.description shouldBe dummyZoneCreate.description
        }
      } yield actual
    }

    "update zone" in {
      for {
        client <- designateClient
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
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        actual <- client.zones.get(expected.id)
      } yield expected shouldBe actual
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
        isIdempotent <- client.zones.delete(expected.id).idempotently(_ shouldBe ())
      } yield isIdempotent
    }

    val dummyRecordsetCreate = Recordset.Create(
      name = "example.org.",
      ttl = Some(3600),
      description = Some("This is an example record set."),
      records = List("10.1.0.2"),
      `type` = "A"
    )

    // TODO: Often get 400 error on this test.
    "create recordsets" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        recordset <- client.zones.recordsets(zone.id).create(dummyRecordsetCreate)
      } yield {
        recordset.model.`type` shouldBe dummyRecordsetCreate.`type`
        recordset.model.zoneId shouldBe zone.id
        recordset.model.description shouldBe dummyRecordsetCreate.description
        recordset.model.records shouldBe dummyRecordsetCreate.records
        recordset.model.ttl shouldBe dummyRecordsetCreate.ttl
        recordset.model.name shouldBe dummyRecordsetCreate.name
      }
    }

    val dummyRecordsetUpdate = Recordset.Update(
      ttl = Some(3601),
      description = Some("cool desc"),
      records = List("10.1.1.1")
    )

    "update recordsets" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        recordset <- client.zones.recordsets(zone.id).create(dummyRecordsetCreate)
        updated <- client.zones.recordsets(zone.id).update(recordset.id, dummyRecordsetUpdate)
      } yield {
        updated.ttl shouldBe dummyRecordsetUpdate.ttl
        updated.description shouldBe dummyRecordsetUpdate.description
        updated.records shouldBe dummyRecordsetUpdate.records
      }
    }

    "delete recordsets" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        recordset <- client.zones.recordsets(zone.id).create(dummyRecordsetCreate)
        isIdempotent <- client.zones.recordsets(zone.id).delete(recordset.id).idempotently(_ shouldBe ())
      } yield isIdempotent
    }

    "list recordsets" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        _ <- client.zones.recordsets(zone.id).create(dummyRecordsetCreate)
        isIdempotent <- client.zones.recordsets(zone.id).list().compile.toList.idempotently(_.isEmpty shouldBe false)
      } yield isIdempotent
    }

  }
}
