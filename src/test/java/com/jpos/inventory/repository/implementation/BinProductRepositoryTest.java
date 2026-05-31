package com.jpos.inventory.repository.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.implementation.file.BinProductRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class BinProductRepositoryTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldLoadProductsFromDat() throws Exception {
        File productFile = temporaryFolder.newFile("product.dat");
        writeProducts(productFile, List.of(
                createProduct("00000000-0000-0000-0000-0050454d6969", "045678901231", "Lemons", ProductCategory.FRUIT),
                createProduct("00000000-0000-0000-0000-008c5cb57a84", "001234567890", "Milk", ProductCategory.DAIRY)));

        BinProductRepository repository = new BinProductRepository(productFile.toPath());

        Product[] products = repository.getProducts();
        Product lemons = repository.getProductBy(new ProductQuery(null, "045678901231"));

        assertEquals(2, products.length);
        assertNotNull(lemons);
        assertEquals("Lemons", lemons.getName());
    }

    @Test
    public void shouldPersistSavedProduct() throws Exception {
        File productFile = temporaryFolder.newFile("product.dat");
        writeProducts(productFile, List.of(
                createProduct("00000000-0000-0000-0000-0050454d6969", "045678901231", "Lemons", ProductCategory.FRUIT)));

        BinProductRepository repository = new BinProductRepository(productFile.toPath());
        Product product = new Product();
        product.setBarcode("123456789012");
        product.setName("Coffee");
        product.setCategory(ProductCategory.BEVERAGE);

        repository.saveProduct(product);

        BinProductRepository reloadedRepository = new BinProductRepository(productFile.toPath());
        Product savedProduct = reloadedRepository.getProductBy(new ProductQuery(null, "123456789012"));

        assertNotNull(savedProduct);
        assertEquals("Coffee", savedProduct.getName());
        assertNotNull(savedProduct.getId());
    }

    @Test
    public void shouldThrowWhenProductDatFileDoesNotExist() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new BinProductRepository(temporaryFolder.getRoot().toPath().resolve("missing-product.dat")));

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    private Product createProduct(String id, String barcode, String name, ProductCategory category) {
        Product product = new Product();
        product.setId(UUID.fromString(id));
        product.setBarcode(barcode);
        product.setName(name);
        product.setCategory(category);
        return product;
    }

    private void writeProducts(File targetFile, List<Product> products) throws Exception {
        ArrayList<Product> serializedProducts = new ArrayList<>(products);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(targetFile.toPath()))) {
            objectOutputStream.writeObject(serializedProducts);
        }
    }
}
