package database.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import model.database.jpa.IJPAHelper;
import model.database.jpa.JPAHelperSimulator;
import model.database.jpa.tables.PortfolioEntity;
import model.database.jpa.tables.StockNames;
import model.database.jpa.tables.StockPrices;
import model.scraping.database.IInserter;
import model.scraping.database.JPAInserter;
import model.scraping.model.ParserStock;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;



public class JPATest {
	
	static IJPAHelper jpaHelper;
	static Random rand = new Random(System.currentTimeMillis());
	
	@BeforeClass
	public static void beforeClass(){ //First of all
		jpaHelper = new JPAHelperSimulator();
	}
	@Test(expected=Exception.class)
	public void testDuplicateEntry() {
		StockPrices sp = new StockPrices(new StockNames("Stock1", "marketA"), 100, 100, 100, 100, new Date(System.currentTimeMillis()));
		jpaHelper.storeObject(sp);
		sp = new StockPrices(new StockNames("Stock1", "marketA"), 100, 100, 100, 100, new Date(System.currentTimeMillis()));
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
		PortfolioEntity portfolio = new PortfolioEntity("Portfolio");
		portfolio.setAlgorithm("testtest");
		jpaHelper.storeObject(portfolio);		
	}
	@Test
	public void testNewPortfolioAndDelete() {
		PortfolioEntity testPortfolio = new PortfolioEntity("testPortfolio");
		jpaHelper.storeObject(testPortfolio);
		
		jpaHelper.remove(testPortfolio);
		
		for (PortfolioEntity p : jpaHelper.getAllPortfolios()) {
			System.out.println(jpaHelper.getAllPortfolios().size());
			if (p.getName().contentEquals("testPortfolio"))
				throw new IllegalArgumentException("Still in the system");
		}
	}
	@Test
	public void testAddStocks() {
		List<ParserStock> list = new ArrayList<ParserStock>(); 
		
		for (int i = 0; i < 100; i++) {
			ParserStock ps = new ParserStock("ParserStock" + i);
			ps.setDate(new Date(1231231));
			ps.setBuy(123);
			ps.setSell(123);
			ps.setLastClose(321);
			ps.setMarket("Market" + i%5);
			ps.setVolume(1233);
			
			list.add(ps);
		}
		// Should add 100 new stocks
		IInserter jpaInserter = new JPAInserter(jpaHelper);
		
		Assert.assertEquals(100, jpaInserter.insertStockData(list));
		
		// Should not be able to add any new stocks
		Assert.assertEquals(0, jpaInserter.insertStockData(list));
		
		list.clear();
		for (int i = 0; i < 100; i++) {
			ParserStock ps = new ParserStock("ParserStock" + i);
			ps.setDate(new Date(new Long("1231231231233")));
			ps.setBuy(123);
			ps.setSell(123);
			ps.setLastClose(321);
			ps.setMarket("Market" + i%5);
			ps.setVolume(1233);
			
			list.add(ps);
		}
		// Should add 100 new stocks
		Assert.assertEquals(100, jpaInserter.insertStockData(list));
		
		// Should not be able to add any new stocks
		Assert.assertEquals(0, jpaInserter.insertStockData(list));
		
		
		// Test if all the stocks in the list is in getLatestStockPrices
		for (ParserStock ps : list) {
			boolean found = false;
			for (StockPrices sp : jpaHelper.getLatestStockPrices()) {
				if (sp.getTime().equals(ps.getDate())) {
					if (sp.getStockName().getName().contentEquals(ps.getName())) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void testPortfolioInvestment() {
		long amountToInvest = 10000;
		PortfolioEntity p = new PortfolioEntity("portfolioInvestment");
		jpaHelper.storeObject(p);
		
		p.invest(amountToInvest, true);
		jpaHelper.updateObject(p);
		
		Assert.assertEquals(amountToInvest, p.getBalance());
	}
	@Test
	public void testStockNamesGetPrices() {
		boolean found = false;
		for (StockNames sn : jpaHelper.getAllStockNames()) {
			if (sn.getStockPrices() != null) {
				found = true;
			}
		}
		if (!found) {
			Assert.fail();
		}
	}
	/**
	 * Removes all entitys from the database
	 */
	@AfterClass
	public static void afterClass() {
		while (jpaHelper.getAllPortfolios().size() > 0) {
			PortfolioEntity p = jpaHelper.getAllPortfolios().get(0);
			jpaHelper.remove(p);
		}
	    for (StockPrices sp : jpaHelper.getAllStockPrices()) {
	    	jpaHelper.remove(sp);
	    }
		for (StockNames sn : jpaHelper.getAllStockNames()) {
	    	jpaHelper.remove(sn);
		}
	}
}