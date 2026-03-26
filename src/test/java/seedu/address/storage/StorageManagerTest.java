package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;

public class StorageManagerTest {

    @TempDir
    public Path testFolder;

    private StorageManager storageManager;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(getTempFilePath("ab"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        JsonAliasStorage aliasStorage = new JsonAliasStorage(getTempFilePath("aliases"));
        storageManager = new StorageManager(addressBookStorage, userPrefsStorage, aliasStorage);
    }

    private Path getTempFilePath(String fileName) {
        return testFolder.resolve(fileName);
    }

    @Test
    public void prefsReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonUserPrefsStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonUserPrefsStorageTest} class.
         */
        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(300, 600, 4, 6));
        storageManager.saveUserPrefs(original);
        UserPrefs retrieved = storageManager.readUserPrefs().get();
        assertEquals(original, retrieved);
    }

    @Test
    public void addressBookReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonAddressBookStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonAddressBookStorageTest} class.
         */
        AddressBook original = getTypicalAddressBook();
        storageManager.saveAddressBook(original);
        ReadOnlyAddressBook retrieved = storageManager.readAddressBook().get();
        assertEquals(original, new AddressBook(retrieved));
    }

    @Test
    public void getAddressBookFilePath() {
        assertNotNull(storageManager.getAddressBookFilePath());
    }

    @Test
    public void aliasReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonAliasStorage} class.
         * More extensive testing of alias saving/reading is done in {@link JsonAliasStorageTest} class.
         */
        Map<String, String> aliases = new HashMap<>();
        aliases.put("ls", "list");
        aliases.put("a", "add");

        storageManager.saveAliases(aliases);
        Map<String, String> retrieved = storageManager.readAliases()
                .orElseThrow(() -> new AssertionError("Aliases file should exist after save"));
        assertEquals(aliases, retrieved);
    }

    // ================ saveAll tests ==============================

    @Test
    public void saveAll_noExistingFiles_savesBothSuccessfully() throws Exception {
        AddressBook book = getTypicalAddressBook();
        Map<String, String> aliases = new HashMap<>();
        aliases.put("ls", "list");

        storageManager.saveAll(book, aliases);

        ReadOnlyAddressBook savedBook = storageManager.readAddressBook()
                .orElseThrow(() -> new AssertionError("AddressBook should exist after saveAll"));
        assertEquals(book, new AddressBook(savedBook));

        Map<String, String> savedAliases = storageManager.readAliases()
                .orElseThrow(() -> new AssertionError("Aliases should exist after saveAll"));
        assertEquals(aliases, savedAliases);

        assertFalse(Files.exists(testFolder.resolve("ab.bak")));
        assertFalse(Files.exists(testFolder.resolve("aliases.bak")));
    }

    @Test
    public void saveAll_existingFiles_successfulSaveDeletesBackups() throws Exception {
        // Pre-populate both files
        storageManager.saveAddressBook(new AddressBook());
        storageManager.saveAliases(new HashMap<>());

        AddressBook newBook = getTypicalAddressBook();
        Map<String, String> newAliases = new HashMap<>();
        newAliases.put("a", "add");

        storageManager.saveAll(newBook, newAliases);

        ReadOnlyAddressBook savedBook = storageManager.readAddressBook()
                .orElseThrow(() -> new AssertionError("AddressBook should exist after saveAll"));
        assertEquals(newBook, new AddressBook(savedBook));

        Map<String, String> savedAliases = storageManager.readAliases()
                .orElseThrow(() -> new AssertionError("Aliases should exist after saveAll"));
        assertEquals(newAliases, savedAliases);

        assertFalse(Files.exists(testFolder.resolve("ab.bak")));
        assertFalse(Files.exists(testFolder.resolve("aliases.bak")));
    }

    @Test
    public void saveAll_aliasesThrowsWithExistingFiles_restoresBoth() throws Exception {
        // Pre-populate both files with original data
        AddressBook originalBook = new AddressBook();
        Map<String, String> originalAliases = new HashMap<>();
        originalAliases.put("orig", "list");
        storageManager.saveAddressBook(originalBook);
        storageManager.saveAliases(originalAliases);

        // Build a StorageManager whose alias storage always throws on save
        Path abPath = getTempFilePath("ab");
        Path aliasPath = getTempFilePath("aliases");
        JsonAddressBookStorage abStorage = new JsonAddressBookStorage(abPath);
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        AliasStorage failingAlias = new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return aliasPath;
            }

            @Override
            public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                return Optional.empty();
            }

            @Override
            public void saveAliases(Map<String, String> aliases) throws IOException {
                throw new IOException("alias disk full");
            }
        };
        StorageManager failingManager = new StorageManager(abStorage, prefs, failingAlias);

        assertThrows(IOException.class, () ->
                failingManager.saveAll(getTypicalAddressBook(), new HashMap<>()));

        // Address book should be restored to original empty book
        ReadOnlyAddressBook restoredBook = abStorage.readAddressBook()
                .orElseThrow(() -> new AssertionError("AddressBook file should still exist"));
        assertEquals(originalBook, new AddressBook(restoredBook));

        // No backup files should linger
        assertFalse(Files.exists(abPath.resolveSibling(abPath.getFileName() + ".bak")));
        assertFalse(Files.exists(aliasPath.resolveSibling(aliasPath.getFileName() + ".bak")));
    }

    @Test
    public void saveAll_abThrowsWithNoExistingFiles_noLeftovers() throws Exception {
        Path abPath = getTempFilePath("ab_fail");
        Path aliasPath = getTempFilePath("aliases_fail");
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        JsonAliasStorage aliasStorage = new JsonAliasStorage(aliasPath);
        AddressBookStorage failingAbStorage = new AddressBookStorage() {
            @Override
            public Path getAddressBookFilePath() {
                return abPath;
            }

            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                return Optional.empty();
            }

            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
                return Optional.empty();
            }

            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("ab disk full");
            }

            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
                throw new IOException("ab disk full");
            }
        };
        StorageManager failingManager = new StorageManager(failingAbStorage, prefs, aliasStorage);

        assertThrows(IOException.class, () ->
                failingManager.saveAll(new AddressBook(), new HashMap<>()));

        assertFalse(Files.exists(abPath));
        assertFalse(Files.exists(abPath.resolveSibling(abPath.getFileName() + ".bak")));
        assertFalse(Files.exists(aliasPath));
        assertFalse(Files.exists(aliasPath.resolveSibling(aliasPath.getFileName() + ".bak")));
    }

    // ================ FailingAliasStorage inner class ==============================

}
