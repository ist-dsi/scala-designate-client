package pt.tecnico.dsi.designate

import pt.tecnico.dsi.designate.models.{Action, FloatingIP, Status}

class FloatingIPsSpec extends Utils {
  "FloatingIPs Service" should {
    "list floating ips" in {
      for {
        designate <- designateClient
        _ <- designate.floatingIps.list.compile.toList
      } yield assert(true)
    }

    "get floating ip" in {
      for {
        client <- keystoneClient
        designate <- designateClient
        // Get sample region
        region <- client.regions.list().head.compile.lastOrError
        // Get sample floatingip
        floatingIp <- designate.floatingIps.list.head.compile.lastOrError
        _ <- designate.floatingIps.get(region.id, floatingIp.id)
      } yield assert(true)
    }

    val dummyFloatingIp = FloatingIP(
      ptrdname = "smtp.example.com",
      description = "cool description",
      ttl = 100000,
      address = "172.24.4.10",
      status = Some(Status.Active),
      action = Some(Action.Create)
    )

    "set floating ip" in {
      for {
        client <- keystoneClient
        designate <- designateClient
        // Get sample region
        region <- client.regions.list().head.compile.lastOrError
        // Get sample floatingip
        floatingIp <- designate.floatingIps.list.head.compile.lastOrError
        _ <- designate.floatingIps.set(region.id, floatingIp.id, dummyFloatingIp)
      } yield assert(true)
    }

    "unset floating ip" in {
      for {
        client <- keystoneClient
        designate <- designateClient
        // Get sample region
        region <- client.regions.list().head.compile.lastOrError
        // Get sample floatingip
        floatingIp <- designate.floatingIps.list.head.compile.lastOrError
        _ <- designate.floatingIps.unset(region.id, floatingIp.id)
      } yield assert(true)
    }
  }
}
