package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.AliasCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.AliasStorage;
import seedu.address.storage.Storage;

public class MainAppTest {

    private final MainApp mainApp = new MainApp();

    @AfterEach
    public void tearDown() {
        AliasCommand.getAliasRegistry().clear();
    }

    @Test
    public void initModelManager_validData_returnsModelWithData() throws DataLoadingException {
        AddressBook testBook = getTypicalAddressBook();
        Storage storage = new StubStorage(() -> Optional.of(testBook));
        Model model = mainApp.initModelManager(storage, new UserPrefs());
        assertEquals(testBook, new AddressBook(model.getAddressBook()));
    }

    @Test
    public void initModelManager_noDataFile_returnsSampleData() {
        Storage storage = new StubStorage(() -> Optional.empty());
        Model model = mainApp.initModelManager(storage, new UserPrefs());
        assertFalse(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void initModelManager_dataLoadingException_returnsEmptyModel() {
        Storage storage = new StubStorage(() -> {
            throw new DataLoadingException(new Exception("load fail"));
        });
        Model model = mainApp.initModelManager(storage, new UserPrefs());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void initAliases_validAliases_loadsIntoRegistry() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("ls", "list");

        AliasStorage storage = new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return null;
            }

            @Override
            public Optional<Map<String, String>> readAliases() {
                return Optional.of(aliases);
            }

            @Override
            public void saveAliases(Map<String, String> a) throws IOException {}
        };

        mainApp.initAliases(storage);

        assertEquals("list", AliasCommand.getAliasRegistry().getCommandWord("ls"));
    }

    @Test
    public void initAliases_invalidPersistedAliases_loadsOnlyValidEntries() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("ls", "list");
        aliases.put("list", "list");
        aliases.put("", "add");

