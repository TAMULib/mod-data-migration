package org.folio.rest.migration.controller;

import org.folio.rest.migration.BibMigration;
import org.folio.rest.migration.InventoryReferenceLinkMigration;
import org.folio.rest.migration.VendorReferenceLinkMigration;
import org.folio.rest.migration.aspect.annotation.CreateReferenceLinkTypes;
import org.folio.rest.migration.model.request.BibContext;
import org.folio.rest.migration.model.request.InventoryReferenceLinkContext;
import org.folio.rest.migration.model.request.VendorReferenceLinkContext;
import org.folio.rest.migration.service.MigrationService;
import org.folio.spring.tenant.annotation.TenantHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/migrate")
public class MigrationController {

  @Autowired
  private MigrationService migrationService;

  @PostMapping("/vendor-reference-links")
  @CreateReferenceLinkTypes(path = "classpath:/referenceLinkTypes/vendors/*.json")
  public void vendorReferenceLinks(@RequestBody VendorReferenceLinkContext context, @TenantHeader String tenant) {
    migrationService.migrate(VendorReferenceLinkMigration.with(context, tenant));
  }

  @PostMapping("/inventory-reference-links")
  @CreateReferenceLinkTypes(path = "classpath:/referenceLinkTypes/inventory/*.json")
  public void inventoryReferenceLinks(@RequestBody InventoryReferenceLinkContext context, @TenantHeader String tenant) {
    migrationService.migrate(InventoryReferenceLinkMigration.with(context, tenant));
  }

  @PostMapping("/bibs")
  public void bibs(@RequestBody BibContext context, @TenantHeader String tenant) {
    migrationService.migrate(BibMigration.with(context, tenant));
  }

}
