public class AmbiguousPersonException extends Exception {
    public AmbiguousPersonException(Person person) {
        super("Person: " + person.getName() + " repeated in the file");
    }
}
