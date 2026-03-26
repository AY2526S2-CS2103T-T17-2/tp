package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;

/**
 * Stores the alias registry as a JSON file on disk.
 */
public class JsonAliasStorage implements AliasStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAliasStorage.class);

    private final Path filePath;

    /**
     * Creates a {@code JsonAliasStorage} with the given file path.
     */
    public JsonAliasStorage(Path filePath) {
        requireNonNull(filePath);
        this.filePath = filePath;
    }

    @Override
    public Path getAliasesFilePath() {
        return filePath;
    }

    @Override
    public Optional<Map<String, String>> readAliases() throws DataLoadingException {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        logger.fine("Reading aliases from " + filePath);
        return JsonUtil.readJsonFile(filePath, JsonSerializableAliasRegistry.class)
                .map(JsonSerializableAliasRegistry::toAliasMap);
    }

    @Override
    public void saveAliases(Map<String, String> aliases) throws IOException {
        requireNonNull(aliases);
        logger.fine("Saving aliases to " + filePath);
        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableAliasRegistry(aliases), filePath);
    }

}
