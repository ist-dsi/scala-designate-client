package pt.tecnico.dsi.openstack.designate

import scala.util.Random
import cats.effect.IO
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.designate.models.{Recordset, Zone}
import pt.tecnico.dsi.openstack.designate.services.Recordsets

class RecordsetsSpec extends Utils {
  val withStubRecord: IO[(Recordsets[IO], String, Recordset.Create, WithId[Recordset])] =
    for {
      designate <- client
      dummyZone <- designate.zones.create(Zone.Create("example.org.", "joe@example.org"))
      recordsets = designate.zones.recordsets(dummyZone.id)
      recordsetCreate = Recordset.Create(
        name = s"sudomain${Random.nextInt()}.example.org.",
        description = Some("This is an example record set."),
        ttl = Some(3600),
        `type` = "A",
        records = List("10.1.0.2"),
      )
      recordset <- recordsets.create(recordsetCreate)
    } yield (recordsets, dummyZone.id, recordsetCreate, recordset)

  "Recordsets service" should {
    "create recordsets" in withStubRecord.flatMap { case (recordsets, dummyZoneId, recordsetCreate, _) =>
      recordsets.create(recordsetCreate).idempotently { recordset =>
        recordset.model.`type` shouldBe recordsetCreate.`type`
        recordset.model.zoneId shouldBe dummyZoneId
        recordset.model.description shouldBe recordsetCreate.description
        recordset.model.records shouldBe recordsetCreate.records
        recordset.model.ttl shouldBe recordsetCreate.ttl
        recordset.model.name shouldBe recordsetCreate.name
      }
    }

    "update recordsets" in withStubRecord.flatMap { case (recordsets, _, _, recordset) =>
      val recordsetUpdate = Recordset.Update(
        ttl = Some(3601),
        description = Some("cool desc"),
        records = List("10.1.1.1")
      )
      recordsets.update(recordset.id, recordsetUpdate).idempotently { updated =>
        updated.ttl shouldBe recordsetUpdate.ttl
        updated.description shouldBe recordsetUpdate.description
        updated.records shouldBe recordsetUpdate.records
      }
    }

    "delete recordsets" in withStubRecord.flatMap { case (recordsets, _, _, recordset) =>
      recordsets.delete(recordset.id).idempotently(_ shouldBe ())
    }

    "list recordsets" in withStubRecord.flatMap { case (recordsets, _, _, recordset) =>
      recordsets.list().compile.toList.idempotently { list =>
        list should contain oneElementOf(Seq(recordset))
      }
    }
  }

  "Designate client" should {
    "list recordsets" in {
      for {
        client <- client
        recordsets <- client.recordsets.compile.toList
      } yield {
        recordsets should not be empty
      }
    }
  }
}
