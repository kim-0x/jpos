package com.jpos.bluejay.api;

import com.jpos.inventory.InventoryFacade;
import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;
import com.jpos.inventory.model.StockRecord;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryFacade inventoryFacade;

    public InventoryController(InventoryFacade inventoryFacade) {
        this.inventoryFacade = inventoryFacade;
    }

    @PostMapping("/products")
    @ResponseStatus(CREATED)
    public void createProduct(@Valid @RequestBody CreateProductRequest request) {
        inventoryFacade.createNewProduct(request.barcode(), request.name(), request.category());
    }

    @GetMapping("/products")
    public List<ProductResponse> getProducts() {
        Product[] products = inventoryFacade.getProducts();
        return Arrays.stream(products)
                .map(p -> new ProductResponse(p.getId() == null ? null : p.getId().toString(), p.getBarcode(), p.getName(), p.getCategory().getValue()))
                .toList();
    }

    @PostMapping("/stocks/entries")
    @ResponseStatus(CREATED)
    public void stockEntry(@Valid @RequestBody StockEntryRequest request) {
        inventoryFacade.stockEntry(request.barcode(), request.cost(), request.quantity());
    }

    @GetMapping("/stocks/report")
    public List<StockReportResponse> getStockReport() {
        StockRecord[] records = inventoryFacade.getStockReport();
        return Arrays.stream(records)
                .map(r -> new StockReportResponse(
                        r.getProduct().getId() == null ? null : r.getProduct().getId().toString(),
                        r.getProduct().getBarcode(),
                        r.getProduct().getName(),
                        r.getCost(),
                        r.getNumberInStock(),
                        r.getCreatedAt().getTime()
                ))
                .toList();
    }

    public record CreateProductRequest(@NotBlank String barcode,
                                       @NotBlank String name,
                                       @NotNull ProductCategory category) {
    }

    public record StockEntryRequest(@NotBlank String barcode,
                                    @DecimalMin(value = "0.0", inclusive = false) double cost,
                                    @DecimalMin(value = "0.0", inclusive = false) float quantity) {
    }

    public record ProductResponse(String id, String barcode, String name, String category) {
    }

    public record StockReportResponse(String productId,
                                      String barcode,
                                      String productName,
                                      double cost,
                                      double numberInStock,
                                      long createdAtEpochMs) {
    }
}
