package database.jpa;


import java.util.Date;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import database.jpa.tables.AlgorithmEntitys;
import database.jpa.tables.PortfolioEntitys;
import database.jpa.tables.StockNames;
import database.jpa.tables.StockPrices;


public class JPATest {
	static JPAHelper jpaHelper;
	static Random rand = new Random(System.currentTimeMillis());
	@BeforeClass
	public static void beforeClass(){ //First of all
		jpaHelper = JPAHelper.getInstance("testdb");
	}
	@Test(expected=Exception.class)
	public void testDuplicateEntry() {
		StockPrices sp = new StockPrices(new StockNames("Stock1", "marketA"), 100, 100, 100, 100, new Date(System.currentTimeMillis()));
		jpaHelper.storeObject(sp);
		jpaHelper.storeObject(sp);
	}
	@Test
	public void testDuplicateSafeEntry() {
		StockNames stockName = new StockNames("Stock2" + rand.nextFloat(), "marketB");
		StockPrices sp = new StockPrices(stockName, 100, 100, 100, 100, new Date(123231));
		System.out.println(sp);
		jpaHelper.storeObjectIfPossible(stockName);
		jpaHelper.storeObjectIfPossible(sp);
		jpaHelper.storeObjectIfPossible(sp);
	}
	@Test
	public void testNewPortfolio() {
		AlgorithmEntitys algorithm = new AlgorithmEntitys("AlgorithmName", "path");
		PortfolioEntitys portfolio = new PortfolioEntitys("Portfolio");
		portfolio.setAlgorithm(algorithm);
		jpaHelper.storeObject(algorithm);
		jpaHelper.storeObject(portfolio);		
	}
	@Test
	public void testNewPortfolioAndDelete() {
		PortfolioEntitys testPortfolio = new PortfolioEntitys("testPortfolio");
		jpaHelper.storeObject(testPortfolio);
		
		jpaHelper.remove(testPortfolio);
		
		for (PortfolioEntitys p : jpaHelper.getAllPortfolios()) {
			System.out.println(jpaHelper.getAllPortfolios().size());
			if (p.getName().contentEquals("testPortfolio"))
				throw new IllegalArgumentException("Still in the system");
		}
	}
	@AfterClass
	public static void afterClass() {
		// TODO: PortfolioInvestment
		// TODO: StocksToWatch
		
		while (jpaHelper.getAllPortfolios().size() > 0) {
			PortfolioEntitys p = jpaHelper.getAllPortfolios().get(0);
			if (p.getHistory() != null)
				if (p.getHistory().iterator().hasNext())
					jpaHelper.remove(p.getHistory().iterator().next());
		}
	    for (AlgorithmEntitys a : jpaHelper.getAllAlgorithms())
			jpaHelper.remove(a);
		
	    
	    for (StockPrices sp : jpaHelper.getAllStockPrices())
	    	jpaHelper.remove(sp);
		for (StockNames sn : jpaHelper.getAllStockNames())
	    	jpaHelper.remove(sn);
	}
}