package pt.tecnico.dsi.openstack.designate.models

import io.circe.derivation.Configuration

given Configuration = Configuration.default.withDefaults.withSnakeCaseMemberNames