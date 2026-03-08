import java.io.*;
import java.util.*;

public class MachineProblem1_GUINTO {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        File validFile = promptForValidFile(scanner);

        try {
            List<DataRecords> records = loadCSV(validFile);

            if (records.isEmpty()) {
                System.out.println("ERROR: No valid records found in file.");
                return;
            }

            sortByDate(records);
            applyMovingAverage(records, 3);
            displayResults(records);

        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        }

        scanner.close();
    }

    private static File promptForValidFile(Scanner scanner) {
        while (true) {
            System.out.print("Enter FULL dataset file path: ");
            String path = scanner.nextLine().trim();

            File file = new File(path);

            if (!file.exists()) {
                System.out.println("ERROR: File does not exist.");
            } else if (!file.isFile()) {
                System.out.println("ERROR: Path is not a file.");
            } else if (!file.canRead()) {
                System.out.println("ERROR: File is not readable.");
            } else if (!path.toLowerCase().endsWith(".csv")) {
                System.out.println("ERROR: File must be a .csv file.");
            } else {
                return file;
            }
        }
    }

    private static List<DataRecords> loadCSV(File file) throws IOException {
        List<DataRecords> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null)
                throw new IOException("CSV file is empty.");

            String[] headers = headerLine.split(",");

            int titleIdx = -1, dateIdx = -1, salesIdx = -1;

            for (int i = 0; i < headers.length; i++) {
                String col = headers[i].trim().toLowerCase();
                if (col.equals("title")) titleIdx = i;
                if (col.equals("release_date")) dateIdx = i;
                if (col.equals("total_sales")) salesIdx = i;
            }

            if (titleIdx == -1 || dateIdx == -1 || salesIdx == -1)
                throw new IOException("Missing required columns: title, release_date, total_sales.");

            String line;
            int lineNum = 1;

            while ((line = br.readLine()) != null) {
                lineNum++;
                try {
                    String[] cols = line.split(",");
                    String title = cols[titleIdx].trim();
                    String date = cols[dateIdx].trim();
                    double sales = Double.parseDouble(cols[salesIdx].trim());
                    records.add(new DataRecords(title, date, sales));
                } catch (Exception e) {
                    System.out.println("WARNING: Skipping invalid row at line " + lineNum + ".");
                }
            }
        }

        return records;
    }

    private static void sortByDate(List<DataRecords> records) {
        records.sort(Comparator.comparing(DataRecords::getReleaseDate));
    }

    private static void applyMovingAverage(List<DataRecords> records, int window) {
        for (int i = 0; i < records.size(); i++) {
            if (i >= window - 1) {
                double sum = 0;
                for (int j = i - (window - 1); j <= i; j++) {
                    sum += records.get(j).getTotalSales();
                }
                records.get(i).setMovingAverage(sum / window);
            }
        }
    }

    private static void displayResults(List<DataRecords> records) {
        String separator = "-".repeat(80);

        System.out.println("\n" + separator);
        System.out.printf("%-35s %-15s %-12s %-15s%n",
                "Title", "Release Date", "Sales (M)", "3-Rec Mov Avg");
        System.out.println(separator);

        for (DataRecords r : records) {
            String movingAvg = (r.getMovingAverage() > 0)
                    ? String.format("%.2f", r.getMovingAverage())
                    : "N/A";

            System.out.printf("%-35s %-15s %-12s %-15s%n",
                    r.getTitle(),
                    r.getReleaseDate(),
                    String.format("%.2f", r.getTotalSales()),
                    movingAvg);
        }

        System.out.println(separator);
        System.out.println("Total records: " + records.size());
    }
}