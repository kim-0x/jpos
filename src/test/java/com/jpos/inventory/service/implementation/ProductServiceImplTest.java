package com.jpos.inventory.service.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.repository.implementation.MockProductRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class ProductServiceImplTest {
    private ProductServiceImpl productService;
    private MockProductRepository productRepository;

    @Before
    public void setUp() throws Exception {
        resetProducts();
        productRepository = new MockProductRepository();
        productService = new ProductServiceImpl(productRepository);
    }

    @After
    public void tearDown() throws Exception {
        resetProducts();
    }

    @Test
    public void shouldSaveProductWhenCategoryExists() {
        productService.saveProduct("barcode-1", "Milk", "Dairy");

        Product[] products = productRepository.getProducts();

        assertEquals(1, products.length);
        assertNotNull(products[0].getId());
        assertEquals("barcode-1", products[0].getBarcode());
        assertEquals("Milk", products[0].getName());
        assertEquals("dairy", products[0].getCategory());
    }

    @Test
    public void shouldThrowWhenCategoryDoesNotExist() {
        assertThrows(InvalidParameterException.class,
                () -> productService.saveProduct("barcode-1", "Milk", "invalid-category"));
    }

    @SuppressWarnings("unchecked")
    private void resetProducts() throws Exception {
        Field productsField = MockProductRepository.class.getDeclaredField("products");
        productsField.setAccessible(true);
        ArrayList<Product> products = (ArrayList<Product>) productsField.get(null);
        products.clear();
    }
}
