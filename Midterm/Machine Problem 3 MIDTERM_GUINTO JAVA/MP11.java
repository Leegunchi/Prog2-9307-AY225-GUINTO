import java.io.*;
import java.util.*;

/**
 * MP11 - Frequency Count for Column Values
 * Dataset : Pearson VUE Exam Results (Sample_Data-Prog-2-csv.csv)
 * Course  : Prog2-9307-AY225-GUINTO
 *
 * Reads a CSV file of Pearson VUE exam records and produces frequency count
 * tables for three key columns: Exam (which cert was taken most/least),
 * Result (PASS vs FAIL breakdown), and Type (Student / Faculty / NTE).
 * Each table is sorted from highest to lowest count.
 */
public class MP11 {

    // ─────────────────────────────────────────────
    // ExamRecord – stores one row of the CSV file
    // ─────────────────────────────────────────────
    static class ExamRecord {
        String candidate; // Full name of the test-taker
        String type;      // Role: Student / Faculty / NTE
        String exam;      // Name of the certification exam
        String examDate;  // Date the exam was taken (MM/DD/YYYY)
        int    score;     // Numeric score
        String result;    // "PASS" or "FAIL"
        String timeUsed;  // Time spent on the exam

        ExamRecord(String candidate, String type, String exam,
                   String examDate, int score, String result, String timeUsed) {
            this.candidate = candidate;
            this.type      = type;
            this.exam      = exam;
            this.examDate  = examDate;
            this.score     = score;
            this.result    = result;
            this.timeUsed  = timeUsed;
        }
    }

    // ──────────────────────────────────────────────────────────
    // main – entry point; drives the three phases of the program
    // ──────────────────────────────────────────────────────────
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Phase 1 – keep asking until a valid CSV file path is provided
        File file = promptForValidFile(scanner);

        try {
            // Phase 2 – parse all CSV rows into ExamRecord objects
            List<ExamRecord> records = loadCSV(file);

            if (records.isEmpty()) {
                System.out.println("ERROR: No valid records found in the file.");
                return;
            }

            // Phase 3 – build and print the frequency count report
            displayFrequencyCounts(records);

        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        }