        AliasStorage storage = new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return null;
            }

            @Override
            public Optional<Map<String, String>> readAliases() {
                return Optional.of(aliases);
            }

            @Override
            public void saveAliases(Map<String, String> a) throws IOException {}
        };

        assertDoesNotThrow(() -> mainApp.initAliases(storage));
        assertEquals("list", AliasCommand.getAliasRegistry().getCommandWord("ls"));
        assertNull(AliasCommand.getAliasRegistry().getCommandWord("list"));
        assertNull(AliasCommand.getAliasRegistry().getCommandWord(""));
    }

    @Test
    public void initAliases_emptyOptional_clearsRegistry() {
        AliasCommand.getAliasRegistry().addAlias("ls", "list", AliasCommand.RESERVED_COMMAND_WORDS);

        AliasStorage storage = new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return null;
            }

            @Override
            public Optional<Map<String, String>> readAliases() {
                return Optional.empty();
            }

            @Override
            public void saveAliases(Map<String, String> a) throws IOException {}
        };

        assertDoesNotThrow(() -> mainApp.initAliases(storage));
        assertTrue(AliasCommand.getAliasRegistry().getAllAliases().isEmpty());
    }

    @Test
    public void initAliases_dataLoadingException_clearsRegistryAndDoesNotThrow() {
        AliasCommand.getAliasRegistry().addAlias("ls", "list", AliasCommand.RESERVED_COMMAND_WORDS);

        AliasStorage storage = new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return null;
            }

            @Override
            public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                throw new DataLoadingException(new Exception("test error"));
            }

            @Override
            public void saveAliases(Map<String, String> a) throws IOException {}
        };

        assertDoesNotThrow(() -> mainApp.initAliases(storage));
        assertTrue(AliasCommand.getAliasRegistry().getAllAliases().isEmpty());
    }

    // ================ initAliases with legacy migration ==============================

    @Test
    public void initAliases_primaryEmpty_migratesFromLegacy() {
        Map<String, String> legacyAliases = new HashMap<>();
        legacyAliases.put("ls", "list");
        legacyAliases.put("a", "add");

        // Primary returns present but empty map
        AliasStorage primary = stubAliasStorage(Optional.of(new HashMap<>()), false);
        AliasStorage legacy = stubAliasStorage(Optional.of(legacyAliases), false);

        assertDoesNotThrow(() -> mainApp.initAliases(primary, legacy));

        assertEquals("list", AliasCommand.getAliasRegistry().getCommandWord("ls"));
        assertEquals("add", AliasCommand.getAliasRegistry().getCommandWord("a"));
    }

    @Test
    public void initAliases_legacyRejectedAliases_loadsValidOnly() {
        Map<String, String> legacyAliases = new HashMap<>();
        legacyAliases.put("ls", "list");
        // "list" clashes with reserved command word → should be rejected
        legacyAliases.put("list", "add");

        AliasStorage primary = stubAliasStorage(Optional.empty(), false);
        AliasStorage legacy = stubAliasStorage(Optional.of(legacyAliases), false);

        assertDoesNotThrow(() -> mainApp.initAliases(primary, legacy));

        assertEquals("list", AliasCommand.getAliasRegistry().getCommandWord("ls"));
        assertNull(AliasCommand.getAliasRegistry().getCommandWord("list"));
    }

    @Test
    public void initAliases_legacyEmpty_clearsRegistry() {
        AliasCommand.getAliasRegistry().addAlias("x", "exit", AliasCommand.RESERVED_COMMAND_WORDS);

        AliasStorage primary = stubAliasStorage(Optional.empty(), false);
        AliasStorage legacy = stubAliasStorage(Optional.of(new HashMap<>()), false);

        assertDoesNotThrow(() -> mainApp.initAliases(primary, legacy));

        assertTrue(AliasCommand.getAliasRegistry().getAllAliases().isEmpty());
    }

    @Test
    public void initAliases_legacyThrows_clearsRegistry() {
        AliasCommand.getAliasRegistry().addAlias("ls", "list", AliasCommand.RESERVED_COMMAND_WORDS);

        AliasStorage primary = stubAliasStorage(Optional.empty(), false);
        AliasStorage legacyThatThrows = new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return null;
            }
            @Override
            public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                throw new DataLoadingException(new Exception("legacy read error"));
            }
            @Override
            public void saveAliases(Map<String, String> a) throws IOException {}
        };

        assertDoesNotThrow(() -> mainApp.initAliases(primary, legacyThatThrows));

        assertTrue(AliasCommand.getAliasRegistry().getAllAliases().isEmpty());
    }

    @Test
    public void initAliases_migrationSaveFails_aliasesStillLoaded() {
        Map<String, String> legacyAliases = new HashMap<>();
        legacyAliases.put("ls", "list");

        // Primary throws on saveAliases but reads empty successfully
        AliasStorage primaryThatFailsSave = new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return null;
            }
            @Override
            public Optional<Map<String, String>> readAliases() {
                return Optional.empty();
            }
            @Override
            public void saveAliases(Map<String, String> a) throws IOException {
                throw new IOException("save failed");
            }
        };
        AliasStorage legacy = stubAliasStorage(Optional.of(legacyAliases), false);

        assertDoesNotThrow(() -> mainApp.initAliases(primaryThatFailsSave, legacy));

        // Aliases should still be in registry even though persistence failed
        assertEquals("list", AliasCommand.getAliasRegistry().getCommandWord("ls"));
    }

    @Test
    public void initAliases_primaryNonEmpty_doesNotMigrate() {
        Map<String, String> primaryAliases = new HashMap<>();
        primaryAliases.put("d", "delete");

        // Legacy returns aliases that differ - should NOT be loaded
        Map<String, String> legacyAliases = new HashMap<>();
        legacyAliases.put("ls", "list");

        AliasStorage primary = stubAliasStorage(Optional.of(primaryAliases), false);
        // legacy should not be read at all since primary is non-empty
        AliasStorage legacyThatShouldNotBeRead = new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return null;
            }
            @Override
            public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                throw new AssertionError("Legacy storage should not be read when primary has aliases");
            }
            @Override
            public void saveAliases(Map<String, String> a) throws IOException {}
        };

        assertDoesNotThrow(() -> mainApp.initAliases(primary, legacyThatShouldNotBeRead));

        assertEquals("delete", AliasCommand.getAliasRegistry().getCommandWord("d"));
        assertNull(AliasCommand.getAliasRegistry().getCommandWord("ls"));
    }

    // ================ helper factories ==============================

    private static AliasStorage stubAliasStorage(Optional<Map<String, String>> readResult,
            boolean throwOnSave) {
        return new AliasStorage() {
            @Override
            public Path getAliasesFilePath() {
                return null;
            }
            @Override
            public Optional<Map<String, String>> readAliases() {
                return readResult;
            }
            @Override
            public void saveAliases(Map<String, String> a) throws IOException {
                if (throwOnSave) {
                    throw new IOException("save error");
                }
            }
        };
    }

    // ================ StubStorage inner class ==============================

    /**
     * Minimal Storage stub used to test initModelManager in isolation.
     * Only getAddressBookFilePath() and readAddressBook() have functional behaviour;
     * all other methods are no-ops or return empty values.
     */
    private static class StubStorage implements Storage {

        interface AbReader {
            Optional<ReadOnlyAddressBook> read() throws DataLoadingException;
        }

        private final AbReader abReader;

        StubStorage(AbReader abReader) {
            this.abReader = abReader;
        }

        @Override
        public Path getAddressBookFilePath() {
            return Path.of("stub/ab.json");
        }

        @Override
        public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
            return abReader.read();
        }

        @Override
        public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
            return Optional.empty();
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        }

        @Override
        public Path getUserPrefsFilePath() {
            return null;
        }

        @Override
        public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
            return Optional.empty();
        }

        @Override
        public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        }

        @Override
        public Path getAliasesFilePath() {
            return null;
        }

        @Override
        public Optional<Map<String, String>> readAliases() throws DataLoadingException {
            return Optional.empty();
        }

        @Override
        public void saveAliases(Map<String, String> aliases) throws IOException {
        }

        @Override
        public void saveAll(ReadOnlyAddressBook addressBook, Map<String, String> aliases) throws IOException {
        }
    }

}
