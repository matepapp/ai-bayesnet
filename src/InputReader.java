import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by matepapp on 2016. 11. 01..
 */
public class InputReader {
    public static Scanner scanner;
    // public static Scanner fileScanner;

    public InputReader() {
//        try {
//            fileScanner = new Scanner(new File(""));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        scanner = new Scanner(System.in);
    }

    public static int[] readIntegerLine() {
        String stringInput = (scanner.hasNextLine() ? scanner.nextLine() : "");
        List<String> stringInputArray = Arrays.asList(stringInput.split(","));

        int numbers = stringInputArray.size();
        int[] inputs = new int[numbers];

        for (int i = 0; i < numbers; i++)
            inputs[i] = Integer.parseInt(stringInputArray.get(i));

        return inputs;
    }

    public static double[] readDoubleLine() {
        String stringInput = (scanner.hasNextLine() ? scanner.nextLine() : "");
        List<String> stringInputArray = Arrays.asList(stringInput.split(","));

        int numbers = stringInputArray.size();
        double[] inputs = new double[numbers];

        for (int i = 0; i < numbers; i++)
            inputs[i] = Double.parseDouble(stringInputArray.get(i));

        return inputs;
    }

//    public static ArrayList<ArrayList<Double>> readFromFile() {
//        ArrayList<ArrayList<Double>> output = new ArrayList<ArrayList<Double>>();
//
//        while(fileScanner.hasNextLine()) {
//            ArrayList<Double> doubleLine = new ArrayList<>();
//            String line = fileScanner.nextLine();
//
//            Scanner lineScanner = new Scanner(line);
//            lineScanner.useDelimiter(",");
//
//            while(lineScanner.hasNextDouble())
//                doubleLine.add(lineScanner.nextDouble());
//
//            output.add(doubleLine);
//            lineScanner.close();
//        }
//        fileScanner.close();
//
//        return output;
//    }
}
