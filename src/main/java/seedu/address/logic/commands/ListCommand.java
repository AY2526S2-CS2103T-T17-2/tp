package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PERSONS;

import java.util.Objects;

import seedu.address.model.Model;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String ARCHIVED_COMMAND_WORD = "listarchived";

    public static final String MESSAGE_SUCCESS = "Listed active contacts";
    public static final String MESSAGE_ARCHIVED_SUCCESS = "Listed archived contacts";

    private final boolean showArchived;

    public ListCommand() {
        this(false);
    }

    public ListCommand(boolean showArchived) {
        this.showArchived = showArchived;
    }

    /** Returns whether this command lists archived contacts. */
    public boolean isShowArchived() {
        return showArchived;
    }


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (showArchived) {
            model.updateFilteredPersonList(person -> person.isArchived());
            return new CommandResult(MESSAGE_ARCHIVED_SUCCESS);
        }

        model.updateFilteredPersonList(PREDICATE_SHOW_ACTIVE_PERSONS);
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ListCommand)) {
            return false;
        }

        ListCommand otherCommand = (ListCommand) other;
        return showArchived == otherCommand.showArchived;
    }

    @Override
    public int hashCode() {
        return Objects.hash(showArchived);
    }
}
