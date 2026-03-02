public class DataRecord {
    private String title;
    private String releaseDate;
    private double totalSales;
    private double movingAverage;

    public DataRecord(String title, String releaseDate, double totalSales) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.totalSales = totalSales;
        this.movingAverage = 0.0;
    }

    public String getTitle() { return title; }
    public String getReleaseDate() { return releaseDate; }
    public double getTotalSales() { return totalSales; }
    public double getMovingAverage() { return movingAverage; }
    public void setMovingAverage(double movingAverage) { this.movingAverage = movingAverage; }

    @Override
    public String toString() {
        String maStr = (movingAverage > 0)
            ? String.format("%.2f", movingAverage)
            : "  N/A ";
        return String.format("%-45s | %-12s | %10.2f | %10s",
            title.length() > 44 ? title.substring(0, 41) + "..." : title,
            releaseDate,
            totalSales,
            maStr);
    }
}