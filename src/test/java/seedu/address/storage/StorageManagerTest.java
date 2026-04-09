package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
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
    public void getUserPrefsFilePath() {
        assertNotNull(storageManager.getUserPrefsFilePath());
    }

    @Test
    public void getAliasesFilePath() {
        assertNotNull(storageManager.getAliasesFilePath());
    }

    @Test
    public void constructor_nullAddressBookStorage_throwsNullPointerException() {
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        JsonAliasStorage aliasStorage = new JsonAliasStorage(getTempFilePath("aliases"));

        assertThrows(NullPointerException.class, () -> new StorageManager(null, userPrefsStorage, aliasStorage));
    }

    @Test
    public void constructor_nullUserPrefsStorage_throwsNullPointerException() {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(getTempFilePath("ab"));
        JsonAliasStorage aliasStorage = new JsonAliasStorage(getTempFilePath("aliases"));

        assertThrows(NullPointerException.class, () -> new StorageManager(addressBookStorage, null, aliasStorage));
    }

    @Test
    public void constructor_nullAliasStorage_throwsNullPointerException() {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(getTempFilePath("ab"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));

        assertThrows(NullPointerException.class, () -> new StorageManager(addressBookStorage, userPrefsStorage, null));
    }

    @Test
    public void readUserPrefs_userPrefsStorageThrows_propagatesDataLoadingException() {
        DataLoadingException expectedException = new DataLoadingException(new IOException("prefs read failure"));
        StorageManager failingManager = new StorageManager(new JsonAddressBookStorage(getTempFilePath("ab")),
                new UserPrefsStorage() {
                    @Override
                    public Path getUserPrefsFilePath() {
                        return getTempFilePath("prefs");
                    }

                    @Override
                    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
                        throw expectedException;
                    }

                    @Override
                    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) {
                    }
                }, new JsonAliasStorage(getTempFilePath("aliases")));

        DataLoadingException actualException = assertThrows(DataLoadingException.class, failingManager::readUserPrefs);
        assertEquals(expectedException, actualException);
    }

    @Test
    public void saveUserPrefs_userPrefsStorageThrows_propagatesIoException() {
        IOException expectedException = new IOException("prefs write failure");
        StorageManager failingManager = new StorageManager(new JsonAddressBookStorage(getTempFilePath("ab")),
                new UserPrefsStorage() {
                    @Override
                    public Path getUserPrefsFilePath() {
                        return getTempFilePath("prefs");
                    }

                    @Override
                    public Optional<UserPrefs> readUserPrefs() {
                        return Optional.empty();
                    }

                    @Override
                    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                        throw expectedException;
                    }
                }, new JsonAliasStorage(getTempFilePath("aliases")));

        IOException actualException = assertThrows(
            IOException.class, () -> failingManager.saveUserPrefs(new UserPrefs()));
        assertEquals(expectedException, actualException);
    }

    @Test
    public void readAddressBook_addressBookStorageThrows_propagatesDataLoadingException() {
        DataLoadingException expectedException = new DataLoadingException(new IOException("address book read failure"));
        StorageManager failingManager = new StorageManager(new AddressBookStorage() {
            @Override
            public Path getAddressBookFilePath() {
                return getTempFilePath("ab");
            }

            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                throw expectedException;
            }

            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
                throw expectedException;
            }

            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) {
            }

            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) {
            }
        },
                new JsonUserPrefsStorage(getTempFilePath("prefs")),
                new JsonAliasStorage(getTempFilePath("aliases")));

        DataLoadingException actualException = assertThrows(
                DataLoadingException.class, failingManager::readAddressBook);
        assertEquals(expectedException, actualException);
    }

    @Test
    public void saveAddressBook_addressBookStorageThrows_propagatesIoException() {
        IOException expectedException = new IOException("address book write failure");
        StorageManager failingManager = new StorageManager(new AddressBookStorage() {
            @Override
            public Path getAddressBookFilePath() {
                return getTempFilePath("ab");
            }

            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() {
                return Optional.empty();
            }

            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) {
                return Optional.empty();
            }

            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw expectedException;
            }

            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
                throw expectedException;
            }
        }, new JsonUserPrefsStorage(getTempFilePath("prefs")), new JsonAliasStorage(getTempFilePath("aliases")));

        IOException actualException = assertThrows(
            IOException.class, () -> failingManager.saveAddressBook(new AddressBook()));
        assertEquals(expectedException, actualException);
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

    @Test
    public void readAliases_aliasStorageThrows_propagatesDataLoadingException() {
        DataLoadingException expectedException = new DataLoadingException(new IOException("aliases read failure"));
        StorageManager failingManager = new StorageManager(new JsonAddressBookStorage(getTempFilePath("ab")),
                new JsonUserPrefsStorage(getTempFilePath("prefs")), new AliasStorage() {
                    @Override
                    public Path getAliasesFilePath() {
                        return getTempFilePath("aliases");
                    }

                    @Override
                    public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                        throw expectedException;
                    }

                    @Override
                    public void saveAliases(Map<String, String> aliases) {
                    }
                });

        DataLoadingException actualException = assertThrows(DataLoadingException.class, failingManager::readAliases);
        assertEquals(expectedException, actualException);
    }

    @Test
    public void saveAliases_aliasStorageThrows_propagatesIoException() {
        IOException expectedException = new IOException("aliases write failure");
        StorageManager failingManager = new StorageManager(new JsonAddressBookStorage(getTempFilePath("ab")),
                new JsonUserPrefsStorage(getTempFilePath("prefs")), new AliasStorage() {
                    @Override
                    public Path getAliasesFilePath() {
                        return getTempFilePath("aliases");
                    }

                    @Override
                    public Optional<Map<String, String>> readAliases() {
                        return Optional.empty();
                    }

                    @Override
                    public void saveAliases(Map<String, String> aliases) throws IOException {
                        throw expectedException;
                    }
                });

        IOException actualException = assertThrows(
            IOException.class, () -> failingManager.saveAliases(new HashMap<>()));
        assertEquals(expectedException, actualException);
    }

    // ================ saveAll tests ==============================

    @Test
    public void saveAll_sharedPathWithIncompatibleStorages_throwsIllegalStateException() {
        // Both AB and alias storage point to the same file, but at least one is NOT JsonPingBookStorage.
        Path sharedPath = getTempFilePath("shared_incompatible");
        JsonAddressBookStorage abStorage = new JsonAddressBookStorage(sharedPath);
        JsonAliasStorage aliasStorage = new JsonAliasStorage(sharedPath);
        StorageManager sm = new StorageManager(abStorage,
                new JsonUserPrefsStorage(getTempFilePath("prefs")), aliasStorage);

        assertThrows(IllegalStateException.class, () -> sm.saveAll(new AddressBook(), new HashMap<>()));
    }

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
    public void saveAll_nullAddressBook_throwsBeforeFileOperations() throws Exception {
        storageManager.saveAddressBook(new AddressBook());
        Map<String, String> aliases = new HashMap<>();
        aliases.put("ls", "list");
        storageManager.saveAliases(aliases);

        Path abPath = getTempFilePath("ab");
        Path aliasPath = getTempFilePath("aliases");

        assertThrows(NullPointerException.class, () -> storageManager.saveAll(null, aliases));

        assertFalse(Files.exists(abPath.resolveSibling(abPath.getFileName() + ".bak")));
        assertFalse(Files.exists(aliasPath.resolveSibling(aliasPath.getFileName() + ".bak")));
        assertEquals(new AddressBook(), new AddressBook(storageManager.readAddressBook()
                .orElseThrow(() -> new AssertionError("AddressBook file should remain unchanged"))));
        assertEquals(aliases, storageManager.readAliases()
                .orElseThrow(() -> new AssertionError("Aliases file should remain unchanged")));
    }

    @Test
    public void saveAll_nullAliases_throwsBeforeFileOperations() throws Exception {
        AddressBook book = getTypicalAddressBook();
        Map<String, String> existingAliases = new HashMap<>();
        existingAliases.put("ls", "list");
        storageManager.saveAddressBook(book);
        storageManager.saveAliases(existingAliases);

        Path abPath = getTempFilePath("ab");
        Path aliasPath = getTempFilePath("aliases");

        assertThrows(NullPointerException.class, () -> storageManager.saveAll(book, null));

        assertFalse(Files.exists(abPath.resolveSibling(abPath.getFileName() + ".bak")));
        assertFalse(Files.exists(aliasPath.resolveSibling(aliasPath.getFileName() + ".bak")));
        assertEquals(book, new AddressBook(storageManager.readAddressBook()
                .orElseThrow(() -> new AssertionError("AddressBook file should remain unchanged"))));
        assertEquals(existingAliases, storageManager.readAliases()
                .orElseThrow(() -> new AssertionError("Aliases file should remain unchanged")));
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
        JsonAliasStorage aliasStorage = new JsonAliasStorage(aliasPath);
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        AliasStorage failingAlias = new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return aliasPath;
            }

            @Override
            public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                return aliasStorage.readAliases();
            }

            @Override
            public void saveAliases(Map<String, String> aliases) throws IOException {
                aliasStorage.saveAliases(aliases);
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

        Map<String, String> restoredAliases = aliasStorage.readAliases()
            .orElseThrow(() -> new AssertionError("Aliases file should still exist"));
        assertEquals(originalAliases, restoredAliases);

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

    @Test
    public void saveAll_aliasesThrowsWithExistingFilesAndRestoreFails_addsSuppressedExceptions() throws Exception {
        assumePosixPermissionsSupported();

        AddressBook originalBook = new AddressBook();
        Map<String, String> originalAliases = new HashMap<>();
        originalAliases.put("orig", "list");
        storageManager.saveAddressBook(originalBook);
        storageManager.saveAliases(originalAliases);

        Path abPath = getTempFilePath("ab");
        Path aliasPath = getTempFilePath("aliases");
        Path abBackupPath = abPath.resolveSibling(abPath.getFileName() + ".bak");
        Path aliasBackupPath = aliasPath.resolveSibling(aliasPath.getFileName() + ".bak");
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(testFolder);

        JsonAddressBookStorage abStorage = new JsonAddressBookStorage(abPath);
        JsonAliasStorage aliasStorage = new JsonAliasStorage(aliasPath);
        StorageManager failingManager = new StorageManager(abStorage,
                new JsonUserPrefsStorage(getTempFilePath("prefs")), new AliasStorage() {
                    @Override
                    public Path getAliasesFilePath() {
                        return aliasPath;
                    }

                    @Override
                    public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                        return aliasStorage.readAliases();
                    }

                    @Override
                    public void saveAliases(Map<String, String> aliases) throws IOException {
                        aliasStorage.saveAliases(aliases);
                        setDirectoryReadOnly(testFolder);
                        throw new IOException("alias disk full");
                    }
                });

        try {
            IOException thrownException = assertThrows(
                IOException.class, () -> failingManager.saveAll(getTypicalAddressBook(), new HashMap<>()));

            assertEquals("alias disk full", thrownException.getMessage());
            assertEquals(2, thrownException.getSuppressed().length);
            assertTrue(Files.exists(abBackupPath));
            assertTrue(Files.exists(aliasBackupPath));
        } finally {
            restoreDirectoryPermissions(testFolder, originalPermissions);
        }
    }

    @Test
    public void saveAll_aliasesThrowsWithNoExistingFilesAndCleanupFails_addsSuppressedExceptions() throws Exception {
        assumePosixPermissionsSupported();

        Path abPath = getTempFilePath("ab_cleanup_fail");
        Path aliasPath = getTempFilePath("aliases_cleanup_fail");
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(testFolder);

        JsonAddressBookStorage abStorage = new JsonAddressBookStorage(abPath);
        JsonAliasStorage aliasStorage = new JsonAliasStorage(aliasPath);
        StorageManager failingManager = new StorageManager(abStorage,
                new JsonUserPrefsStorage(getTempFilePath("prefs")), new AliasStorage() {
                    @Override
                    public Path getAliasesFilePath() {
                        return aliasPath;
                    }

                    @Override
                    public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                        return aliasStorage.readAliases();
                    }

                    @Override
                    public void saveAliases(Map<String, String> aliases) throws IOException {
                        aliasStorage.saveAliases(aliases);
                        setDirectoryReadOnly(testFolder);
                        throw new IOException("alias disk full");
                    }
                });

        try {
            IOException thrownException = assertThrows(
                IOException.class, () -> failingManager.saveAll(getTypicalAddressBook(), new HashMap<>()));

            assertEquals("alias disk full", thrownException.getMessage());
            assertEquals(2, thrownException.getSuppressed().length);
        } finally {
            restoreDirectoryPermissions(testFolder, originalPermissions);
        }

        assertTrue(Files.exists(abPath));
        assertTrue(Files.exists(aliasPath));
    }

    @Test
    public void saveAll_successfulSaveAndBackupDeletionFails_doesNotThrow() throws Exception {
        assumePosixPermissionsSupported();

        storageManager.saveAddressBook(new AddressBook());
        storageManager.saveAliases(new HashMap<>());

        Path abPath = getTempFilePath("ab");
        Path aliasPath = getTempFilePath("aliases");
        Path abBackupPath = abPath.resolveSibling(abPath.getFileName() + ".bak");
        Path aliasBackupPath = aliasPath.resolveSibling(aliasPath.getFileName() + ".bak");
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(testFolder);

        JsonAliasStorage aliasStorage = new JsonAliasStorage(aliasPath);
        StorageManager saveSucceedsButCleanupFails = new StorageManager(new JsonAddressBookStorage(abPath),
                new JsonUserPrefsStorage(getTempFilePath("prefs")), new AliasStorage() {
                    @Override
                    public Path getAliasesFilePath() {
                        return aliasPath;
                    }

                    @Override
                    public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                        return aliasStorage.readAliases();
                    }

                    @Override
                    public void saveAliases(Map<String, String> aliases) throws IOException {
                        aliasStorage.saveAliases(aliases);
                        setDirectoryReadOnly(testFolder);
                    }
                });

        try {
            saveSucceedsButCleanupFails.saveAll(getTypicalAddressBook(), new HashMap<>());
        } finally {
            restoreDirectoryPermissions(testFolder, originalPermissions);
        }

        assertTrue(Files.exists(abBackupPath));
        assertTrue(Files.exists(aliasBackupPath));
    }

    // ================ saveAll – shared JsonPingBookStorage (combined write path) ==============================

    /** Helper: builds a StorageManager backed by the same JsonPingBookStorage for both AB and alias. */
    private StorageManager sharedPingBookStorageManager(JsonPingBookStorage ping) {
        return new StorageManager(ping, new JsonUserPrefsStorage(getTempFilePath("prefs")), ping);
    }

    @Test
    public void saveAll_sharedStorage_savesBoth() throws Exception {
        Path path = getTempFilePath("ping_shared.json");
        JsonPingBookStorage ping = new JsonPingBookStorage(path);
        StorageManager sm = sharedPingBookStorageManager(ping);

        AddressBook book = getTypicalAddressBook();
        Map<String, String> aliases = new HashMap<>();
        aliases.put("ls", "list");

        sm.saveAll(book, aliases);

        assertEquals(book, new AddressBook(ping.readAddressBook().get()));
        assertEquals(aliases, ping.readAliases().get());
        assertFalse(Files.exists(path.resolveSibling(path.getFileName() + ".bak")));
    }

    @Test
    public void saveAll_sharedStorageExisting_savesAndDeletesBackup() throws Exception {
        Path path = getTempFilePath("ping_shared2.json");
        JsonPingBookStorage ping = new JsonPingBookStorage(path);
        // Pre-populate so a backup is created during saveAll
        ping.saveAll(new AddressBook(), new HashMap<>());

        StorageManager sm = sharedPingBookStorageManager(ping);
        AddressBook book = getTypicalAddressBook();
        Map<String, String> aliases = new HashMap<>();
        aliases.put("a", "add");

        sm.saveAll(book, aliases);

        assertEquals(book, new AddressBook(ping.readAddressBook().get()));
        assertEquals(aliases, ping.readAliases().get());
        // Backup should be cleaned up after successful save
        assertFalse(Files.exists(path.resolveSibling(path.getFileName() + ".bak")));
    }

    @Test
    public void saveAll_sharedStorageSaveFails_restoresBackup() throws Exception {
        Path path = getTempFilePath("ping_fail_existing.json");
        JsonPingBookStorage realPing = new JsonPingBookStorage(path);

        // Pre-populate original data
        AddressBook originalBook = getTypicalAddressBook();
        Map<String, String> originalAliases = new HashMap<>();
        originalAliases.put("orig", "list");
        realPing.saveAll(originalBook, originalAliases);

        // Create a failing variant that throws on the combined saveAll
        IOException toThrow = new IOException("simulated disk full");
        JsonPingBookStorage failingPing = new JsonPingBookStorage(path) {
            @Override
            public void saveAll(ReadOnlyAddressBook ab, java.util.Map<String, String> al) throws IOException {
                throw toThrow;
            }
        };
        StorageManager sm = sharedPingBookStorageManager(failingPing);

        assertThrows(IOException.class, () -> sm.saveAll(new AddressBook(), new HashMap<>()));

        // Original file should be restored; backup cleaned up
        assertFalse(Files.exists(path.resolveSibling(path.getFileName() + ".bak")));
        assertEquals(originalBook, new AddressBook(realPing.readAddressBook().get()));
        assertEquals(originalAliases, realPing.readAliases().get());
    }

    @Test
    public void saveAll_sharedStorageNewFile_throwsNoLeftovers() throws Exception {
        Path path = getTempFilePath("ping_fail_new.json");
        // File must not exist before the save attempt
        assertFalse(Files.exists(path));

        IOException toThrow = new IOException("simulated disk full");
        JsonPingBookStorage failingPing = new JsonPingBookStorage(path) {
            @Override
            public void saveAll(ReadOnlyAddressBook ab, java.util.Map<String, String> al) throws IOException {
                throw toThrow;
            }
        };
        StorageManager sm = sharedPingBookStorageManager(failingPing);

        assertThrows(IOException.class, () -> sm.saveAll(new AddressBook(), new HashMap<>()));

        // No partial file should remain
        assertFalse(Files.exists(path));
        assertFalse(Files.exists(path.resolveSibling(path.getFileName() + ".bak")));
    }

    // ================ helpers ==============================

    private void assumePosixPermissionsSupported() throws IOException {
        Assumptions.assumeTrue(Files.getFileStore(testFolder).supportsFileAttributeView("posix"));
    }

    private void setDirectoryReadOnly(Path directory) throws IOException {
        Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("r-xr-xr-x"));
    }

    private void restoreDirectoryPermissions(Path directory, Set<PosixFilePermission> permissions) throws IOException {
        Files.setPosixFilePermissions(directory, permissions);
    }

    // ================ FailingAliasStorage inner class ==============================

}
