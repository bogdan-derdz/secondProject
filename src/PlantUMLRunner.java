import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PlantUMLRunner {
    private static String plantUMLPath;

    public static void setPlantUMLPath(String plantUMLPath) {
        PlantUMLRunner.plantUMLPath = plantUMLPath;
    }

    public static void generateDiagram(String UMLData, String outputDirPath, String outputFileName) {
        File file = new File(outputDirPath + "/" + outputFileName + ".txt");

        try {
            FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8);

            writer.write(UMLData);
            String command = "java " + "-jar " + plantUMLPath + " -charset UTF-8 " + file.getPath() + " -o " + outputDirPath + outputFileName;

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
