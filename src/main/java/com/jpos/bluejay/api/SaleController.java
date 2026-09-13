package com.jpos.bluejay.api;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.SaleFacade;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.model.SaleItem;
import com.jpos.sale.model.SaleItemData;
import com.jpos.sale.model.SaleTransaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleFacade saleFacade;

    public SaleController(SaleFacade saleFacade) {
        this.saleFacade = saleFacade;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessSaleResponse processTransaction(@Valid @RequestBody ProcessSaleRequest request) {
        SaleItemData[] items = request.items().stream()
                .map(i -> new SaleItemData(i.barcode(), i.quantity()))
                .toArray(SaleItemData[]::new);

        UUID transactionId = request.transactionDateEpochMs() == null
                ? saleFacade.processSaleTransaction(request.receiptNumber(), items)
                : saleFacade.processSaleTransaction(request.receiptNumber(), items, new Date(request.transactionDateEpochMs()));

        return new ProcessSaleResponse(transactionId.toString());
    }

    @GetMapping("/transactions/{transactionId}")
    public SaleTransactionResponse getTransaction(@PathVariable UUID transactionId) {
        SaleTransaction transaction = saleFacade.getTransactionById(transactionId);
        if (transaction == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        }
        return toResponse(transaction);
    }

    @GetMapping("/transactions")
    public List<SaleTransactionResponse> getAllTransactions() {
        return Arrays.stream(saleFacade.getAllTransactions()).map(this::toResponse).toList();
    }

    @PutMapping("/prices")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPrice(@Valid @RequestBody SetPriceRequest request) {
        validateIdentifier(request.productId(), request.barcode());
        saleFacade.setProductPrice(toProductQuery(request.productId(), request.barcode()), request.margin());
    }

    @GetMapping("/prices/current")
    public CurrentPriceResponse getCurrentPrice(@RequestParam(required = false) UUID productId,
                                                @RequestParam(required = false) String barcode) {
        validateIdentifier(productId, barcode);
        PriceBook priceBook = saleFacade.getCurrentProductPrice(toProductQuery(productId, barcode));
        if (priceBook == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found");
        }
        return new CurrentPriceResponse(
                priceBook.getProductId().toString(),
                priceBook.getCost(),
                priceBook.getMargin(),
                priceBook.getSalePrice(),
                priceBook.getEffectiveAt().getTime()
        );
    }

    @GetMapping("/products/{barcode}/availability")
    public ProductAvailabilityResponse isProductAvailable(@PathVariable String barcode) {
        return new ProductAvailabilityResponse(barcode, saleFacade.isProductAvailable(barcode));
    }

    @GetMapping("/products/{productId}/name")
    public ProductNameResponse getProductName(@PathVariable UUID productId) {
        String name = saleFacade.getProductName(productId);
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return new ProductNameResponse(productId.toString(), name);
    }

    private ProductQuery toProductQuery(UUID productId, String barcode) {
        return new ProductQuery(productId, barcode);
    }

    private void validateIdentifier(UUID productId, String barcode) {
        boolean hasProductId = productId != null;
        boolean hasBarcode = barcode != null && !barcode.isBlank();
        if (hasProductId == hasBarcode) {
            throw new IllegalArgumentException("Provide exactly one identifier: either productId or barcode.");
        }
    }

    private SaleTransactionResponse toResponse(SaleTransaction transaction) {
        return new SaleTransactionResponse(
                transaction.getHeader().getTransactionId().toString(),
                transaction.getHeader().getReceiptNumber(),
                transaction.getHeader().getGrandTotal(),
                transaction.getHeader().getTransactionDate().getTime(),
                Arrays.stream(transaction.getSaleItems()).map(this::toResponse).toList()
        );
    }

    private SaleItemResponse toResponse(SaleItem item) {
        return new SaleItemResponse(
                item.getProductId().toString(),
                item.getQuantity(),
                item.getCost(),
                item.getPrice(),
                item.getTotalPrice()
        );
    }

    public record ProcessSaleRequest(@NotBlank String receiptNumber,
                                     @NotEmpty List<@Valid SaleItemRequest> items,
                                     Long transactionDateEpochMs) {
    }

    public record SaleItemRequest(@NotBlank String barcode,
                                  @DecimalMin(value = "0.0", inclusive = false) float quantity) {
    }

    public record ProcessSaleResponse(String transactionId) {
    }

    public record SaleTransactionResponse(String transactionId,
                                          String receiptNumber,
                                          double grandTotal,
                                          long transactionDateEpochMs,
                                          List<SaleItemResponse> items) {
    }

    public record SaleItemResponse(String productId,
                                   float quantity,
                                   double cost,
                                   double price,
                                   double totalPrice) {
    }

    public record SetPriceRequest(UUID productId,
                                  String barcode,
                                  @DecimalMin(value = "0.0", inclusive = false) float margin) {
    }

    public record CurrentPriceResponse(String productId,
                                       double cost,
                                       float margin,
                                       double salePrice,
                                       long effectiveAtEpochMs) {
    }

    public record ProductAvailabilityResponse(String barcode, boolean available) {
    }

    public record ProductNameResponse(String productId, String name) {
    }
}
