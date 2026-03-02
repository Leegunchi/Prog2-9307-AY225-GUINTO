import java.io.*;
import java.util.*;

public class MachineProblem1_GUINTO {

    // ─── File Validation ────────────────────────────────────────────────────────

    public static File promptForValidFile() {
        Scanner input = new Scanner(System.in);
        File file;

        while (true) {
            System.out.print("\nEnter dataset file path: ");
            String path = input.nextLine().trim();
            file = new File(path);

            if (!file.exists() || !file.isFile()) {
                System.out.println("[ERROR] File not found or path is not a file. Please try again.");
            } else if (!file.canRead()) {
                System.out.println("[ERROR] File is not readable. Check permissions and try again.");
            } else if (!path.toLowerCase().endsWith(".csv")) {
                System.out.println("[ERROR] File does not appear to be a CSV file (.csv extension required).");
            } else {
                System.out.println("[OK] File found and validated.");
                break;
            }
        }

        return file;
    }

    // ─── CSV Loading ─────────────────────────────────────────────────────────────

    public static List<DataRecord> loadCSV(File file) throws IOException {
        List<DataRecord> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty.");
            }

            // Parse header to find column indices
            String[] headers = parseCSVLine(headerLine);
            int titleIdx = -1, releaseDateIdx = -1, totalSalesIdx = -1;

            for (int i = 0; i < headers.length; i++) {
                switch (headers[i].trim().toLowerCase()) {
                    case "title":        titleIdx = i;        break;
                    case "release_date": releaseDateIdx = i;  break;
                    case "total_sales":  totalSalesIdx = i;   break;
                }
            }

            if (titleIdx == -1 || releaseDateIdx == -1 || totalSalesIdx == -1) {
                throw new IOException("CSV is missing required columns: title, release_date, total_sales.");
            }

            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                try {
                    String[] cols = parseCSVLine(line);
                    if (cols.length <= Math.max(titleIdx, Math.max(releaseDateIdx, totalSalesIdx))) {
                        continue; // skip malformed rows
                    }

                    String title       = cols[titleIdx].trim();
                    String releaseDate = cols[releaseDateIdx].trim();
                    String salesStr    = cols[totalSalesIdx].trim();

                    if (title.isEmpty() || releaseDate.isEmpty() || salesStr.isEmpty()) continue;

                    double totalSales = Double.parseDouble(salesStr);
                    records.add(new DataRecord(title, releaseDate, totalSales));

                } catch (NumberFormatException e) {
                    System.out.printf("[WARN] Skipping line %d – invalid sales value.%n", lineNum);
                }
            }
        }

        return records;
    }

    // Handles quoted CSV fields
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    // ─── Sorting ──────────────────────────────────────────────────────────────────

    public static void sortByDate(List<DataRecord> records) {
        records.sort(Comparator.comparing(DataRecord::getReleaseDate));
    }

    // ─── Moving Average ───────────────────────────────────────────────────────────

    public static void applyMovingAverage(List<DataRecord> records, int window) {
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

    // ─── Display ──────────────────────────────────────────────────────────────────

    public static void displayResults(List<DataRecord> records, int window) {
        String divider = "-".repeat(90);

        System.out.println("\n" + divider);
        System.out.printf(" SALES TREND – %d-RECORD MOVING AVERAGE%n", window);
        System.out.println(divider);
        System.out.printf("%-45s | %-12s | %10s | %10s%n",
            "Title", "Release Date", "Sales (M)", window + "-Rec MA");
        System.out.println(divider);

        for (DataRecord r : records) {
            System.out.println(r);
        }

        System.out.println(divider);
        System.out.printf("Total records displayed: %d%n", records.size());
        System.out.println(divider);
    }

    // ─── Main ─────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   SALES TREND MOVING AVERAGE ANALYZER ");
        System.out.println("========================================");

        File file = promptForValidFile();

        try {
            System.out.println("\nLoading dataset...");
            List<DataRecord> records = loadCSV(file);

            if (records.isEmpty()) {
                System.out.println("[ERROR] No valid records found in the file.");
                return;
            }

            System.out.printf("Loaded %d valid records.%n", records.size());

            System.out.println("Sorting by release date...");
            sortByDate(records);

            int window = 3;
            System.out.printf("Computing %d-record moving average...%n", window);
            applyMovingAverage(records, window);

            displayResults(records, window);

        } catch (IOException e) {
            System.out.println("[ERROR] Could not read file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}