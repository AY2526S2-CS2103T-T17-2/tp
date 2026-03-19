package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: name, phone, email, remark and tags are present and not null; address is optional.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address; // optional - may be null
    private final Remark remark;
    private final boolean isArchived;
    private final Set<Tag> tags = new HashSet<>();
    private final boolean starred;

    /**
     * Creates a Person with all fields including address.
     * Address may be {@code null} to indicate it was not provided.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags) {
        this(name, phone, email, address, remark, false, tags, false);
    }

    /**
     * Creates a Person with all fields including address and starred state.
     * Address may be {@code null} to indicate it was not provided.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            boolean starred) {
        this(name, phone, email, address, remark, false, tags, starred);
    }

    /**
     * Creates a Person with all fields including address and archived state.
     * Address may be {@code null} to indicate it was not provided.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark,
            boolean isArchived, Set<Tag> tags) {
        this(name, phone, email, address, remark, isArchived, tags, false);
    }

    /**
     * Creates a Person with all fields including address, archived state, and starred state.
     * Address may be {@code null} to indicate it was not provided.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark,
            boolean isArchived, Set<Tag> tags, boolean starred) {
        requireAllNonNull(name, phone, email, remark, tags);
        requireNonNull(name);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address; // nullable
        this.remark = remark;
        this.isArchived = isArchived;
        this.tags.addAll(tags);
        this.starred = starred;
    }

    /**
     * Creates a Person without an address (address defaults to {@code null}).
     */
    public Person(Name name, Phone phone, Email email, Remark remark, Set<Tag> tags) {
        this(name, phone, email, null, remark, tags);
    }

    /**
     * Creates a Person without an address and with a specific starred state.
     */
    public Person(Name name, Phone phone, Email email, Remark remark, Set<Tag> tags, boolean starred) {
        this(name, phone, email, null, remark, tags, starred);
    }

    /**
     * Creates a Person without an address and with a specific archived state.
     */
    public Person(Name name, Phone phone, Email email, Remark remark, boolean isArchived, Set<Tag> tags) {
        this(name, phone, email, null, remark, isArchived, tags);
    }

    /**
     * Creates a Person without an address and with specific archived and starred states.
     */
    public Person(Name name, Phone phone, Email email, Remark remark,
            boolean isArchived, Set<Tag> tags, boolean starred) {
        this(name, phone, email, null, remark, isArchived, tags, starred);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    /**
     * Returns the address, or {@code null} if no address was provided.
     * Callers should check {@link #hasAddress()} before using this value.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Returns true if this person has an address set.
     */
    public boolean hasAddress() {
        return address != null;
    }

    public Remark getRemark() {
        return remark;
    }

    /**
     * Returns true if this person is archived.
     */
    public boolean isArchived() {
        return isArchived;
    }

    /**
     * Returns a copy of this person with the given archived state.
     */
    public Person withArchived(boolean archived) {
        return new Person(name, phone, email, address, remark, archived, tags, starred);
    }

    /**
     * Returns true if this person is starred.
     */
    public boolean isStarred() {
        return starred;
    }

    /**
     * Returns a copy of this person with the given starred state.
     */
    public Person withStarred(boolean starred) {
        return new Person(name, phone, email, address, remark, isArchived, tags, starred);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person)) {
            return false;
        }

        Person otherPerson = (Person) other;
        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && Objects.equals(address, otherPerson.address)
                && remark.equals(otherPerson.remark)
                && isArchived == otherPerson.isArchived
                && tags.equals(otherPerson.tags)
                && starred == otherPerson.starred;
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, remark, isArchived, tags, starred);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("remark", remark)
                .add("isArchived", isArchived)
                .add("tags", tags)
                .add("starred", starred)
                .toString();
    }

}
