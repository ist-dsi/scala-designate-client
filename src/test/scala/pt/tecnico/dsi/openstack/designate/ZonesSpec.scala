package pt.tecnico.dsi.openstack.designate

import cats.effect.IO
import org.scalatest.Assertion
import pt.tecnico.dsi.openstack.designate.models.Zone

class ZonesSpec extends Utils {
  import designate.zones

  // Intellij gets confused and thinks ioAssertion2FutureAssertion conversion its being applied inside of `Resource.use`
  // instead of outside of `use`. We are explicit on the types params for `use` so Intellij doesn't show us an error.

  "The Zones service" should {
    "list zones" in withStubZone.use[IO, Assertion] { dummyZone =>
      zones.list().compile.toList.idempotently(_ should contain (dummyZone))
    }

    "create zones" in {
      val zoneCreate = Zone.Create(s"zones${randomName()}.org.", "john.doe@zones.org")
      for {
        result <- zones.create(zoneCreate).idempotently { actual =>
          actual.email shouldBe zoneCreate.email
          actual.name shouldBe zoneCreate.name
          actual.description shouldBe zoneCreate.description
        }
        zone <- zones.getByName(zoneCreate.name)
        _ <- zones.delete(zone.id)
      } yield result
    }

    "get zone" in withStubZone.use[IO, Assertion] { dummyZone =>
      zones.get(dummyZone.id).idempotently(_ shouldBe dummyZone)
    }

    "update zone" in withStubZone.use[IO, Assertion] { dummyZone =>
      val dummyZoneUpdate = Zone.Update(
        email = Some("afonso@example.org"),
        ttl = Some(600),
        description = Some("new description")
      )
      zones.update(dummyZone.id, dummyZoneUpdate).idempotently { actual =>
        dummyZoneUpdate.email.forall(_ == actual.model.email) shouldBe true
        dummyZoneUpdate.description shouldBe actual.model.description
        dummyZoneUpdate.ttl shouldBe actual.model.ttl
      }
    }

    "delete zone" in withStubZone.use[IO, Assertion] { dummyZone =>
      zones.delete(dummyZone.id).idempotently(_ shouldBe ())
    }

    "list nameservers" in withStubZone.use[IO, Assertion] { dummyZone =>
      zones.nameservers(dummyZone.id).compile.toList.idempotently { nameservers =>
        nameservers.length should be >= 1
      }
    }
  }
}
