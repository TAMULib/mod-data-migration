package org.folio.rest.migration.controller;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.migration.BibMigration;
import org.folio.rest.migration.BoundWithMigration;
import org.folio.rest.migration.DivITPatronMigration;
import org.folio.rest.migration.FeeFineMigration;
import org.folio.rest.migration.HoldingsMigration;
import org.folio.rest.migration.InventoryReferenceLinkMigration;
import org.folio.rest.migration.ItemMigration;
import org.folio.rest.migration.LoanMigration;
import org.folio.rest.migration.PurchaseOrderMigration;
import org.folio.rest.migration.ProxyForMigration;
import org.folio.rest.migration.RequestMigration;
import org.folio.rest.migration.UserMigration;
import org.folio.rest.migration.UserReferenceLinkMigration;
import org.folio.rest.migration.VendorMigration;
import org.folio.rest.migration.VendorReferenceLinkMigration;
import org.folio.rest.migration.aspect.annotation.CreateCalendarPeriods;
import org.folio.rest.migration.aspect.annotation.CreateReferenceData;
import org.folio.rest.migration.aspect.annotation.CreateReferenceLinkTypes;
import org.folio.rest.migration.aspect.annotation.UpdateRules;
import org.folio.rest.migration.model.request.bib.BibContext;
import org.folio.rest.migration.model.request.boundwith.BoundWithContext;
import org.folio.rest.migration.model.request.divitpatron.DivITPatronContext;
import org.folio.rest.migration.model.request.feefine.FeeFineContext;
import org.folio.rest.migration.model.request.holdings.HoldingsContext;
import org.folio.rest.migration.model.request.inventory.InventoryReferenceLinkContext;
import org.folio.rest.migration.model.request.item.ItemContext;
import org.folio.rest.migration.model.request.loan.LoanContext;
import org.folio.rest.migration.model.request.proxyfor.ProxyForContext;
import org.folio.rest.migration.model.request.purchaseorder.PurchaseOrderContext;
import org.folio.rest.migration.model.request.request.RequestContext;
import org.folio.rest.migration.model.request.user.UserContext;
import org.folio.rest.migration.model.request.user.UserReferenceLinkContext;
import org.folio.rest.migration.model.request.vendor.VendorContext;
import org.folio.rest.migration.model.request.vendor.VendorReferenceLinkContext;
import org.folio.rest.migration.service.CalendarService;
import org.folio.rest.migration.service.MigrationService;
import org.folio.rest.migration.service.ReferenceDataService;
import org.folio.rest.migration.service.RulesService;
import org.folio.spring.tenant.annotation.TenantHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/migrate")
public class MigrationController {

  @Autowired
  private MigrationService migrationService;

  @Autowired
  private ReferenceDataService referenceDataService;

  @Autowired
  private CalendarService calendarService;

  @Autowired
  private RulesService rulesService;

  @PostMapping("/reference-data")
  public CompletableFuture<Void> loadReferenceData(@TenantHeader String tenant) {
    return referenceDataService.loadReferenceDataAsync("classpath:/referenceData/**/*.json", tenant);
  }

  @PostMapping("/calendar-periods")
  public CompletableFuture<Void> createCalendarPeriods(@TenantHeader String tenant) {
    return calendarService.createCalendarPeriodsAsync("classpath:/calendar/*.json", tenant);
  }

  @PostMapping("/circ-rules")
  public CompletableFuture<Void> updateCircRules(@TenantHeader String tenant) {
    return rulesService.updateRulesAsync("classpath:/rules/loans/rules.json", "circulation-rules-storage", tenant);
  }

  @PostMapping("/bib-mapping-rules")
  public CompletableFuture<Void> updateBibMappingRules(@TenantHeader String tenant) {
    return rulesService.updateRulesAsync("classpath:/rules/bibs/rules.json", "mapping-rules", tenant);
  }

  @PostMapping("/user-reference-links")
  @CreateReferenceLinkTypes(path = "classpath:/referenceLinkTypes/users/*.json")
  public CompletableFuture<String> userReferenceLinks(
      @RequestBody UserReferenceLinkContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(UserReferenceLinkMigration.with(context, tenant));
  }

  @PostMapping("/users")
  @CreateReferenceData(pattern = "classpath:/referenceData/users/*.json")
  public CompletableFuture<String> users(
      @RequestBody UserContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(UserMigration.with(context, tenant));
  }

