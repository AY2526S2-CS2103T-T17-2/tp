package seedu.address.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/**
 * An Immutable PingBook that is serializable to JSON format.
 * Combines both person data and alias data into a single file.
 */
class JsonSerializablePingBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final Map<String, String> aliases;

    /**
     * Constructs a {@code JsonSerializablePingBook} with the given persons and aliases.
     */
    @JsonCreator
    public JsonSerializablePingBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("aliases") Map<String, String> aliases) {
        if (persons != null) {
            this.persons.addAll(persons);
        }
        this.aliases = (aliases != null) ? new HashMap<>(aliases) : new HashMap<>();
    }

    /**
     * Converts from model types into this class for Jackson use.
     */
    public JsonSerializablePingBook(ReadOnlyAddressBook source, Map<String, String> aliases) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        this.aliases = new HashMap<>(aliases == null ? Collections.emptyMap() : aliases);
    }

    public Map<String, String> getAliases() {
        return new HashMap<>(aliases);
    }

    /**
     * Converts this ping book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            boolean hasDuplicateIdentity = addressBook.getPersonList().stream()
                    .anyMatch(person::isSamePerson);
            if (hasDuplicateIdentity) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            addressBook.addPerson(person);
        }
        return addressBook;
    }

}
