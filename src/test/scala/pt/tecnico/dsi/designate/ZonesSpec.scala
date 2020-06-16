package pt.tecnico.dsi.designate

import pt.tecnico.dsi.designate.models.{RecordsetCreate, RecordsetUpdate, ZoneCreate, ZoneUpdate}

class ZonesSpec extends Utils {
  "The Zones service" should {

    val dummyZoneCreate = ZoneCreate(
      name = "example.org.",
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
            actual.email == dummyZoneCreate.email &&
            actual.name == dummyZoneCreate.name &&
            actual.description == dummyZoneCreate.description
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
          val equalDesc = dummyZoneUpdate.description == actual.model.description
          val equalTtl = dummyZoneUpdate.ttl == actual.model.ttl
          assert (equalEmail && equalDesc && equalTtl)
        }
      } yield isIdempotent
    }

    "get zone" in {
      for {
        client <- designateClient
        expected <- client.zones.create(dummyZoneCreate)
        actual <- client.zones.get(expected.id)
      } yield expected shouldEqual actual
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

    val dummyRecordsetCreate = RecordsetCreate(
      name = "example.org.",
      ttl = Some(3600),
      description = Some("This is an example record set."),
      records = Seq("10.1.0.2"),
      `type` = "A"
    )

    // TODO: Often get 400 error on this test.
    "create recordsets" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        recordset <- client.zones.recordsets(zone.id).create(dummyRecordsetCreate)
      } yield assert {
        recordset.model.`type` == dummyRecordsetCreate.`type` &&
        recordset.model.zoneId == zone.id &&
        recordset.model.description == dummyRecordsetCreate.description &&
        recordset.model.records == dummyRecordsetCreate.records &&
        recordset.model.ttl == dummyRecordsetCreate.ttl &&
        recordset.model.name == dummyRecordsetCreate.name
      }
    }

    val dummyRecordsetUpdate = RecordsetUpdate(
      ttl = Some(3601),
      description = Some("cool desc"),
      records = Seq("10.1.1.1")
    )

    "update recordsets" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        recordset <- client.zones.recordsets(zone.id).create(dummyRecordsetCreate)
        updated <- client.zones.recordsets(zone.id).update(recordset.id, dummyRecordsetUpdate)
      } yield assert {
        updated.ttl == dummyRecordsetUpdate.ttl &&
        updated.description == dummyRecordsetUpdate.description &&
        updated.records == dummyRecordsetUpdate.records
      }
    }

    "delete recordsets" in {
      for {
        client <- designateClient
        zone <- client.zones.create(dummyZoneCreate)
        recordset <- client.zones.recordsets(zone.id).create(dummyRecordsetCreate)
        isIdempotent <- client.zones.recordsets(zone.id).delete(recordset.id).valueShouldIdempotentlyBe(())
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
