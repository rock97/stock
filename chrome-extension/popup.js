// 存储自选股票列表
let stockList = [];

// 获取DOM元素
const stockListElement = document.getElementById('stockList');
const stockCodeInput = document.getElementById('stockCode');
const addStockButton = document.getElementById('addStock');
const updateTimeElement = document.getElementById('update-time');

// 从storage中加载自选股票
chrome.storage.local.get(['stocks'], function(result) {
  if (result.stocks) {
    stockList = result.stocks;
    updateStockList();
  }
});

// 添加股票
addStockButton.addEventListener('click', function() {
  const code = stockCodeInput.value.trim();
  if (code && !stockList.includes(code)) {
    stockList.push(code);
    chrome.storage.local.set({ stocks: stockList });
    stockCodeInput.value = '';
    updateStockList();
  }
});

// 更新股票列表
async function updateStockList() {
  stockListElement.innerHTML = '';
  
  for (const code of stockList) {
    try {
      const data = await fetchStockData(code);
      const stockItem = createStockElement(data);
      stockListElement.appendChild(stockItem);
    } catch (error) {
      console.error('获取股票数据失败:', error);
    }
  }
  
  updateTimeElement.textContent = `更新时间: ${new Date().toLocaleTimeString()}`;
}

// 获取股票数据
async function fetchStockData(code) {
  const response = await fetch(`https://api.doctorxiong.club/v1/stock?code=${code}`);
  const data = await response.json();
  return data.data[0];
}

// 创建股票元素
function createStockElement(stock) {
  const div = document.createElement('div');
  div.className = 'stock-item';
  
  const changePercent = parseFloat(stock.priceChange);
  const changeClass = changePercent >= 0 ? 'up' : 'down';
  const changeSymbol = changePercent >= 0 ? '+' : '';
  
  div.innerHTML = `
    <div class="stock-info">
      <div class="stock-name">${stock.name}</div>
      <div class="stock-code">${stock.code}</div>
    </div>
    <div>
      <div class="stock-price">${stock.price}</div>
      <div class="stock-change ${changeClass}">${changeSymbol}${stock.priceChange}%</div>
    </div>
    <button onclick="removeStock('${stock.code}')" style="margin-left: 8px; padding: 2px 6px;">删除</button>
  `;
  
  return div;
}

// 删除股票
function removeStock(code) {
  stockList = stockList.filter(item => item !== code);
  chrome.storage.local.set({ stocks: stockList });
  updateStockList();
}

// 定时更新数据
setInterval(updateStockList, 10000);

// 初始更新
updateStockList();