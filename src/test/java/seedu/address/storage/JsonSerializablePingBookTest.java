package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializablePingBookTest {

    private static final java.nio.file.Path TEST_DATA_FOLDER =
            java.nio.file.Paths.get("src", "test", "data", "JsonSerializablePingBookTest");

    private static final java.nio.file.Path TYPICAL_FILE =
            TEST_DATA_FOLDER.resolve("typicalPersonsPingBook.json");
    private static final java.nio.file.Path INVALID_PERSON_FILE =
            TEST_DATA_FOLDER.resolve("invalidPersonPingBook.json");
    private static final java.nio.file.Path DUPLICATE_PERSON_FILE =
            TEST_DATA_FOLDER.resolve("duplicatePersonPingBook.json");
    private static final java.nio.file.Path NO_ALIASES_FIELD_FILE =
            TEST_DATA_FOLDER.resolve("noAliasesFieldPingBook.json");

    // ===================== toModelType =====================

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializablePingBook data = JsonUtil.readJsonFile(TYPICAL_FILE,
                JsonSerializablePingBook.class).get();
        AddressBook fromFile = data.toModelType();
        AddressBook expected = TypicalPersons.getTypicalAddressBook();
        assertEquals(expected, fromFile);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializablePingBook data = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializablePingBook.class).get();
        assertThrows(IllegalValueException.class, data::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializablePingBook data = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializablePingBook.class).get();
        assertThrows(IllegalValueException.class,
                JsonSerializablePingBook.MESSAGE_DUPLICATE_PERSON, data::toModelType);
    }

    // ===================== getAliases =====================

    @Test
    public void getAliases_typicalFile_returnsPersistedAliases() throws Exception {
        JsonSerializablePingBook data = JsonUtil.readJsonFile(TYPICAL_FILE,
                JsonSerializablePingBook.class).get();
        Map<String, String> aliases = data.getAliases();
        assertEquals("list", aliases.get("ls"));
        assertEquals("add", aliases.get("a"));
    }

    @Test
    public void getAliases_noAliasesField_returnsEmptyMap() throws Exception {
        JsonSerializablePingBook data = JsonUtil.readJsonFile(NO_ALIASES_FIELD_FILE,
                JsonSerializablePingBook.class).get();
        Map<String, String> aliases = data.getAliases();
        assertNotNull(aliases);
        assertEquals(new HashMap<>(), aliases);
    }

    @Test
    public void getAliases_returnsDefensiveCopy() throws Exception {
        JsonSerializablePingBook data = JsonUtil.readJsonFile(TYPICAL_FILE,
                JsonSerializablePingBook.class).get();
        Map<String, String> first = data.getAliases();
        first.put("mutation", "should-not-propagate");
        Map<String, String> second = data.getAliases();
        assertFalse(second.containsKey("mutation"));
    }

    // ===================== constructor from model =====================

    @Test
    public void constructor_fromModel_roundTrip() throws Exception {
        AddressBook original = getTypicalAddressBook();
        Map<String, String> aliases = new HashMap<>();
        aliases.put("ls", "list");

        JsonSerializablePingBook serialized = new JsonSerializablePingBook(original, aliases);

        assertEquals(original, serialized.toModelType());
        assertEquals(aliases, serialized.getAliases());
    }

    @Test
    public void constructor_fromModel_nullAliasesDefaultsToEmpty() throws Exception {
        // The Jackson @JsonCreator path passes null when field is absent
        JsonSerializablePingBook data = new JsonSerializablePingBook(
                (java.util.List<JsonAdaptedPerson>) null, null);
        assertEquals(new HashMap<>(), data.getAliases());
        assertEquals(new AddressBook(), data.toModelType());
    }

    @Test
    public void constructor_nullAliases_defaultsToEmpty() throws Exception {
        // The model-source constructor should handle null aliases gracefully
        AddressBook book = getTypicalAddressBook();
        JsonSerializablePingBook serialized = new JsonSerializablePingBook(book, null);
        assertEquals(new HashMap<>(), serialized.getAliases());
        assertEquals(book, serialized.toModelType());
    }

}
