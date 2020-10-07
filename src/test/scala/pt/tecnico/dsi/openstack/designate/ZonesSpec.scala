package pt.tecnico.dsi.openstack.designate

import java.util.UUID
import cats.effect.IO
import pt.tecnico.dsi.openstack.common.models.UnexpectedStatus
import org.scalatest.Assertion
import pt.tecnico.dsi.openstack.designate.models.Zone

class ZonesSpec extends Utils {
  import designate.zones

  // Intellij gets confused and thinks ioAssertion2FutureAssertion conversion its being applied inside of `Resource.use`
  // instead of outside of `use`. We are explicit on the types params for `use` so Intellij doesn't show us an error.

  "The Zones service" should {
    "list zones" in withStubZone.use[IO, Assertion] { dummyZone =>
      zones.list().idempotently(_ should contain (dummyZone))
    }

    "createOrUpdate zones" in {
      val zoneCreate = Zone.Create(s"zones${randomName()}.org.", "john.doe@zones.org")
      for {
        result <- zones.createOrUpdate(zoneCreate).idempotently { actual =>
          actual.email shouldBe zoneCreate.email
          actual.name shouldBe zoneCreate.name
          actual.description shouldBe zoneCreate.description
        }
        zone <- zones.applyByName(zoneCreate.name)
        _ <- zones.delete(zone.id)
      } yield result
    }

    "get zone (existing id)" in withStubZone.use[IO, Assertion] { dummyZone =>
      zones.get(dummyZone.id).idempotently(_.value shouldBe dummyZone)
    }
    "get zone (non-existing id)" in {
      zones.get(UUID.randomUUID().toString).idempotently(_ shouldBe None)
    }

    "apply zone (existing id)" in withStubZone.use[IO, Assertion] { dummyZone =>
      zones.apply(dummyZone.id).idempotently(_ shouldBe dummyZone)
    }
    "apply zone (non-existing id)" in {
      zones.apply(UUID.randomUUID().toString).attempt.idempotently(_.left.value shouldBe a [UnexpectedStatus])
    }

    "update zone" in withStubZone.use[IO, Assertion] { dummyZone =>
      val dummyZoneUpdate = Zone.Update(
        email = Some("afonso@example.org"),
        ttl = Some(600),
        description = Some("new description")
      )
      zones.update(dummyZone.id, dummyZoneUpdate).idempotently { actual =>
        dummyZoneUpdate.email.forall(_ == actual.email) shouldBe true
        dummyZoneUpdate.description shouldBe actual.description
        dummyZoneUpdate.ttl shouldBe actual.ttl
      }
    }

    "delete zone" in withStubZone.use[IO, Assertion] { dummyZone =>
      zones.delete(dummyZone).idempotently(_ shouldBe ())
    }

    "list nameservers" in withStubZone.use[IO, Assertion] { dummyZone =>
      zones.nameservers(dummyZone.id).idempotently { nameservers =>
        nameservers.length should be >= 1
      }
    }
  }
}
