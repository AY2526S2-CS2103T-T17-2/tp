package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class UnarchiveCommandTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_validIndexUnfilteredArchivedList_success() {
        Person activePerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person archivedPerson = activePerson.withArchived(true);
        model.setPerson(activePerson, archivedPerson);
        model.setViewPredicate(Model.PREDICATE_SHOW_ARCHIVED_PERSONS);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ARCHIVED_PERSONS);

        UnarchiveCommand unarchiveCommand = new UnarchiveCommand(INDEX_FIRST_PERSON);
        Person unarchivedPerson = archivedPerson.withArchived(false);

        String expectedMessage = String.format(UnarchiveCommand.MESSAGE_UNARCHIVE_PERSON_SUCCESS,
                Messages.format(unarchivedPerson));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(archivedPerson, unarchivedPerson);
        expectedModel.setViewPredicate(Model.PREDICATE_SHOW_ARCHIVED_PERSONS);
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_ARCHIVED_PERSONS);

        assertCommandSuccess(unarchiveCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Person activePerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person archivedPerson = activePerson.withArchived(true);
        model.setPerson(activePerson, archivedPerson);
        model.setViewPredicate(Model.PREDICATE_SHOW_ARCHIVED_PERSONS);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ARCHIVED_PERSONS);

        UnarchiveCommand unarchiveCommand = new UnarchiveCommand(INDEX_SECOND_PERSON);

        assertCommandFailure(unarchiveCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_activeView_throwsCommandExceptionWithListArchivedGuidance() {
        UnarchiveCommand unarchiveCommand = new UnarchiveCommand(INDEX_FIRST_PERSON);
        assertCommandFailure(unarchiveCommand, model,
                UnarchiveCommand.MESSAGE_NO_ARCHIVED_CONTACTS_SHOWN);
    }

    @Test
    public void execute_alreadyActive_throwsCommandException() {
        model.setViewPredicate(Model.PREDICATE_SHOW_ARCHIVED_PERSONS);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        UnarchiveCommand unarchiveCommand = new UnarchiveCommand(INDEX_FIRST_PERSON);

        assertCommandFailure(unarchiveCommand, model, UnarchiveCommand.MESSAGE_PERSON_ALREADY_ACTIVE);
    }

    @Test
    public void equals() {
        UnarchiveCommand unarchiveFirstCommand = new UnarchiveCommand(INDEX_FIRST_PERSON);
        UnarchiveCommand unarchiveSecondCommand = new UnarchiveCommand(INDEX_SECOND_PERSON);

        assertTrue(unarchiveFirstCommand.equals(unarchiveFirstCommand));
        assertTrue(unarchiveFirstCommand.equals(new UnarchiveCommand(INDEX_FIRST_PERSON)));

        assertFalse(unarchiveFirstCommand.equals(1));
        assertFalse(unarchiveFirstCommand.equals(null));
        assertFalse(unarchiveFirstCommand.equals(unarchiveSecondCommand));
    }
}
