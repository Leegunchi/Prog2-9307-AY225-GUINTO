import java.io.*;
import java.util.*;

public class MP10 {

    // ─────────────────────────────────────────────
    // ExamRecord – stores one row of the CSV file
    // ─────────────────────────────────────────────
    static class ExamRecord {
        String candidate; 
        String type;      
        String exam;      
        String examDate;  
        int    score;     
        String result;    
        String timeUsed;  

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

        /**
         * duplicateKey – two records are duplicates when the same candidate
         * sat the same exam (case-insensitive comparison).
         */
        String duplicateKey() {
            return candidate.toLowerCase() + "||" + exam.toLowerCase();
        }

        /** One-line formatted representation used in the output table. */
        String toRow() {
            String examLabel = exam.length() > 44 ? exam.substring(0, 41) + "..." : exam;
            return String.format("  %-26s %-46s %-12s %5d  %-5s  %s",
                    candidate, examLabel, examDate, score, result, timeUsed);
        }
    }

    // ──────────────────────────────────────────────────────────
    // main – entry point; drives the three phases of the program
    // ──────────────────────────────────────────────────────────
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Phase 1 – prompt until a valid CSV path is given
        File file = promptForValidFile(scanner);

        try {
            // Phase 2 – parse all data rows from the CSV
            List<ExamRecord> records = loadCSV(file);

            if (records.isEmpty()) {
                System.out.println("ERROR: No valid records found in the file.");
                return;
            }

            // Phase 3 – detect duplicates and print the report
            detectAndDisplayDuplicates(records);

        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        }

        scanner.close();
    }

    // ─────────────────────────────────────────────────────────────────────
    // promptForValidFile – keeps asking until the user gives a path that
    //   points to an existing, readable file with a ".csv" extension
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
    // loadCSV – opens the file with BufferedReader, scans for the real header
    //   row (skipping Pearson VUE metadata at the top), maps column indices,
    //   then parses every data row into an ExamRecord
    // ─────────────────────────────────────────────────────────────────────
    private static List<ExamRecord> loadCSV(File file) throws IOException {
        List<ExamRecord> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String  line;
            int     lineNum     = 0;
            boolean headerFound = false;

            // Column index variables – filled in once the header row is identified
            int candidateIdx = -1, typeIdx = -1, examIdx   = -1;
            int dateIdx      = -1, scoreIdx = -1, resultIdx = -1, timeIdx = -1;

            while ((line = br.readLine()) != null) {
                lineNum++;

                // Remove UTF-8 BOM character if present on the first line
                if (lineNum == 1 && line.startsWith("\uFEFF"))
                    line = line.substring(1);

                String[] cols = splitCSVLine(line);

                // ── Scan for the real header row ──
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
    // detectAndDisplayDuplicates – groups records by their duplicateKey(),
    //   collects groups with more than one entry, and prints a formatted
    //   report with per-group tables and a final summary
    // ─────────────────────────────────────────────────────────────────────
    private static void detectAndDisplayDuplicates(List<ExamRecord> records) {
        // Map: duplicateKey → list of all records sharing that key
        Map<String, List<ExamRecord>> keyMap = new LinkedHashMap<>();
        for (ExamRecord r : records)
            keyMap.computeIfAbsent(r.duplicateKey(), k -> new ArrayList<>()).add(r);

        // Separate groups: duplicate (size > 1) vs. unique (size == 1)
        List<List<ExamRecord>> dupeGroups = new ArrayList<>();
        int uniqueCount = 0;
        for (List<ExamRecord> group : keyMap.values()) {
            if (group.size() > 1) dupeGroups.add(group);
            else                  uniqueCount++;
        }

        // Total individual records that belong to a duplicate group
        int dupeTotalRecords = dupeGroups.stream().mapToInt(List::size).sum();

        String heavy = "=".repeat(100);
        String light = "-".repeat(100);

        System.out.println("\n" + heavy);
        System.out.println("  MP10 - DUPLICATE RECORD DETECTION REPORT");
        System.out.println("  Pearson VUE Exam Results");
        System.out.println(heavy);

        if (dupeGroups.isEmpty()) {
            System.out.println("  No duplicate records were found in this dataset.");
        } else {
            System.out.printf("  %-26s %-46s %-12s %5s  %-5s  %s%n",
                    "Candidate", "Exam", "Date", "Score", "Pass?", "Time Used");
            System.out.println("  " + light);

            int groupNum = 1;
            for (List<ExamRecord> group : dupeGroups) {
                System.out.printf("%n  [Duplicate Group #%d]  (%d entries)%n",
                        groupNum++, group.size());
                for (ExamRecord r : group)
                    System.out.println(r.toRow());
            }
        }

        // ── Dataset statistics summary ──
        System.out.println("\n" + heavy);
        System.out.println("  DATASET STATISTICS");
        System.out.println(heavy);
        System.out.printf("  %-35s : %d%n", "Total records loaded",          records.size());
        System.out.printf("  %-35s : %d%n", "Unique candidate-exam pairs",   uniqueCount);
        System.out.printf("  %-35s : %d%n", "Duplicate groups found",        dupeGroups.size());
        System.out.printf("  %-35s : %d%n", "Records flagged as duplicates", dupeTotalRecords);
        System.out.println(heavy);
        System.out.println("  END OF REPORT");
        System.out.println(heavy);
    }

    // ──────────────────────────────────────────────────────────────────────
    // splitCSVLine – splits on commas but treats text inside double-quotes
    //   as a single field (handles "Last, First" name format in the dataset)
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
    // safeGet – retrieves a column value safely; returns "" if the index is
    //   out of range (guards against rows with fewer columns than the header)
    // ──────────────────────────────────────────────────────────────────────
    private static String safeGet(String[] cols, int idx) {
        return (idx >= 0 && idx < cols.length) ? cols[idx].trim() : "";
    }
}