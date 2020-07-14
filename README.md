# scala-designate-client [![license](http://img.shields.io/:license-MIT-blue.svg)](LICENSE)
[![Scaladoc](http://javadoc-badge.appspot.com/pt.tecnico.dsi/scala-designate-client_2.13.svg?label=scaladoc&style=plastic&maxAge=604800)](https://ist-dsi.github.io/scala-designate-client/api/latest/pt/tecnico/dsi/openstack/designate/index.html)
[![Latest version](https://index.scala-lang.org/ist-dsi/scala-designate-client/scala-designate-client/latest.svg)](https://index.scala-lang.org/ist-dsi/scala-designate-client/scala-designate-client)

[![Build Status](https://travis-ci.org/ist-dsi/scala-designate-client.svg?branch=master&style=plastic&maxAge=604800)](https://travis-ci.org/ist-dsi/scala-designate-client)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/1c7b4506389742b993d6f018d2a19509)](https://www.codacy.com/gh/ist-dsi/scala-designate-client?utm_source=github.com&utm_medium=referral&utm_content=ist-dsi/scala-designate-client&utm_campaign=Badge_Coverage)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/1c7b4506389742b993d6f018d2a19509)](https://www.codacy.com/gh/ist-dsi/scala-designate-client?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ist-dsi/scala-designate-client&amp;utm_campaign=Badge_Grade)
[![BCH compliance](https://bettercodehub.com/edge/badge/ist-dsi/scala-designate-client)](https://bettercodehub.com/results/ist-dsi/scala-designate-client)

The Scala client for Openstack Designate.

Currently supported endpoints:

- [Zones](https://docs.openstack.org/api-ref/dns/#zone)
- [Zone Ownership Transfers - Requests](https://docs.openstack.org/api-ref/dns/#zone-ownership-transfers-requests)
- [Zone Ownership Transfers - Accepts](https://docs.openstack.org/api-ref/dns/#zone-ownership-transfers-accepts)
- [Recordsets](https://docs.openstack.org/api-ref/dns/#recordsets)
- [Limits](https://docs.openstack.org/api-ref/dns/#limits)
- [Quotas](https://docs.openstack.org/api-ref/dns/#quotas)
- [Floating IPs](https://docs.openstack.org/api-ref/dns/#floatingips)

[Latest scaladoc documentation](https://ist-dsi.github.io/scala-designate-client/api/latest/pt/tecnico/dsi/openstack/designate/index.html)

## Install
Add the following dependency to your `build.sbt`:
```sbt
libraryDependencies += "pt.tecnico.dsi" %% "scala-designate-client" % "0.0.0"
```
We use [semantic versioning](http://semver.org).

## License
scala-designate-client is open source and available under the [MIT license](LICENSE).
