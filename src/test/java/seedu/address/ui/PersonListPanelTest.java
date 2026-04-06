package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonListPanelTest {

    private static final int FX_TIMEOUT_SECONDS = 5;
    private static final int MIN_VISIBLE_ROWS = 4;
    private static final double LIST_VIEW_WIDTH_ALLOWANCE = PersonListPanel.LIST_VIEW_WIDTH_ALLOWANCE;
    private static final double WIDTH_TOLERANCE = 8;

    @BeforeAll
    public static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> startupThrowable = new AtomicReference<>();
        try {
            Platform.startup(() -> latch.countDown());
        } catch (IllegalStateException e) {
            latch.countDown();
        } catch (UnsupportedOperationException e) {
            startupThrowable.set(e);
            latch.countDown();
        } catch (RuntimeException e) {
            startupThrowable.set(e);
            latch.countDown();
        }

        assertTrue(latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS), "Timed out waiting for JavaFX toolkit startup.");
        Throwable throwable = startupThrowable.get();
        assumeTrue(throwable == null, () ->
                "Skipping PersonListPanelTest because JavaFX is unavailable: "
                        + throwable.getClass().getSimpleName());
    }

    @Test
    public void listPanel_initialLayout_sizesCellsToViewportWithoutManualResize() throws InterruptedException {
        ObservableList<Person> persons = FXCollections.observableArrayList(IntStream.rangeClosed(1, 10)
                .mapToObj(index -> new PersonBuilder()
                        .withName("Person " + index)
                        .withEmail("person" + index + "@example.com")
                        .withAddress("Address " + index)
                        .build())
                .toList());

        runFxAndWait(() -> {
            PersonListPanel panel = new PersonListPanel(persons);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(panel.getRoot(), 420, 720);
                stage.setScene(scene);
                stage.show();

                applyCssAndLayout(scene);
                // A second pass stabilizes VirtualFlow metrics before width assertions run.
                applyCssAndLayout(scene);

                ListView<?> listView = (ListView<?>) panel.getRoot().lookup("#personListView");
                assertTrue(listView.getWidth() > 0, "List view width should be set on first layout.");

                List<ListCell<?>> initialCells = getPopulatedCells(listView);
                double initialListWidth = listView.getWidth();
                double initialGraphicWidth = getFirstGraphicPrefWidth(initialCells);
                int initialCellCount = initialCells.size();

                assertTrue(initialCellCount >= MIN_VISIBLE_ROWS,
                        "Initial layout should populate more than three visible rows without requiring a resize.");
                assertTrue(initialGraphicWidth > 0,
                        "Card width should be established on first layout.");
                assertTrue(Math.abs(initialGraphicWidth - (initialListWidth - LIST_VIEW_WIDTH_ALLOWANCE))
                    < WIDTH_TOLERANCE,
                        "Card width should track the list viewport on first layout.");
            } finally {
                stage.close();
            }
        });
    }

    private static void runFxAndWait(ThrowingRunnable runnable) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> throwable = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                throwable.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS), "Timed out waiting for JavaFX task completion.");
        Throwable failure = throwable.get();
        if (failure != null) {
            throw new AssertionError("Exception on JavaFX thread", failure);
        }
    }

    private static void applyCssAndLayout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    private static List<ListCell<?>> getPopulatedCells(ListView<?> listView) {
        List<ListCell<?>> populatedCells = new ArrayList<>();
        for (Node node : listView.lookupAll(".list-cell")) {
            if (node instanceof ListCell<?> cell && !cell.isEmpty() && cell.getGraphic() != null) {
                populatedCells.add(cell);
            }
        }
        return populatedCells;
    }

    private static double getFirstGraphicPrefWidth(List<ListCell<?>> cells) {
        if (cells == null || cells.isEmpty()) {
            throw new AssertionError("Expected at least one cell but found 0");
        }
        Node firstGraphic = cells.get(0).getGraphic();
        if (!(firstGraphic instanceof Region region)) {
            throw new AssertionError("Expected first cell graphic to be a Region but found: "
                    + (firstGraphic == null ? "null" : firstGraphic.getClass().getSimpleName()));
        }
        return region.getPrefWidth();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}