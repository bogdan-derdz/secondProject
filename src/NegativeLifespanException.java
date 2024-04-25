public class NegativeLifespanException extends Exception {
    public NegativeLifespanException(Person person) {
        super("Person " + person.getName() + " born in: " + person.getBirthDate() + " die in: " + person.getDeathDate() + " Die before birth day!");
    }
}