  @PostMapping("/vendor-reference-links")
  @CreateReferenceLinkTypes(path = "classpath:/referenceLinkTypes/vendors/*.json")
  public CompletableFuture<String> vendorReferenceLinks(
      @RequestBody VendorReferenceLinkContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(VendorReferenceLinkMigration.with(context, tenant));
  }

  @PostMapping("/vendors")
  @CreateReferenceData(pattern = "classpath:/referenceData/vendors/*.json")
  public CompletableFuture<String> vendors(
      @RequestBody VendorContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(VendorMigration.with(context, tenant));
  }

  @PostMapping("/inventory-reference-links")
  @CreateReferenceLinkTypes(path = "classpath:/referenceLinkTypes/inventory/*.json")
  public CompletableFuture<String> inventoryReferenceLinks(
      @RequestBody InventoryReferenceLinkContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(InventoryReferenceLinkMigration.with(context, tenant));
  }

  @PostMapping("/bibs")
  @CreateReferenceData(pattern = "classpath:/referenceData/bibs/*.json")
  @UpdateRules(file = "classpath:/rules/bibs/rules.json", path = "mapping-rules")
  public CompletableFuture<String> bibs(
      @RequestBody BibContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData,
      @RequestParam(required = false, defaultValue = "false") boolean skipRules
  ) {
    return migrationService.migrate(BibMigration.with(context, tenant));
  }

  @PostMapping("/holdings")
  @CreateReferenceData(pattern = "classpath:/referenceData/holdings/*.json")
  @CreateReferenceLinkTypes(path = "classpath:/referenceLinkTypes/holdings/*.json")
  public CompletableFuture<String> holdings(
      @RequestBody HoldingsContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(HoldingsMigration.with(context, tenant));
  }

  @PostMapping("/items")
  @CreateReferenceData(pattern = "classpath:/referenceData/items/*.json")
  public CompletableFuture<String> items(
      @RequestBody ItemContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(ItemMigration.with(context, tenant));
  }

  @PostMapping("/requests")
  public CompletableFuture<String> requests(
      @RequestBody RequestContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(RequestMigration.with(context, tenant));
  }

  @PostMapping("/loans")
  @CreateCalendarPeriods(pattern = "classpath:/calendar/*.json")
  @CreateReferenceData(pattern = "classpath:/referenceData/loans/*.json")
  @UpdateRules(file = "classpath:/rules/loans/rules.json", path = "circulation-rules-storage")
  public CompletableFuture<String> loans(
      @RequestBody LoanContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData,
      @RequestParam(required = false, defaultValue = "false") boolean skipRules,
      @RequestParam(required = false, defaultValue = "false") boolean skipCalendarPeriods
  ) {
    return migrationService.migrate(LoanMigration.with(context, tenant));
  }

  @PostMapping("/feesfines")
  @CreateReferenceData(pattern = "classpath:/referenceData/feesfines/*.json")
  public CompletableFuture<String> feesfines(
      @RequestBody FeeFineContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(FeeFineMigration.with(context, tenant));
  }

  @PostMapping("/proxyfor")
  public CompletableFuture<String> proxyfor(
      @RequestBody ProxyForContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(ProxyForMigration.with(context, tenant));
  }

  @PostMapping("/boundwith")
  public CompletableFuture<String> boundwith(
      @RequestBody BoundWithContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(BoundWithMigration.with(context, tenant));
  }

  @PostMapping("/divitpatron")
  public CompletableFuture<String> divitpatron(
      @RequestBody DivITPatronContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(DivITPatronMigration.with(context, tenant));
  }

  @PostMapping("/purchaseorders")
  @CreateReferenceData(pattern = "classpath:/referenceData/purchaseorders/*.json")
  public CompletableFuture<String> purchaseorders(
      @RequestBody PurchaseOrderContext context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return migrationService.migrate(PurchaseOrderMigration.with(context, tenant));
  }

  @PostMapping("/coursereserves")
  @CreateReferenceData(pattern = "classpath:/referenceData/coursereserves/*.json")
  public String coursereserves(
      @RequestBody JsonNode context,
      @TenantHeader String tenant,
      @RequestParam(required = false, defaultValue = "false") boolean skipReferenceData
  ) {
    return "Migration not yet implemented";
  }

}
