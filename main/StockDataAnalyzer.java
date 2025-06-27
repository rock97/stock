import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import main.StockDailyData;

public class StockDataAnalyzer {
    
    private List<StockDailyData> stockDataList;
    
    public StockDataAnalyzer() {
        this.stockDataList = new ArrayList<>();
    }
    
    public void loadDataFromCSV(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // 跳过标题行
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3) {
                    StockDailyData data = new StockDailyData(
                        values[0], 
                        values[1], 
                        values[2]
                    );
                    stockDataList.add(data);
                }
            }
        }
    }
    
    public void analyzeTrends() {
        // 实现趋势分析逻辑
    }
    
    public void generateKLineChart() {
        // 初始化JavaFX应用
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setTitle("K线图");
            
            // 创建K线图
            LineChart<Number, Number> kLineChart = createKLineChart();
            
            Scene scene = new Scene(kLineChart, 800, 600);
            stage.setScene(scene);
            stage.show();
        });
    }
    
    private LineChart<Number, Number> createKLineChart() {
        // 创建坐标轴
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("交易日");
        yAxis.setLabel("价格");
        
        // 创建K线图
        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("K线图");
        
        // 添加数据系列
        XYChart.Series<Number, Number> openSeries = new XYChart.Series<>();
        openSeries.setName("开盘价");
        
        XYChart.Series<Number, Number> highSeries = new XYChart.Series<>();
        highSeries.setName("最高价");
        
        XYChart.Series<Number, Number> lowSeries = new XYChart.Series<>();
        lowSeries.setName("最低价");
        
        XYChart.Series<Number, Number> closeSeries = new XYChart.Series<>();
        closeSeries.setName("收盘价");
        
        // 添加数据点
        for (int i = 0; i < stockDataList.size(); i++) {
            StockData data = stockDataList.get(i);
            openSeries.getData().add(new XYChart.Data<>(i, data.getOpen()));
            highSeries.getData().add(new XYChart.Data<>(i, data.getHigh()));
            lowSeries.getData().add(new XYChart.Data<>(i, data.getLow()));
            closeSeries.getData().add(new XYChart.Data<>(i, data.getClose()));
        }
        
        // 将系列添加到图表
        lineChart.getData().addAll(openSeries, highSeries, lowSeries, closeSeries);
        
        return lineChart;
    }
    
    public static void main(String[] args) {
        StockDataAnalyzer analyzer = new StockDataAnalyzer();
        try {
            analyzer.loadDataFromCSV("d:\\coding\\stock\\20250627_all_stocks_pattern.csv");
            analyzer.analyzeTrends();
            analyzer.generateKLineChart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}