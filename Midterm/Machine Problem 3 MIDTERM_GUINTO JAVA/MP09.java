import java.io.*;
import java.util.*;

public class MP09 {

    // ExamRecord – stores one row of the CSV file
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
    }

    // ─────────────────────────────────────────────────────
    // main – entry point; drives the three program phases
    // ─────────────────────────────────────────────────────
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        File file = promptForValidFile(scanner);

        try {
            List<ExamRecord> records = loadCSV(file);

            if (records.isEmpty()) {
                System.out.println("ERROR: No valid records found in the file.");
                return;
            }

            displayStatistics(records);

        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        }

        scanner.close();
    }

    // promptForValidFile – loops until the user supplies a path to a
    //   file that exists, is readable, and ends with ".csv"
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

    // loadCSV – reads the file line by line with BufferedReader,
    //   skips metadata rows, maps the real header, and builds the record list
    private static List<ExamRecord> loadCSV(File file) throws IOException {
        List<ExamRecord> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            boolean headerFound = false;

            int candidateIdx = -1, typeIdx = -1, examIdx   = -1;
            int dateIdx      = -1, scoreIdx = -1, resultIdx = -1, timeIdx = -1;

            while ((line = br.readLine()) != null) {
                lineNum++;

                if (lineNum == 1 && line.startsWith("\uFEFF"))
                    line = line.substring(1);

                String[] cols = splitCSVLine(line);

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

                try {
                    String candidate = cols[candidateIdx].trim();
                    String type      = (typeIdx   >= 0 && typeIdx   < cols.length) ? cols[typeIdx].trim()   : "";
                    String exam      = (examIdx   >= 0 && examIdx   < cols.length) ? cols[examIdx].trim()   : "";
                    String examDate  = (dateIdx   >= 0 && dateIdx   < cols.length) ? cols[dateIdx].trim()   : "";
                    int    score     = (scoreIdx  >= 0 && scoreIdx  < cols.length) ? Integer.parseInt(cols[scoreIdx].trim()) : 0;
                    String result    = (resultIdx >= 0 && resultIdx < cols.length) ? cols[resultIdx].trim() : "";
                    String timeUsed  = (timeIdx   >= 0 && timeIdx   < cols.length) ? cols[timeIdx].trim()   : "";

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

    // displayStatistics – computes overall stats and a per-exam breakdown,
    //   then prints a formatted report to the console
    private static void displayStatistics(List<ExamRecord> records) {
        int totalRecords = records.size();
        int passCount    = 0;
        int failCount    = 0;
        int highScore    = Integer.MIN_VALUE;
        int lowScore     = Integer.MAX_VALUE;
        long scoreSum    = 0;

        String topCandidate    = "";  
        String bottomCandidate = "";  

       
        Map<String, int[]> examStats = new LinkedHashMap<>();

        for (ExamRecord r : records) {
            if (r.result.equalsIgnoreCase("PASS")) passCount++;
            else                                   failCount++;

            if (r.score > highScore) { highScore = r.score; topCandidate    = r.candidate; }
            if (r.score < lowScore)  { lowScore  = r.score; bottomCandidate = r.candidate; }

            scoreSum += r.score;

            examStats.computeIfAbsent(r.exam, k -> new int[]{0, 0, 0});
            examStats.get(r.exam)[0]++;                                         
            if (r.result.equalsIgnoreCase("PASS")) examStats.get(r.exam)[1]++;  // pass
            examStats.get(r.exam)[2] += r.score;                                 
        }

        double avgScore  = (double) scoreSum / totalRecords;
        double passRate  = passCount * 100.0  / totalRecords;

        // ── Print overall statistics ──
        String line = "=".repeat(72);
        String thin = "-".repeat(72);

        System.out.println("\n" + line);
        System.out.println("  MP09 - DATASET STATISTICS REPORT");
        System.out.println("  Pearson VUE Exam Results");
        System.out.println(line);

        System.out.printf("  %-30s : %d%n",   "Total Records",         totalRecords);
        System.out.printf("  %-30s : %d%n",   "Total Passed",          passCount);
        System.out.printf("  %-30s : %d%n",   "Total Failed",          failCount);
        System.out.printf("  %-30s : %.1f%%%n","Pass Rate",             passRate);
        System.out.printf("  %-30s : %.2f%n", "Average Score",         avgScore);
        System.out.printf("  %-30s : %d  (%s)%n", "Highest Score",     highScore, topCandidate);
        System.out.printf("  %-30s : %d  (%s)%n", "Lowest Score",      lowScore,  bottomCandidate);

        // ── Print per-exam breakdown ──
        System.out.println("\n" + thin);
        System.out.printf("  %-48s %6s %5s %5s %7s%n",
                "Exam", "Total", "Pass", "Fail", "AvgScr");
        System.out.println(thin);

        // Sort exams by total takers descending for readability
        examStats.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
                .forEach(e -> {
                    String exam    = e.getKey();
                    int[]  s       = e.getValue();
                    int    total   = s[0];
                    int    pass    = s[1];
                    int    fail    = total - pass;
                    double avg     = (double) s[2] / total;

                    // Truncate long exam names so the table stays aligned
                    String label = exam.length() > 46 ? exam.substring(0, 43) + "..." : exam;
                    System.out.printf("  %-48s %6d %5d %5d %7.1f%n",
                            label, total, pass, fail, avg);
                });

        System.out.println(line);
        System.out.println("  END OF REPORT");
        System.out.println(line);
    }

    // ─────────────────────────────────────────────────────────────────
    // splitCSVLine – splits a CSV line on commas while ignoring commas
    //   that appear inside double-quoted fields (e.g. "Last, First")
    // ─────────────────────────────────────────────────────────────────
    private static String[] splitCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb    = new StringBuilder();
        boolean inQuotes    = false;

        for (char c : line.toCharArray()) {
            if (c == '"')              inQuotes = !inQuotes;  // toggle quote context
            else if (c == ',' && !inQuotes) { fields.add(sb.toString()); sb.setLength(0); }
            else                       sb.append(c);
        }
        fields.add(sb.toString());  // add the last field
        return fields.toArray(new String[0]);
    }

    // ─────────────────────────────────────────────────────────────────
    // isEmptyRow – returns true when every column in the array is blank
    // ─────────────────────────────────────────────────────────────────
    private static boolean isEmptyRow(String[] cols) {
        for (String col : cols)
            if (!col.trim().isEmpty()) return false;
        return true;
    }
}