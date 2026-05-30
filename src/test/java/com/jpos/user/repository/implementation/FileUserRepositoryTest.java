package com.jpos.user.repository.implementation;

import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.file.CsvUserRepository;
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

public class FileUserRepositoryTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldLoadUsersFromCsv() throws Exception {
        File userFile = createFile("user.csv", """
                id,name,password,role
                00000000-0000-0000-0000-0085374aeb72,admin,gjsot,admin
                00000000-0000-0000-0000-00a12b34c56d,cashier,password,cashier
                """);

        UserRepository repository = new CsvUserRepository(userFile.toPath());
        User[] users = repository.getUsers();

        assertEquals(2, users.length);
        assertTrue(repository.validUser("admin", "gjsot"));
        assertNotNull(repository.getUserLogin("cashier"));
    }

    @Test
    public void shouldPersistAddedUser() throws Exception {
        File userFile = createFile("user.csv", """
                id,name,password,role
                00000000-0000-0000-0000-0085374aeb72,admin,gjsot,admin
                """);

        UserRepository repository = new CsvUserRepository(userFile.toPath());
        repository.addUser("cashier", "password", "cashier");

        UserRepository reloadedRepository = new CsvUserRepository(userFile.toPath());

        assertEquals(2, reloadedRepository.getUsers().length);
        assertTrue(reloadedRepository.validUser("cashier", "password"));
    }

    @Test
    public void shouldThrowWhenUserFileDoesNotExist() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new CsvUserRepository(temporaryFolder.getRoot().toPath().resolve("missing-user.csv")));

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    private File createFile(String fileName, String content) throws Exception {
        File file = temporaryFolder.newFile(fileName);
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return file;
    }
}
