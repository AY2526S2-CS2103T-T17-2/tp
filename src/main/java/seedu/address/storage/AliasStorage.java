package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;

/**
 * Represents a storage for command aliases.
 */
public interface AliasStorage {

    /**
     * Returns the file path of the aliases data file.
     */
    Path getAliasesFilePath();

    /**
     * Returns the alias map loaded from storage.
     * Returns {@code Optional.empty()} if the storage file is not found.
     *
     * @throws DataLoadingException if loading failed.
     */
    Optional<Map<String, String>> readAliases() throws DataLoadingException;

    /**
     * Saves the given alias map to storage.
     *
     * @throws IOException if there was any problem writing to the file.
     */
    void saveAliases(Map<String, String> aliases) throws IOException;

}
