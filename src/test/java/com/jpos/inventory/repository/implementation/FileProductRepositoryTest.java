package com.jpos.inventory.repository.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;
import com.jpos.inventory.model.ProductQuery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class FileProductRepositoryTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldLoadProductsFromCsv() throws Exception {
        File productFile = createFile("product.csv", """
                id,barcode,name,category
                00000000-0000-0000-0000-0050454d6969,045678901231,Lemons,fruit
                00000000-0000-0000-0000-008c5cb57a84,001234567890,Milk,dairy
                """);

        FileProductRepository repository = new FileProductRepository(productFile.toPath());

        Product[] products = repository.getProducts();
        Product lemons = repository.getProductBy(new ProductQuery(null, "045678901231"));

        assertEquals(2, products.length);
        assertNotNull(lemons);
        assertEquals("Lemons", lemons.getName());
    }

    @Test
    public void shouldPersistSavedProduct() throws Exception {
        File productFile = createFile("product.csv", """
                id,barcode,name,category
                00000000-0000-0000-0000-0050454d6969,045678901231,Lemons,fruit
                """);

        FileProductRepository repository = new FileProductRepository(productFile.toPath());
        Product product = new Product();
        product.setBarcode("123456789012");
        product.setName("Coffee");
        product.setCategory(ProductCategory.BEVERAGE);

        repository.saveProduct(product);

        FileProductRepository reloadedRepository = new FileProductRepository(productFile.toPath());
        Product savedProduct = reloadedRepository.getProductBy(new ProductQuery(null, "123456789012"));

        assertNotNull(savedProduct);
        assertEquals("Coffee", savedProduct.getName());
        assertNotNull(savedProduct.getId());
    }

    @Test
    public void shouldThrowWhenProductFileDoesNotExist() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new FileProductRepository(temporaryFolder.getRoot().toPath().resolve("missing-product.csv")));

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    private File createFile(String fileName, String content) throws Exception {
        File file = temporaryFolder.newFile(fileName);
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return file;
    }
}
