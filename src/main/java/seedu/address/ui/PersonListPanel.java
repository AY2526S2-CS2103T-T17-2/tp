package seedu.address.ui;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    static final double LIST_VIEW_WIDTH_ALLOWANCE = 18; // Accounts for the list view scrollbar width.

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setMinWidth(0);
        personListView.setMaxWidth(Double.MAX_VALUE);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        private PersonCard personCard;

        PersonListViewCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setMinWidth(0);
            setMaxWidth(Control.USE_PREF_SIZE);
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (getGraphic() instanceof Region currentCard) {
                currentCard.prefWidthProperty().unbind();
            }

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                if (personCard == null) {
                    personCard = new PersonCard(person, getIndex() + 1);
                } else {
                    personCard.update(person, getIndex() + 1);
                }

                Region cardRoot = personCard.getRoot();
                cardRoot.setMinWidth(0);
                cardRoot.setMaxWidth(Double.MAX_VALUE);
                cardRoot.prefWidthProperty().bind(Bindings.max(0,
                        getListView().widthProperty().subtract(LIST_VIEW_WIDTH_ALLOWANCE)));
                setGraphic(cardRoot);
                setText(null);
            }
        }

        @Override
        protected double computePrefHeight(double width) {
            if (getGraphic() instanceof Region cardRoot) {
                double contentWidth;
                if (width < 0) {
                    contentWidth = Math.max(0, cardRoot.prefWidth(-1));
                } else {
                    contentWidth = Math.max(0, width - snappedLeftInset() - snappedRightInset());
                }
                return cardRoot.prefHeight(contentWidth) + snappedTopInset() + snappedBottomInset();
            }
            return super.computePrefHeight(width);
        }
    }

}
