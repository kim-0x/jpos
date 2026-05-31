package com.jpos.user.repository.implementation;

import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.file.DatUserRepository;
import com.jpos.user.utils.UserBuilder;
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

public class DatUserRepositoryTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldLoadUsersFromDat() throws Exception {
        File userFile = temporaryFolder.newFile("users.dat");
        writeUsers(userFile, List.of(
                createUser("00000000-0000-0000-0000-0085374aeb72", "admin", "gjsot", "admin"),
                createUser("00000000-0000-0000-0000-00a12b34c56d", "cashier", "password", "cashier")));

        UserRepository repository = new DatUserRepository(userFile.toPath());

        assertEquals(2, repository.getUsers().length);
        assertTrue(repository.validUser("admin", "gjsot"));
        assertNotNull(repository.getUserLogin("cashier"));
    }

    @Test
    public void shouldBootstrapDefaultAdminWhenDatIsEmpty() throws Exception {
        File userFile = temporaryFolder.newFile("users.dat");
        Files.write(userFile.toPath(), new byte[0]);

        UserRepository repository = new DatUserRepository(userFile.toPath());

        assertEquals(1, repository.getUsers().length);
        assertTrue(repository.validUser("admin", "admin"));
    }

    @Test
    public void shouldPersistAddedUser() throws Exception {
        File userFile = temporaryFolder.newFile("users.dat");
        Files.write(userFile.toPath(), new byte[0]);

        UserRepository repository = new DatUserRepository(userFile.toPath());
        repository.addUser("cashier", "password", "cashier");

        UserRepository reloadedRepository = new DatUserRepository(userFile.toPath());

        assertEquals(2, reloadedRepository.getUsers().length);
        assertTrue(reloadedRepository.validUser("admin", "admin"));
        assertTrue(reloadedRepository.validUser("cashier", "password"));
    }

    @Test
    public void shouldThrowWhenUserDatFileDoesNotExist() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new DatUserRepository(temporaryFolder.getRoot().toPath().resolve("missing-users.dat")));

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    private User createUser(String id, String username, String password, String role) {
        User user = UserBuilder.createUser(username, password, role);
        user.setId(UUID.fromString(id));
        return user;
    }

    private void writeUsers(File targetFile, List<User> users) throws Exception {
        ArrayList<User> serializedUsers = new ArrayList<>(users);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(targetFile.toPath()))) {
            objectOutputStream.writeObject(serializedUsers);
        }
    }
}
