// 设置定时任务，每隔一定时间更新股票数据
chrome.alarms.create('updateStocks', {
  periodInMinutes: 1
});

// 监听定时任务
chrome.alarms.onAlarm.addListener(async (alarm) => {
  if (alarm.name === 'updateStocks') {
    // 从storage中获取自选股票列表
    const result = await chrome.storage.local.get(['stocks']);
    const stockList = result.stocks || [];

    // 更新每个股票的数据
    for (const code of stockList) {
      try {
        const response = await fetch(`https://api.doctorxiong.club/v1/stock?code=${code}`);
        const data = await response.json();
        const stock = data.data[0];

        // 检查是否需要发送通知
        checkAndNotify(stock);
      } catch (error) {
        console.error('获取股票数据失败:', error);
      }
    }
  }
});

// 检查股票数据并发送通知
function checkAndNotify(stock) {
  const changePercent = parseFloat(stock.priceChange);
  
  // 涨跌幅超过5%发送通知
  if (Math.abs(changePercent) >= 5) {
    const type = changePercent > 0 ? '上涨' : '下跌';
    chrome.notifications.create({
      type: 'basic',
      iconUrl: 'images/icon128.png',
      title: '股票提醒',
      message: `${stock.name}(${stock.code}) ${type}${Math.abs(changePercent)}%`
    });
  }
}