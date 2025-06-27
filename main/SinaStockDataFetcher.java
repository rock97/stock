package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 新浪财经股票日线数据获取工具
 * 可以获取指定股票的历史日线数据
 */
public class SinaStockDataFetcher {

    private static final String SINA_STOCK_HISTORY_API = "https://quotes.sina.cn/cn/api/jsonp_v2.php/var%%20_%s_day_data=%%20/CN_MarketDataService.getKLineData";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 获取股票历史日线数据
     *
     * @param stockCode 股票代码（如：sh600000或sz000001）
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate   结束日期（格式：yyyy-MM-dd）
     * @return 股票历史日线数据列表
     */
    public List<StockDailyData> fetchStockDailyData(String stockCode, LocalDate startDate, LocalDate endDate) {
        List<StockDailyData> result = new ArrayList<>();
        try {
            // 构建请求URL
            String requestUrl = buildRequestUrl(stockCode, startDate, endDate);

            // 发送HTTP请求
            String responseData = sendHttpRequest(requestUrl);

            // 解析响应数据
            result = parseResponseData(responseData);

        } catch (Exception e) {
            System.err.println("获取股票日线数据失败: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 构建请求URL
     */
    private String buildRequestUrl(String stockCode, LocalDate startDate, LocalDate endDate) {
        String formattedStartDate = startDate.format(DATE_FORMATTER);
        String formattedEndDate = endDate.format(DATE_FORMATTER);

        // 新浪财经API需要的股票代码格式转换
        String sinaStockCode = convertToSinaStockCode(stockCode);

        return String.format(SINA_STOCK_HISTORY_API, sinaStockCode) +
                "?symbol=" + sinaStockCode +
                "&scale=240" + // 日K线
                "&ma=5" + // 5日均线
                "&datalen=60" + // 最大数据长度
                "&from=" + formattedStartDate +
                "&to=" + formattedEndDate;
    }

    /**
     * 转换股票代码为新浪财经API格式
     */
    private String convertToSinaStockCode(String stockCode) {
        // 如果已经是新浪格式，直接返回
        if (stockCode.startsWith("sh") || stockCode.startsWith("sz")) {
            return stockCode;
        }

        // 上证股票以6开头，深证股票以0或3开头
        if (stockCode.startsWith("6")) {
            return "sh" + stockCode;
        } else if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
            return "sz" + stockCode;
        }

        // 默认返回原始代码
        return stockCode;
    }

    /**
     * 发送HTTP请求获取数据
     */
    private String sendHttpRequest(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    /**
     * 解析响应数据
     */
    private List<StockDailyData> parseResponseData(String responseData) {
        List<StockDailyData> result = new ArrayList<>();

        // 提取JSON数据部分
        int startIndex = responseData.indexOf("[");
        int endIndex = responseData.lastIndexOf("]");

        if (startIndex >= 0 && endIndex > startIndex) {
            String jsonData = responseData.substring(startIndex, endIndex + 1);

            // 简单解析JSON数组（避免引入额外的JSON库依赖）
            // 格式:
            // [{"day":"2023-01-01","open":"10.00","high":"10.50","low":"9.80","close":"10.20","volume":"12345678"},
            // ...]
            String[] items = jsonData.split("\\},\\{");

            for (String item : items) {
                // 清理JSON格式字符
                item = item.replace("[", "").replace("]", "").replace("{", "").replace("}", "");

                // 解析每个字段
                String day = extractValue(item, "day");
                String open = extractValue(item, "open");
                String high = extractValue(item, "high");
                String low = extractValue(item, "low");
                String close = extractValue(item, "close");
                String volume = extractValue(item, "volume");

                if (day != null && !day.isEmpty()) {
                    StockDailyData dailyData = new StockDailyData(
                            day,
                            parseDouble(open),
                            parseDouble(high),
                            parseDouble(low),
                            parseDouble(close),
                            parseLong(volume));
                    result.add(dailyData);
                }
            }
        }

        return result;
    }

    /**
     * 从JSON字符串中提取字段值
     */
    private String extractValue(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":\"";
        int startIndex = json.indexOf(pattern);
        if (startIndex >= 0) {
            startIndex += pattern.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex > startIndex) {
                return json.substring(startIndex, endIndex);
            }
        }
        return "";
    }

    /**
     * 安全解析Double值
     */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 安全解析Long值
     */
    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 生成股票涨跌链接字符串
     *
     * @param dataList 股票数据列表
     * @return 涨跌链接字符串，1表示上涨，0表示下跌或平
     */
    public String generatePriceChangeString(List<StockDailyData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return "";
        }

        StringBuilder priceChangeBuilder = new StringBuilder();
        for (StockDailyData data : dataList) {
            priceChangeBuilder.append(data.getPriceChange());
            System.out.println(data);
        }

        return priceChangeBuilder.toString();
    }

    /**
     * 将股票代码和涨跌链接字符串保存到CSV文件
     *
     * @param stockCode 股票代码
     * @param dataList  股票数据列表
     * @param filePath  保存文件路径
     * @param append    是否追加到现有文件
     */
    public void saveToCSV(String stockCode, String stockName, List<StockDailyData> dataList, String filePath,
            boolean append) {
        if (dataList == null || dataList.isEmpty()) {
            System.out.println("没有数据可保存");
            return;
        }

        try {
            List<String> lines = new ArrayList<>();

            // 如果是新文件或不追加，添加CSV头
            if (!append || !Files.exists(Paths.get(filePath))) {
                lines.add("股票代码,股票名称,涨跌字符串");
            }

            // 生成涨跌链接字符串
            String priceChangeString = generatePriceChangeString(dataList);

            // 添加数据行
            lines.add(String.format("%s,%s,%s", stockCode, stockName, priceChangeString));

            // 写入文件（追加模式或覆盖模式）
            if (append && Files.exists(Paths.get(filePath))) {
                Files.write(Paths.get(filePath), lines, java.nio.file.StandardOpenOption.APPEND);
            } else {
                Files.write(Paths.get(filePath), lines);
            }

            System.out.println("成功保存" + stockCode + "的涨跌链接字符串到文件: " + filePath);
        } catch (IOException e) {
            System.err.println("保存数据到CSV文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 主方法，用于测试
     */
    public static void main(String[] args) {
        SinaStockDataFetcher fetcher = new SinaStockDataFetcher();

        // 测试涨停判断逻辑
        testPriceChangeLogic();
        AllStockCode allStockCode = new AllStockCode();

        // 定义要获取的股票列表
        List<StockInfo> stockInfos = allStockCode.getAllStockInfo();

        // 设置日期范围
        LocalDate endDate = LocalDate.now(); // 今天
        LocalDate startDate = endDate.minusMonths(1); // 3个月前

        System.out.println("获取时间范围: " + startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) +
                " 至 " + endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        String dataStr = endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 所有股票数据保存到同一个文件
        String allStocksFilePath = "./" + dataStr + "_all_stocks_pattern.csv";

        // 为每只股票获取数据并保存到同一个文件
        for (int i = 0; i < stockInfos.size(); i++) {
            StockInfo stockInfo = stockInfos.get(i);
            String stockCode = stockInfo.getCode();
            String stockName = stockInfo.getName();
            System.out.println("\n正在获取 " + i + " " + stockName + " 的日线数据...");

            // 获取股票日线数据
            List<StockDailyData> dataList = fetcher.fetchStockDailyData(stockCode, startDate, endDate);

            if (dataList.isEmpty()) {
                System.out.println("未获取到 " + stockName + " 的数据");
                continue;
            }
            // 保存到同一个CSV文件，第一个股票不追加，后续股票追加
            boolean append = (i > 0); // 第一个股票不追加，后续股票追加
            fetcher.saveToCSV(stockCode, stockName, dataList, allStocksFilePath, append);
        }

        System.out.println("\n所有数据已保存到文件: " + allStocksFilePath);
    }

    /**
     * 测试涨跌判断逻辑
     */
    private static void testPriceChangeLogic() {
        System.out.println("\n===== 测试涨跌判断逻辑 =====");

        // 创建测试数据
        StockDailyData data1 = new StockDailyData("2023-01-01", 100.0, 110.0, 95.0, 105.0, 1000000); // 上涨5%
        StockDailyData data2 = new StockDailyData("2023-01-02", 100.0, 90.0, 85.0, 90.0, 1000000); // 下跌10%
        StockDailyData data3 = new StockDailyData("2023-01-03", 100.0, 115.0, 100.0, 110.0, 1000000); // 上涨10%（涨停）
        StockDailyData data4 = new StockDailyData("2023-01-04", 100.0, 100.0, 100.0, 100.0, 1000000); // 平

        // 打印测试结果
        System.out.println("上涨5%: " + data1.getPriceChange() + " (期望值: 1)");
        System.out.println("下跌10%: " + data2.getPriceChange() + " (期望值: 0)");
        System.out.println("上涨10%（涨停）: " + data3.getPriceChange() + " (期望值: 2)");
        System.out.println("平: " + data4.getPriceChange() + " (期望值: 0)");
        System.out.println("===== 测试结束 =====\n");
    }
}