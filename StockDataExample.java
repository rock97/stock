import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 股票数据获取示例程序
 * 展示如何使用SinaStockDataFetcher获取多只股票的日线数据
 */
public class StockDataExample {

    public static void main(String[] args) {
        // 创建股票数据获取器
        SinaStockDataFetcher fetcher = new SinaStockDataFetcher();
        
        // 设置日期范围
        LocalDate endDate = LocalDate.now(); // 今天
        LocalDate startDate = endDate.minusMonths(1); // 1个月前
        
        System.out.println("获取时间范围: " + startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + 
                          " 至 " + endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // 定义要获取的股票列表
        String[] stockCodes = {
            "sh000001", // 上证指数
            "sz399001", // 深证成指
            "sh601318", // 中国平安
            "sh600519", // 贵州茅台
            "sz000858"  // 五粮液
        };
        
        // 为每只股票获取数据并保存
        for (String stockCode : stockCodes) {
            System.out.println("\n正在获取 " + stockCode + " 的日线数据...");
            
            // 获取股票日线数据
            List<SinaStockDataFetcher.StockDailyData> dataList = 
                    fetcher.fetchStockDailyData(stockCode, startDate, endDate);
            
            if (dataList.isEmpty()) {
                System.out.println("未获取到 " + stockCode + " 的数据");
                continue;
            }
            
            // 打印最新的5条数据
            System.out.println("最新5条数据:");
            int count = 0;
            for (int i = dataList.size() - 1; i >= 0 && count < 5; i--, count++) {
                System.out.println(dataList.get(i));
            }
            
            // 保存到CSV文件
            String filePath = "./" + stockCode + "_daily_data.csv";
            fetcher.saveToCSV(stockCode, dataList, filePath);
        }
        
        System.out.println("\n所有数据获取完成！");
        
        // 示例：如何使用特定股票代码格式
        System.out.println("\n示例：如何使用不同格式的股票代码");
        demonstrateStockCodeFormats(fetcher, startDate, endDate);
    }
    
    /**
     * 演示不同格式股票代码的使用
     */
    private static void demonstrateStockCodeFormats(SinaStockDataFetcher fetcher, 
                                                  LocalDate startDate, 
                                                  LocalDate endDate) {
        // 不同格式的股票代码
        String[] differentFormatCodes = {
            "sh600000",  // 浦发银行 (带市场前缀)
            "600000",    // 浦发银行 (仅代码)
            "000001",    // 平安银行 (仅代码)
            "sz000001"   // 平安银行 (带市场前缀)
        };
        
        for (String code : differentFormatCodes) {
            System.out.println("获取股票代码 " + code + " 的数据...");
            List<SinaStockDataFetcher.StockDailyData> dataList = 
                    fetcher.fetchStockDailyData(code, startDate, endDate.minusDays(20));
            
            if (!dataList.isEmpty()) {
                System.out.println("获取到 " + dataList.size() + " 条数据，最新日期: " + 
                                 dataList.get(dataList.size() - 1).getDate());
            } else {
                System.out.println("未获取到数据");
            }
        }
    }
}