public class DataRecords {

    private String title;
    private String releaseDate;
    private double totalSales;
    private double movingAverage;

    public DataRecords(String title, String releaseDate, double totalSales) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.totalSales = totalSales;
        this.movingAverage = 0.0;
    }

    public String getTitle() { return title; }
    public String getReleaseDate() { return releaseDate; }
    public double getTotalSales() { return totalSales; }
    public double getMovingAverage() { return movingAverage; }

    public void setMovingAverage(double movingAverage) {
        this.movingAverage = movingAverage;
    }
}