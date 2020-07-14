package pt.tecnico.dsi.openstack.designate

import cats.effect.{IO, Resource}
import org.scalatest.{Assertion, BeforeAndAfterAll}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.designate.models.Recordset
import pt.tecnico.dsi.openstack.designate.services.Recordsets

class RecordsetsSpec extends Utils with BeforeAndAfterAll {
  // This way we use the same zone for every test, and make the logs smaller and easier to debug.
  val (zone, deleteZone) = withStubZone.allocated.unsafeRunSync()
  override protected def afterAll(): Unit = deleteZone.unsafeRunSync()

  val recordsets: Recordsets[IO] = designate.zones.recordsets(zone.id)

  val withStubRecord: Resource[IO, WithId[Recordset]] = {
    val create = withRandomName { name =>
      recordsets.create(Recordset.Create(
        name = s"$name.${zone.name}",
        description = Some("This is an example record set."),
        ttl = Some(3600),
        `type` = "A",
        records = List("10.1.0.2"),
      ))
    }
    Resource.make(create)(record => recordsets.delete(record.id))
  }

  // Intellij gets confused and thinks ioAssertion2FutureAssertion conversion its being applied inside of `Resource.use`
  // instead of outside of `use`. We are explicit on the types params for `use` so Intellij doesn't show us an error.

  "Recordsets service" should {
    "create recordsets" in {
      val recordsetCreate = Recordset.Create(
        name = s"${randomName()}.${zone.name}",
        description = Some("This is an example record set."),
        ttl = Some(3600),
        `type` = "A",
        records = List("10.1.0.2"),
      )
      recordsets.create(recordsetCreate).idempotently { recordset =>
        recordset.model.`type` shouldBe recordsetCreate.`type`
        recordset.model.zoneId shouldBe zone.id
        recordset.model.description shouldBe recordsetCreate.description
        recordset.model.records shouldBe recordsetCreate.records
        recordset.model.ttl shouldBe recordsetCreate.ttl
        recordset.model.name shouldBe recordsetCreate.name
      }
    }

    "update recordsets" in withStubRecord.use[IO, Assertion] { recordset =>
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

    "delete recordsets" in withStubRecord.use[IO, Assertion] { recordset =>
      recordsets.delete(recordset.id).idempotently(_ shouldBe ())
    }

    "list recordsets" in withStubRecord.use[IO, Assertion] { recordset =>
      recordsets.list().compile.toList.idempotently { list =>
        list should contain (recordset)
      }
    }
  }

  "Designate client" should {
    "list recordsets" in {
      designate.recordsets.compile.toList.map(_ should not be empty)
    }
  }
}
