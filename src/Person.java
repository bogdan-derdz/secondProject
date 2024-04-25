import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Person {
    private String name;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private List<Person> parents;

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public List<Person> getParents() {
        return parents;
    }

    public Person(String name, LocalDate birthDate, LocalDate deathDate) {
        this.name = name;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.parents = new ArrayList<>();
    }

    public void addParent(Person person) {
        parents.add(person);
    }

    public static Person fromCsvLine(String line) {
        String[] data = line.split(",", -1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate birthDate = LocalDate.parse(data[1], formatter);
        LocalDate deathDate = data[2].isEmpty() ? null : LocalDate.parse(data[2], formatter);

        return new Person(data[0], birthDate, deathDate);
    }

    public static List<Person> fromCsv(String path) {
        String line = "";
        List<Person> people = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            reader.readLine();

            Map<String, PersonWithParentsNames> mapPersonWithParentNames = new HashMap<>();

            while ((line = reader.readLine()) != null) {

                PersonWithParentsNames personWithNames = PersonWithParentsNames.fromCsvLine(line);
                personWithNames.getPerson().validateLifeSpan();
                personWithNames.getPerson().validateAmbiguous(people);

                Person person = personWithNames.getPerson();
                people.add(person);
                mapPersonWithParentNames.put(person.name, personWithNames);


            }
            PersonWithParentsNames.linkRelatives(mapPersonWithParentNames);

            try {
                for (Person person : people) {
                    System.out.println("Sprwadzam");
                    person.validateParentingAge();
                }
            } catch (ParentingAgeException exception) {
                Scanner scanner = new Scanner(System.in);
                System.out.println(exception.getMessage());
                System.out.println("Please confirm [Y/N]:");
                String response = scanner.nextLine();
                if (!response.equals("Y") && !response.equals("y"))
                    people.remove(exception.person);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        } catch (NegativeLifespanException e) {
            throw new RuntimeException(e);
        } catch (AmbiguousPersonException e) {
            throw new RuntimeException(e);
        }
        return people;
    }

    private void validateLifeSpan() throws NegativeLifespanException {
        if (deathDate != null && deathDate.isBefore(birthDate)) {
            throw new NegativeLifespanException(this);
        }
    }

    private void validateAmbiguous(List<Person> list) throws AmbiguousPersonException {
        for (Person p : list) {
            if (p.name == this.name) {
                throw new AmbiguousPersonException(this);
            }
        }
    }

    private void validateParentingAge() throws ParentingAgeException {
        for (Person parent : parents) {
            if (birthDate.isBefore(parent.birthDate.plusYears(15)) || (parent.deathDate != null && birthDate.isAfter(parent.deathDate)))
                throw new ParentingAgeException(this, parent);
        }
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", deathDate=" + deathDate +
                ", parents=" + parents +
                '}';
    }

    public static void toBinaryFile(List<Person> people, String filename) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(filename);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(people);
        }
    }

    public static List<Person> fromBinaryFile(String filename) throws IOException, ClassNotFoundException {
        try (
                FileInputStream fis = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);
        ) {
            return (List<Person>) ois.readObject();
        }
    }

    public String generateTree() {
        Function<Person, String> cleanPersonName = person -> person.name.replace(" ", "");

        Function<Person, String> addObject = person -> String.format("object %s", cleanPersonName.apply(person));

        String name = cleanPersonName.apply(this);

        StringBuilder uml = new StringBuilder();
        uml.append("@startuml\n");
        uml.append(addObject.apply(this));

        if (this.parents != null) {
            String parentsString = parents.stream()
                    .map(parent -> "\n" + addObject.apply(parent) + "\n" + cleanPersonName.apply(parent) + "<--" + name)
                    .collect(Collectors.joining());

            uml.append(parentsString);
        }

        uml.append("\n@enduml");

        return uml.toString();
    }

    public static String generateTree(List<Person> people) {
        Function<Person, String> cleanPersonName = person -> person.name.replace(" ", "");

        Function<Person, String> addObject = person -> String.format("object %s\n", cleanPersonName.apply(person));

        String peopleString = people.stream()
                .map(person -> addObject.apply(person))
                .collect(Collectors.joining());

        String parentsString = people.stream().flatMap(person -> person.parents.isEmpty() ? Stream.empty() :
                        person.parents.stream()
                                .map(parent -> "\n" + cleanPersonName.apply(parent) + "<--" + cleanPersonName.apply(person)))
                .collect(Collectors.joining());


        return String.format("@startuml\n%s\n%s\n@enduml", peopleString, parentsString);
    }
}