        scanner.close();
    }

    // ─────────────────────────────────────────────────────────────────────
    // promptForValidFile – loops until the user supplies a path pointing to
    //   an existing, readable ".csv" file
    // ─────────────────────────────────────────────────────────────────────
    private static File promptForValidFile(Scanner scanner) {
        while (true) {
            System.out.print("Enter FULL dataset file path: ");
            String path = scanner.nextLine().trim();
            File file = new File(path);

            if      (!file.exists())                       System.out.println("ERROR: File does not exist.");
            else if (!file.isFile())                       System.out.println("ERROR: Path is not a file.");
            else if (!file.canRead())                      System.out.println("ERROR: File is not readable.");
            else if (!path.toLowerCase().endsWith(".csv")) System.out.println("ERROR: File must be a .csv file.");
            else return file;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // loadCSV – reads the file with BufferedReader, skips the Pearson VUE
    //   metadata rows at the top, locates the real column header, then
    //   parses each data row into an ExamRecord stored in an ArrayList
    // ─────────────────────────────────────────────────────────────────────
    private static List<ExamRecord> loadCSV(File file) throws IOException {
        List<ExamRecord> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String  line;
            int     lineNum     = 0;
            boolean headerFound = false;

            // Column index variables – resolved once the header row is located
            int candidateIdx = -1, typeIdx = -1, examIdx   = -1;
            int dateIdx      = -1, scoreIdx = -1, resultIdx = -1, timeIdx = -1;

            while ((line = br.readLine()) != null) {
                lineNum++;

                // Strip UTF-8 BOM on the first line if present
                if (lineNum == 1 && line.startsWith("\uFEFF"))
                    line = line.substring(1);

                String[] cols = splitCSVLine(line);

                // ── Find and map the header row ──
                if (!headerFound) {
                    for (int i = 0; i < cols.length; i++) {
                        String h = cols[i].trim().toLowerCase();
                        if      (h.equals("candidate"))             candidateIdx = i;
                        else if (h.equals("student/ faculty/ nte")) typeIdx      = i;
                        else if (h.equals("exam"))                  examIdx      = i;
                        else if (h.equals("exam date"))             dateIdx      = i;
                        else if (h.equals("score"))                 scoreIdx     = i;
                        else if (h.equals("result"))                resultIdx    = i;
                        else if (h.equals("time used"))             timeIdx      = i;
                    }
                    if (candidateIdx != -1 && examIdx != -1 && scoreIdx != -1)
                        headerFound = true;
                    continue;
                }

                if (line.trim().isEmpty() || isEmptyRow(cols)) continue;

                // ── Parse one data row ──
                try {
                    String candidate = cols[candidateIdx].trim();
                    String type      = safeGet(cols, typeIdx);
                    String exam      = safeGet(cols, examIdx);
                    String examDate  = safeGet(cols, dateIdx);
                    int    score     = scoreIdx >= 0 && scoreIdx < cols.length
                                       ? Integer.parseInt(cols[scoreIdx].trim()) : 0;
                    String result    = safeGet(cols, resultIdx);
                    String timeUsed  = safeGet(cols, timeIdx);

                    if (candidate.isEmpty() || exam.isEmpty()) continue;

                    records.add(new ExamRecord(candidate, type, exam,
                                               examDate, score, result, timeUsed));
                } catch (Exception e) {
                    System.out.println("WARNING: Skipping invalid row at line " + lineNum + ".");
                }
            }
        }

        return records;
    }

    // ─────────────────────────────────────────────────────────────────────
    // displayFrequencyCounts – tallies occurrences for the Exam, Result, and
    //   Type columns, then prints each as a frequency table sorted high→low
    // ─────────────────────────────────────────────────────────────────────
    private static void displayFrequencyCounts(List<ExamRecord> records) {
        int total = records.size();

        // Frequency maps – value → how many times it appears in the dataset
        Map<String, Integer> examFreq   = new LinkedHashMap<>();  // which exam was taken
        Map<String, Integer> resultFreq = new LinkedHashMap<>();  // PASS or FAIL
        Map<String, Integer> typeFreq   = new LinkedHashMap<>();  // Student/Faculty/NTE

        // Single pass through all records to tally every map at once
        for (ExamRecord r : records) {
            examFreq  .merge(r.exam,   1, Integer::sum);  // merge adds 1, or sums if key exists
            resultFreq.merge(r.result, 1, Integer::sum);
            typeFreq  .merge(r.type,   1, Integer::sum);
        }

        String heavy = "=".repeat(72);

        System.out.println("\n" + heavy);
        System.out.println("  MP11 - FREQUENCY COUNT REPORT");
        System.out.println("  Pearson VUE Exam Results  |  Total Records: " + total);
        System.out.println(heavy);

        // Print each frequency table using the shared helper
        printTable("Exam",           examFreq,   total);
        printTable("Result",         resultFreq, total);
        printTable("Candidate Type", typeFreq,   total);

        // ── Dataset statistics summary ──
        System.out.println(heavy);
        System.out.println("  DATASET STATISTICS");
        System.out.println(heavy);
        System.out.printf("  %-30s : %d%n",   "Total records loaded",   total);
        System.out.printf("  %-30s : %d%n",   "Distinct exams",         examFreq.size());
        System.out.printf("  %-30s : %d%n",   "Distinct candidate types", typeFreq.size());
        System.out.printf("  %-30s : %.1f%%%n","Overall pass rate",
                resultFreq.getOrDefault("PASS", 0) * 100.0 / total);
        System.out.println(heavy);
        System.out.println("  END OF REPORT");
        System.out.println(heavy);
    }

    // ─────────────────────────────────────────────────────────────────────
    // printTable – sorts a frequency map by count (descending), then prints
    //   it as a labelled table showing count, percentage, and a bar chart
    //   so the distribution is immediately visible
    // ─────────────────────────────────────────────────────────────────────
    private static void printTable(String columnName, Map<String, Integer> freqMap, int total) {
        // Sort the map entries by count, highest first
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(freqMap.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        int maxCount = sorted.isEmpty() ? 1 : sorted.get(0).getValue(); // for bar scaling
        int barMax   = 30; // maximum bar width in characters

        String thin = "-".repeat(72);
        System.out.println("\n  [ " + columnName + " ]");
        System.out.println("  " + thin);
        System.out.printf("  %-44s %6s %7s  %s%n", "Value", "Count", "   %", "Bar");
        System.out.println("  " + thin);

        for (Map.Entry<String, Integer> entry : sorted) {
            String value   = entry.getKey();
            int    count   = entry.getValue();
            double pct     = count * 100.0 / total;

            // Scale bar length proportionally to the highest count in the table
            int    barLen  = (int) Math.round((double) count / maxCount * barMax);
            String bar     = "█".repeat(barLen);

            // Truncate long labels so columns stay aligned
            String label   = value.length() > 42 ? value.substring(0, 39) + "..." : value;

            System.out.printf("  %-44s %6d %6.1f%%  %s%n", label, count, pct, bar);
        }

        System.out.println("  " + thin);
    }

    // ──────────────────────────────────────────────────────────────────────
    // splitCSVLine – splits on commas but respects double-quoted fields
    //   (necessary because candidate names are stored as "Last, First")
    // ──────────────────────────────────────────────────────────────────────
    private static String[] splitCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb    = new StringBuilder();
        boolean inQuotes    = false;

        for (char c : line.toCharArray()) {
            if (c == '"')              inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) { fields.add(sb.toString()); sb.setLength(0); }
            else                       sb.append(c);
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    // ─────────────────────────────────────────────────────────────────
    // isEmptyRow – returns true when every column value is blank/empty
    // ─────────────────────────────────────────────────────────────────
    private static boolean isEmptyRow(String[] cols) {
        for (String col : cols)
            if (!col.trim().isEmpty()) return false;
        return true;
    }

    // ──────────────────────────────────────────────────────────────────────
    // safeGet – retrieves cols[idx].trim() or "" if idx is out of bounds
    // ──────────────────────────────────────────────────────────────────────
    private static String safeGet(String[] cols, int idx) {
        return (idx >= 0 && idx < cols.length) ? cols[idx].trim() : "";
    }
}