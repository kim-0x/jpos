package com.jpos.inventory.repository.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MockProductRepositoryTest {
    private MockProductRepository repository;

    @Before
    public void setUp() throws Exception {
        resetProducts();
        repository = new MockProductRepository();
    }

    @After
    public void tearDown() throws Exception {
        resetProducts();
    }

    @Test
    public void shouldTreatProductsWithDifferentIdentifiersAsNotEqual() {
        Product firstProduct = createProduct(UUID.randomUUID(), "barcode-1");
        Product secondProduct = createProduct(UUID.randomUUID(), "barcode-2");

        assertEquals(1, firstProduct.compareTo(secondProduct));
        assertEquals(1, firstProduct.compareTo(null));
    }

    @Test
    public void shouldTreatProductsWithSameIdAsEqual() {
        UUID productId = UUID.randomUUID();
        Product firstProduct = createProduct(productId, "barcode-1");
        Product secondProduct = createProduct(productId, "barcode-2");

        assertEquals(0, firstProduct.compareTo(secondProduct));
    }

    @Test
    public void shouldTreatProductsWithSameBarcodeAsEqual() {
        Product firstProduct = createProduct(UUID.randomUUID(), "shared-barcode");
        Product secondProduct = createProduct(UUID.randomUUID(), "shared-barcode");

        assertEquals(0, firstProduct.compareTo(secondProduct));
    }

    @Test
    public void shouldGetProductByIdOrBarcode() {
        Product product = createProduct(UUID.randomUUID(), "lookup-barcode");
        repository.saveProduct(product);

        Product productById = repository.getProductBy(new ProductQuery(product.getId(), null));
        Product productByBarcode = repository.getProductBy(new ProductQuery(null, product.getBarcode()));

        assertNotNull(productById);
        assertNotNull(productByBarcode);
        assertEquals(product.getId(), productById.getId());
        assertEquals(product.getId(), productByBarcode.getId());
    }

    @Test
    public void shouldReplaceExistingProductWhenBarcodeMatches() {
        Product existingProduct = createProduct(UUID.randomUUID(), "existing-barcode");
        existingProduct.setName("Old Product");
        repository.saveProduct(existingProduct);

        Product replacementProduct = createProduct(null, "existing-barcode");
        replacementProduct.setName("Updated Product");
        repository.saveProduct(replacementProduct);

        Product savedProduct = repository.getProductBy(new ProductQuery(existingProduct.getId(), null));
        Product[] products = repository.getProducts();

        assertNotNull(savedProduct);
        assertEquals(existingProduct.getId(), savedProduct.getId());
        assertEquals("Updated Product", savedProduct.getName());
        assertEquals(1, products.length);
    }

    @Test
    public void shouldReturnNullWhenNoProductMatchesQuery() {
        repository.saveProduct(createProduct(UUID.randomUUID(), "known-barcode"));

        Product product = repository.getProductBy(new ProductQuery(UUID.randomUUID(), "missing-barcode"));

        assertNull(product);
    }

    private Product createProduct(UUID productId, String barcode) {
        Product product = new Product();
        product.setId(productId);
        product.setBarcode(barcode);
        return product;
    }

    @SuppressWarnings("unchecked")
    private void resetProducts() throws Exception {
        Field productsField = MockProductRepository.class.getDeclaredField("products");
        productsField.setAccessible(true);
        ArrayList<Product> products = (ArrayList<Product>) productsField.get(null);
        products.clear();
    }
}
