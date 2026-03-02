import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class MachineProblem1_GUINTO extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    public MachineProblem1_GUINTO() {
        setTitle("Sales Trend Moving Average Analyzer");
        setSize(1000, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] columns = {"Title", "Release Date", "Sales (M)", "3-Record Moving Avg"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // REQUIREMENT #1 → Ask full file path at start
        File validFile = promptForValidFile();

        try {
            List<DataRecord> records = loadCSV(validFile);

            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No valid records found in file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            sortByDate(records);
            applyMovingAverage(records, 3);
            displayResults(records);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Unexpected Error:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────
    // 1–3 FILE VALIDATION (LOOP UNTIL VALID)
    // ─────────────────────────────────────────────
    private File promptForValidFile() {
        while (true) {

            String path = JOptionPane.showInputDialog(
                    this,
                    "Enter FULL dataset file path:",
                    "Dataset Input",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (path == null) {
                System.exit(0);
            }

            File file = new File(path.trim());

            if (!file.exists()) {
                JOptionPane.showMessageDialog(this,
                        "ERROR: File does not exist.",
                        "Invalid File",
                        JOptionPane.ERROR_MESSAGE);
            }
            else if (!file.isFile()) {
                JOptionPane.showMessageDialog(this,
                        "ERROR: Path is not a file.",
                        "Invalid File",
                        JOptionPane.ERROR_MESSAGE);
            }
            else if (!file.canRead()) {
                JOptionPane.showMessageDialog(this,
                        "ERROR: File is not readable.",
                        "Invalid File",
                        JOptionPane.ERROR_MESSAGE);
            }
            else if (!path.toLowerCase().endsWith(".csv")) {
                JOptionPane.showMessageDialog(this,
                        "ERROR: File must be a .csv file.",
                        "Invalid File",
                        JOptionPane.ERROR_MESSAGE);
            }
            else {
                return file; // VALID
            }
        }
    }

    // ─────────────────────────────────────────────
    // CSV LOADING (BufferedReader Required)
    // ─────────────────────────────────────────────
    private List<DataRecord> loadCSV(File file) throws IOException {

        List<DataRecord> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String headerLine = br.readLine();
            if (headerLine == null)
                throw new IOException("CSV file is empty.");

            String[] headers = headerLine.split(",");

            int titleIdx = -1;
            int dateIdx = -1;
            int salesIdx = -1;

            for (int i = 0; i < headers.length; i++) {
                String col = headers[i].trim().toLowerCase();
                if (col.equals("title")) titleIdx = i;
                if (col.equals("release_date")) dateIdx = i;
                if (col.equals("total_sales")) salesIdx = i;
            }

            if (titleIdx == -1 || dateIdx == -1 || salesIdx == -1)
                throw new IOException("Missing required columns.");

            String line;

            while ((line = br.readLine()) != null) {
                try {
                    String[] cols = line.split(",");

                    String title = cols[titleIdx].trim();
                    String date = cols[dateIdx].trim();
                    double sales = Double.parseDouble(cols[salesIdx].trim());

                    records.add(new DataRecord(title, date, sales));

                } catch (Exception e) {
                    // Skip invalid rows
                }
            }
        }

        return records;
    }

    // ─────────────────────────────────────────────
    // SORT BY DATE
    // ─────────────────────────────────────────────
    private void sortByDate(List<DataRecord> records) {
        records.sort(Comparator.comparing(DataRecord::getReleaseDate));
    }

    // ─────────────────────────────────────────────
    // 3-RECORD MOVING AVERAGE
    // ─────────────────────────────────────────────
    private void applyMovingAverage(List<DataRecord> records, int window) {

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

    // ─────────────────────────────────────────────
    // DISPLAY RESULTS (Formatted JTable)
    // ─────────────────────────────────────────────
    private void displayResults(List<DataRecord> records) {

        tableModel.setRowCount(0);

        for (DataRecord r : records) {

            String movingAvg = (r.getMovingAverage() > 0)
                    ? String.format("%.2f", r.getMovingAverage())
                    : "N/A";

            tableModel.addRow(new Object[]{
                    r.getTitle(),
                    r.getReleaseDate(),
                    String.format("%.2f", r.getTotalSales()),
                    movingAvg
            });
        }
    }

    // ─────────────────────────────────────────────
    // MAIN METHOD
    // ─────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MachineProblem1_GUINTO().setVisible(true);
        });
    }
}