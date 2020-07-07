package org.folio.rest.migration.controller;

import org.folio.rest.migration.BibMigration;
import org.folio.rest.migration.HoldingMigration;
import org.folio.rest.migration.InventoryReferenceLinkMigration;
import org.folio.rest.migration.ItemMigration;
import org.folio.rest.migration.UserReferenceLinkMigration;
import org.folio.rest.migration.VendorMigration;
import org.folio.rest.migration.VendorReferenceLinkMigration;
import org.folio.rest.migration.aspect.annotation.CreateReferenceLinkTypes;
import org.folio.rest.migration.model.request.BibContext;
import org.folio.rest.migration.model.request.HoldingContext;
import org.folio.rest.migration.model.request.InventoryReferenceLinkContext;
import org.folio.rest.migration.model.request.ItemContext;
import org.folio.rest.migration.model.request.UserReferenceLinkContext;
import org.folio.rest.migration.model.request.VendorContext;
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

  @PostMapping("/user-reference-links")
  @CreateReferenceLinkTypes(path = "classpath:/referenceLinkTypes/users/*.json")
  public void userReferenceLinks(@RequestBody UserReferenceLinkContext context, @TenantHeader String tenant) {
    migrationService.migrate(UserReferenceLinkMigration.with(context, tenant));
  }

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

  @PostMapping("/holdings")
  public void holdings(@RequestBody HoldingContext context, @TenantHeader String tenant) {
    migrationService.migrate(HoldingMigration.with(context, tenant));
  }

  @PostMapping("/items")
  public void items(@RequestBody ItemContext context, @TenantHeader String tenant) {
    migrationService.migrate(ItemMigration.with(context, tenant));
  }

  @PostMapping("/vendors")
  public void vendors(@RequestBody VendorContext context, @TenantHeader String tenant) {
    migrationService.migrate(VendorMigration.with(context, tenant));
  }

}