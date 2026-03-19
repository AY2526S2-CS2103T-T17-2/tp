package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_personWithAddressAndStarred_doesNotThrow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Person person = new PersonBuilder().withStarred(true).build();

        Platform.runLater(() -> {
            assertDoesNotThrow(() -> new PersonCard(person, 1));
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_personWithoutAddressAndUnstarred_doesNotThrow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Person person = new PersonBuilder().withNoAddress().withStarred(false).build();

        Platform.runLater(() -> {
            assertDoesNotThrow(() -> new PersonCard(person, 1));
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_personWithTags_doesNotThrow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Person person = new PersonBuilder().withTags("friends", "teammate").build();

        Platform.runLater(() -> {
            assertDoesNotThrow(() -> new PersonCard(person, 1));
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);
    }
}
