package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * An UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final double MIN_TEXT_CONTRAST_RATIO = 4.5;
    private static final String BLACK_TEXT = "#000000";
    private static final String WHITE_TEXT = "#FFFFFF";
    private static final String PHONE_GLYPH = "\u260E  ";
    private static final String ADDRESS_GLYPH = "\u2302  ";
    private static final String EMAIL_GLYPH = "\u2709  ";
    private static final String REMARK_GLYPH = "\u270E  ";

    private static final String[] TAG_BG_COLORS = {
        "#e8eaff", "#dff5e3", "#fde8e8", "#fff4de", "#e4f3f8",
        "#f3e8fa", "#ffeee4", "#e2f0e9", "#fce4ec", "#e8f5e9"
    };

    private static final String[] TAG_TEXT_COLORS = {
        "#4f6df5", "#2d8a4e", "#d04040", "#b8860b", "#2980b9",
        "#7b3fa0", "#c0652a", "#3d7c5f", "#c62860", "#388e3c"
    };


    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */
    @FXML
    private GridPane cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private FlowPane tags;
    @FXML
    private Label remark;
    @FXML
    private Label starredIndicator;

    /**
     * Creates a {@code PersonCode} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        update(person, displayedIndex);
    }

    /**
     * Updates this card to display the given {@code Person} and index.
     */
    public void update(Person person, int displayedIndex) {
        tags.getChildren().clear();
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(PHONE_GLYPH + person.getPhone().value);
        boolean hasAddress = person.hasAddress();
        address.setText(hasAddress ? ADDRESS_GLYPH + person.getAddress().value : "");
        address.setManaged(hasAddress);
        address.setVisible(hasAddress);
        email.setText(EMAIL_GLYPH + person.getEmail().value);
        String remarkValue = person.getRemark().value;
        boolean hasRemark = !remarkValue.isEmpty();
        remark.setText(hasRemark ? REMARK_GLYPH + remarkValue : "");
        remark.setManaged(hasRemark);
        remark.setVisible(hasRemark);
        boolean isStarred = person.isStarred();
        starredIndicator.setVisible(isStarred);
        starredIndicator.setManaged(isStarred);
        boolean hasTags = !person.getTags().isEmpty();
        tags.setManaged(hasTags);
        tags.setVisible(hasTags);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> {
                    Label tagLabel = new Label(tag.tagName);
                    int idx = Math.floorMod(tag.tagName.hashCode(), TAG_BG_COLORS.length);
                    String backgroundColor = TAG_BG_COLORS[idx];
                    String textColor = resolveAccessibleTextColor(backgroundColor, TAG_TEXT_COLORS[idx]);
                    tagLabel.setStyle("-fx-background-color: " + backgroundColor
                            + "; -fx-text-fill: " + textColor + ";");
                    tags.getChildren().add(tagLabel);
                });
    }

    private static String resolveAccessibleTextColor(String backgroundColor, String preferredTextColor) {
        if (calculateContrastRatio(backgroundColor, preferredTextColor) >= MIN_TEXT_CONTRAST_RATIO) {
            return preferredTextColor;
        }

        double blackContrast = calculateContrastRatio(backgroundColor, BLACK_TEXT);
        double whiteContrast = calculateContrastRatio(backgroundColor, WHITE_TEXT);
        return blackContrast >= whiteContrast ? BLACK_TEXT : WHITE_TEXT;
    }

    private static double calculateContrastRatio(String backgroundColor, String textColor) {
        double backgroundLuminance = calculateRelativeLuminance(backgroundColor);
        double textLuminance = calculateRelativeLuminance(textColor);
        double lighter = Math.max(backgroundLuminance, textLuminance);
        double darker = Math.min(backgroundLuminance, textLuminance);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double calculateRelativeLuminance(String hexColor) {
        validateHexColor(hexColor);
        int red = Integer.parseInt(hexColor.substring(1, 3), 16);
        int green = Integer.parseInt(hexColor.substring(3, 5), 16);
        int blue = Integer.parseInt(hexColor.substring(5, 7), 16);
        return 0.2126 * toLinearChannel(red) + 0.7152 * toLinearChannel(green) + 0.0722 * toLinearChannel(blue);
    }

    private static void validateHexColor(String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7
                || !hexColor.substring(1).matches("[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Expected color in #RRGGBB format but got: " + hexColor);
        }
    }

    private static double toLinearChannel(int channel) {
        double normalized = channel / 255.0;
        if (normalized <= 0.04045) {
            return normalized / 12.92;
        }
        return Math.pow((normalized + 0.055) / 1.055, 2.4);
    }
}
