package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.AliasCommand;
import seedu.address.storage.AliasStorage;

public class MainAppTest {

    private final MainApp mainApp = new MainApp();

    @AfterEach
    public void tearDown() {
        AliasCommand.getAliasRegistry().clear();
    }

    @Test
    public void initAliases_validAliases_loadsIntoRegistry() throws DataLoadingException {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("ls", "list");

        AliasStorage storage = new AliasStorage() {
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
    public void initAliases_emptyOptional_registryUnchanged() {
        AliasStorage storage = new AliasStorage() {
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
    public void initAliases_dataLoadingException_doesNotThrow() {
        AliasStorage storage = new AliasStorage() {
            @Override
            public Optional<Map<String, String>> readAliases() throws DataLoadingException {
                throw new DataLoadingException(new Exception("test error"));
            }

            @Override
            public void saveAliases(Map<String, String> a) throws IOException {}
        };

        assertDoesNotThrow(() -> mainApp.initAliases(storage));
    }

}
