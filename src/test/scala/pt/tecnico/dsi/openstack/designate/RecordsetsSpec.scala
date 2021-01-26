package pt.tecnico.dsi.openstack.designate

import cats.effect.unsafe.implicits.global
import scala.annotation.nowarn
import cats.effect.{IO, Resource}
import cats.syntax.show._
import org.scalatest.BeforeAndAfterAll
import pt.tecnico.dsi.openstack.designate.models.Recordset
import pt.tecnico.dsi.openstack.designate.services.Recordsets

class RecordsetsSpec extends Utils with BeforeAndAfterAll {
  // This way we use the same zone for every test, and make the logs smaller and easier to debug.
  val (zone, deleteZone) = withStubZone.allocated.unsafeRunSync()
  override protected def afterAll(): Unit = {
    deleteZone.unsafeRunSync()
    super.afterAll()
  }
  
  val recordsets: Recordsets[IO] = designate.zones.recordsets(zone.id)
  
  val withStubRecord: Resource[IO, Recordset] = resourceCreator(recordsets) { name =>
    Recordset.Create(
      name = s"$name.${zone.name}",
      description = Some("This is an example record set."),
      ttl = Some(3600),
      `type` = "A",
      records = List("10.1.0.2"),
    )
  }
  
  "Recordsets service" should {
    "createOrUpdate recordsets" in {
      val recordsetCreate = Recordset.Create(
        name = s"${randomName()}.${zone.name}",
        description = Some("This is an example record set."),
        ttl = Some(3600),
        `type` = "A",
        records = List("10.1.0.2"),
      )
      recordsets.createOrUpdate(recordsetCreate).idempotently { recordset =>
        recordset.`type` shouldBe recordsetCreate.`type`
        recordset.zoneId shouldBe zone.id
        recordset.description shouldBe recordsetCreate.description
        recordset.records shouldBe recordsetCreate.records
        recordset.ttl shouldBe recordsetCreate.ttl
        recordset.name shouldBe recordsetCreate.name
      }
    }
    
    "update recordsets" in withStubRecord.use { recordset =>
      val recordsetUpdate = Recordset.Update(
        ttl = Some(3601),
        description = Some("cool desc"),
        records = Some(List("10.1.1.1")),
      )
      recordsets.update(recordset.id, recordsetUpdate).idempotently { updated =>
        updated.ttl shouldBe recordsetUpdate.ttl
        updated.description shouldBe recordsetUpdate.description
        updated.records shouldBe recordsetUpdate.records.value
      }
    }
    
    "delete recordsets" in withStubRecord.use { recordset =>
      recordsets.delete(recordset.id).idempotently(_ shouldBe ())
    }
    
    "list recordsets" in withStubRecord.use { recordset =>
      recordsets.list().idempotently { list =>
        list should contain (recordset)
      }
    }
    
    s"show recordsets" in withStubRecord.use { model =>
      //This line is a fail fast mechanism, and prevents false positives from the linter
      println(show"$model")
      IO("""show"$model"""" should compile): @nowarn
    }
  }
  
  "Designate client" should {
    "list recordsets" in {
      designate.recordsets.compile.toList.map(_ should not be empty)
    }
  }
}
