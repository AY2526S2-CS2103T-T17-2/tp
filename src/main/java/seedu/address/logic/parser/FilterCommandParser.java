package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.TagContainsKeywordsPredicate;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new FilterCommand object.
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FilterCommand
     * and returns a FilterCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FilterCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG);

        List<String> rawTagValues = argMultimap.getAllValues(PREFIX_TAG);
        List<String> tagKeywords = new ArrayList<>();

        for (String rawTag : rawTagValues) {
            String trimmedTag = rawTag.trim();
            if (trimmedTag.isEmpty() || !Tag.isValidTagName(trimmedTag)) {
                throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
            }
            tagKeywords.add(trimmedTag);
        }

        if (tagKeywords.isEmpty() || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        return new FilterCommand(new TagContainsKeywordsPredicate(tagKeywords));
    }
}

