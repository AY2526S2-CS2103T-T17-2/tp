package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * API of the Storage component
 */
public interface Storage extends AddressBookStorage, UserPrefsStorage, AliasStorage {

    @Override
    Optional<UserPrefs> readUserPrefs() throws DataLoadingException;

    @Override
    void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException;

    @Override
    Path getAddressBookFilePath();

    @Override
    Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException;

    @Override
    void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException;

    /**
     * Saves both the address book and alias map together.
     * Implementers are responsible for atomicity: a failure after a partial write
     * should not leave the two files in an inconsistent state.
     */
    void saveAll(ReadOnlyAddressBook addressBook, Map<String, String> aliases) throws IOException;

}
