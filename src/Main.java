import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Person> people = Person.fromCsv("family.csv");

//        for (Person person : people) {
//            System.out.println(person);
//        }

//        System.out.println(people.get(3).generateTree());
        System.out.println(Person.generateTree(people));
    }
}
