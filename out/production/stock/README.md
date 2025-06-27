# 新浪财经股票日线数据获取工具

这是一个用Java编写的工具，用于获取新浪财经提供的股票日线数据。该工具可以获取指定股票在特定日期范围内的历史交易数据，并支持将数据保存为CSV格式。

## 功能特点

- 支持获取A股市场股票的日线数据
- 支持上证和深证股票代码
- 可指定日期范围获取历史数据
- 支持将数据导出为CSV格式
- 无需额外依赖，使用Java标准库实现

## 使用方法

### 基本用法

```java
// 创建股票数据获取器
SinaStockDataFetcher fetcher = new SinaStockDataFetcher();

// 设置股票代码和日期范围
String stockCode = "sh000001"; // 上证指数
LocalDate startDate = LocalDate.now().minusMonths(1); // 1个月前
LocalDate endDate = LocalDate.now(); // 今天

// 获取股票日线数据
List<StockDailyData> dataList = fetcher.fetchStockDailyData(stockCode, startDate, endDate);

// 打印数据
for (StockDailyData data : dataList) {
    System.out.println(data);
}

// 保存到CSV文件
String filePath = "./" + stockCode + "_daily_data.csv";
fetcher.saveToCSV(stockCode, dataList, filePath);
```

### 股票代码格式

工具支持以下格式的股票代码：

- 带市场前缀的代码：`sh600000`（上海）、`sz000001`（深圳）
- 仅数字的代码：`600000`、`000001`（工具会自动添加市场前缀）

对于仅数字的代码，工具会根据以下规则自动添加市场前缀：
- 以`6`开头的代码添加`sh`前缀（上海市场）
- 以`0`或`3`开头的代码添加`sz`前缀（深圳市场）

### 指数代码

常用指数代码：
- 上证指数：`sh000001`
- 深证成指：`sz399001`
- 创业板指：`sz399006`
- 沪深300：`sh000300`

## 数据格式

获取的每条股票日线数据包含以下字段：

- `date`: 交易日期（格式：YYYY-MM-DD）
- `open`: 开盘价
- `high`: 最高价
- `low`: 最低价
- `close`: 收盘价
- `volume`: 成交量

## 示例程序

项目包含两个Java文件：

1. `SinaStockDataFetcher.java` - 核心功能实现
2. `StockDataExample.java` - 使用示例

运行`StockDataExample`可以获取多只股票的日线数据并保存为CSV文件。

## 编译和运行

```bash
# 编译
javac SinaStockDataFetcher.java StockDataExample.java

# 运行示例程序
java StockDataExample
```

## 注意事项

- 该工具使用新浪财经公开API获取数据，请合理使用，避免频繁请求
- 获取的数据仅供参考，交易决策请以官方数据为准
- 工具不保证数据的完整性和准确性
- 使用该工具时请遵守相关法律法规和新浪财经的使用条款