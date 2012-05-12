package database.jpa;

import java.util.Date;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import model.database.jpa.tables.PortfolioEntity;
import model.database.jpa.tables.PortfolioHistory;
import model.database.jpa.tables.StockNames;
import model.database.jpa.tables.StockPrices;

import org.junit.Test;

import testhelpers.DatabaseCleaner;

/**
 * @author Daniel
 *
 * This is mainly a test class for learning JPA.
*/
public class MainBasicJPATest extends DatabaseCleaner {
	
	@Test 
	public void addNewPortfolios() {
		PortfolioEntity portfolio = new PortfolioEntity("portfolio 1");
		portfolio.setAlgorithm("TestAlgorithm1");
		jpaHelper.storeObject(portfolio);
		
		PortfolioEntity portfolio2 = new PortfolioEntity("portfolio 2");
		portfolio.setAlgorithm("TestAlgorithm2");
		jpaHelper.storeObject(portfolio2);
		
		Assert.assertEquals(2, jpaHelper.getAllPortfolios().size());
	}
	@Test
	public void mainTest() {
		
	//__ These two blocks of code are stolen from above, just to provide portfolios 
	//	 to test now that everything is wiped from the DB between tests
		PortfolioEntity portfolio = new PortfolioEntity("portfolio 1");
		portfolio.setAlgorithm("TestAlgorithm1");
		jpaHelper.storeObject(portfolio);
		
		PortfolioEntity portfolio2 = new PortfolioEntity("portfolio 2");
		portfolio.setAlgorithm("TestAlgorithm2");
		jpaHelper.storeObject(portfolio2);
	//
		
		for (PortfolioEntity p : jpaHelper.getAllPortfolios()) {
			jpaHelper.investMoney(10000000, p);
			System.out.println(p);
		}
		
		List<StockNames> stockNames = jpaHelper.getAllStockNames();
		
		if (stockNames.size() == 0) {
			StockNames stockName = new StockNames("Stock1", "MarketA", true);
			jpaHelper.storeObject(stockName);
			
			stockName = new StockNames("Stock2", "MarketB", true);
			jpaHelper.storeObject(stockName);
			
			
			stockName = new StockNames("Stock3", "MarketB", true);
			jpaHelper.storeObject(stockName);
			
			stockName = new StockNames("Stock4", "MarketA", true);
			jpaHelper.storeObject(stockName);
			
			stockName = new StockNames("Stock5", "MarketB", true);
			jpaHelper.storeObject(stockName);
			
			stockName = new StockNames("Stock6", "MarketB", true);
			jpaHelper.storeObject(stockName);
			
			stockName = new StockNames("Stock7", "MarketB", true);
			jpaHelper.storeObject(stockName);
			
			stockName = new StockNames("Stock8", "MarketB", true);
			jpaHelper.storeObject(stockName);
			
			stockName = new StockNames("Stock9", "MarketB", true);
			jpaHelper.storeObject(stockName);
			
			stockName = new StockNames("Stock10", "MarketB", true);
			jpaHelper.storeObject(stockName);
			
			stockNames = jpaHelper.getAllStockNames(); 
		}
		
		Random r = new Random(System.currentTimeMillis());
		
		// create one stockPrice for each stockName
		for (StockNames stockName : stockNames) {
			StockPrices sp = new StockPrices(stockName, r.nextInt(1000), r.nextInt(1000), r.nextInt(1000), r.nextInt(1000), new Date(System.currentTimeMillis()));
			jpaHelper.storeObject(sp);
		}
		
		StockPrices aStock = jpaHelper.getAllStockPrices().get(0);
		
		PortfolioEntity p = jpaHelper.getAllPortfolios().get(0);
		
		if (r.nextBoolean())
			p.addPortfolioHistory(new PortfolioHistory(aStock, new Date(System.currentTimeMillis()), 10, p));
		else
			p.addPortfolioHistory(new PortfolioHistory(aStock, new Date(System.currentTimeMillis()-10000), 10, p));
		jpaHelper.updateObject(p);
		
		List<StockPrices> prices = jpaHelper.getAllStockPrices();
		
		for (StockPrices s : prices) {
			System.out.println(s);
		}
		
		List<StockPrices> bla = jpaHelper.getCurrentStocks(jpaHelper.getAllPortfolios().get(0));
		
		System.out.println("Current nr of stocks: " + bla.size());
		
		jpaHelper.getAllPortfolios().get(0);
		
		System.out.println(jpaHelper.getOldStocks(jpaHelper.getAllPortfolios().get(0)).size());
		System.out.println(p.getTotalInvestedAmount());
		System.out.println(jpaHelper.getStockInfo(10).size() + " " + jpaHelper.getStockInfo(10).get(0).getRight().size());
		
		for (StockPrices sp2 : jpaHelper.getStockInfo(10).get(0).getRight()) {
			System.out.println(sp2);
		}
		
		//-------- Test get latest StockPrice from any stockPrice
		StockPrices old = jpaHelper.getAllStockPrices().get(0);
		StockPrices newestPrice = jpaHelper.getLatestStockPrice(jpaHelper.getAllStockPrices().get(0));
		System.out.println("Given stockPrice: " + old);
		System.out.println("Latest stockPrice: " + newestPrice);
		//------
		
		System.out.println();
		
		for (PortfolioHistory phistory :  p.getHistory()) {
			System.out.println(phistory);
		}
		
		System.out.println();
		
		StockPrices stock = new StockPrices(jpaHelper.getAllStockNames().get(0), 123, 123, 123, 123, new Date(1233));
		
		jpaHelper.storeObject(stock);
		
		p.addPortfolioHistory(new PortfolioHistory(stock, new Date(123), 77, p));
		
		PortfolioHistory pHistory = p.getSpecificPortfolioHistory(stock, 77);
		
		System.out.println(pHistory);
		
		List<StockPrices> ble = jpaHelper.getCurrentStocks(jpaHelper.getAllPortfolios().get(0));
		
		for (StockPrices sp : ble) {
			System.out.println(sp.getTime());
		}
		
		jpaHelper.remove(stock);
	}
}