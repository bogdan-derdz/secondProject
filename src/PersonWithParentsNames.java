import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersonWithParentsNames {
    private Person person;
    private List<String> names;

    public PersonWithParentsNames(Person person, List<String> names) {
        this.person = person;
        this.names = names;
    }

    public Person getPerson() {
        return person;
    }

    public List<String> getNames() {
        return names;
    }

    public static PersonWithParentsNames fromCsvLine(String line) {
        Person person = Person.fromCsvLine(line);
        String[] fields = line.split(",", -1);
        List<String> names = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            if (!fields[i + 3].isEmpty()) {
                names.add(fields[i + 3]);
            }
        }

        return new PersonWithParentsNames(person, names);
    }

    public static void linkRelatives(Map<String, PersonWithParentsNames> mapPersonWithParentNames) {
        for (PersonWithParentsNames personWithNames : mapPersonWithParentNames.values()) {
            for (String parentName : personWithNames.names) {
                personWithNames.person.addParent(mapPersonWithParentNames.get(parentName).person);
            }
        }
    }
}
