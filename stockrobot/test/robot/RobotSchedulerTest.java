package robot;

import static org.junit.Assert.*;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import model.database.jpa.IJPAHelper;
import model.database.jpa.tables.PortfolioEntity;
import model.database.jpa.tables.StockNames;
import model.database.jpa.tables.StockPrices;
import model.portfolio.IAlgorithm;
import model.portfolio.IPortfolio;
import model.portfolio.IPortfolioHandler;
import model.portfolio.IRobot_Algorithms;
import model.portfolio.Portfolio;
import model.robot.RobotScheduler;
import model.trader.ITrader;

import org.junit.Test;

import testhelpers.DatabaseCleaner;
import utils.global.Log;
import utils.global.Pair;

/**
 * @author Mattias Markehed
 * mattias.markehed@gmail.com
 *
 * filename: RobotSchedulerTest.java
 * Description:
 * RobotSchedulerTest runs different tests on RobotScheduler to verify that it's
 * Functioning correctly and new modifications doesn't break the class.
 */
public class RobotSchedulerTest extends DatabaseCleaner implements IRobot_Algorithms{
	@Test
	public void testRobotSchedulerClient() {
		Log.setFilter(Log.TAG.DEBUG, true);
		Log.setFilter(Log.TAG.VERY_VERBOSE, true);
		Log.log(Log.TAG.NORMAL, "Starting pauseUnpauseTest");
		
		Random rand = new Random(System.currentTimeMillis());
		int port = rand.nextInt(5000) + 1025;
		
		RobotSchedulerServerTest testServer = new RobotSchedulerServerTest(port);
		testServer.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		Log.log(Log.TAG.NORMAL, "Creating RobotScheduler");
		TestPortfolioHandler pHandler = new TestPortfolioHandler(null);
		RobotScheduler schedueler = null;
		try {
			schedueler = new RobotScheduler(pHandler, "127.0.0.1", port);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		Log.log(Log.TAG.NORMAL, "Starting RobotScheduler");
		Thread tSched = new Thread(schedueler);
		
		tSched.start();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		
		Assert.assertTrue(testServer.sendSignal());
		
		testServer.kill();
	}
	
	@Test
	public void pauseUnpauseTest() {
		Log.setFilter(Log.TAG.DEBUG, true);
		Log.setFilter(Log.TAG.VERY_VERBOSE, true);
		Log.log(Log.TAG.NORMAL, "Starting pauseUnpauseTest");
		
		TestPortfolioHandler pHandler = new TestPortfolioHandler(null);
		RobotScheduler schedueler = new RobotScheduler(pHandler);
		schedueler.setPauseLength(RobotScheduler.MILLI_SECOND*1000);
		Thread tSched = new Thread(schedueler);

		tSched.start();
		schedueler.pause();
		Assert.assertTrue(schedueler.isPaused()); //TODO: this test fails
		schedueler.unpause();
		Assert.assertFalse(schedueler.isPaused());
	}
	/**
	 * Test the regular start, stop, pause and unpause for
	 * empty scheduler.
	 */
	@Test
	public void emptyRunTest() {
				
		Log.setFilter(Log.TAG.DEBUG, true);
		Log.setFilter(Log.TAG.VERY_VERBOSE, true);
		
		Log.log(Log.TAG.NORMAL, "Starting emptyRunTest");
		
		TestPortfolioHandler pHandler = new TestPortfolioHandler(null);
		RobotScheduler schedueler = new RobotScheduler(pHandler);
		schedueler.setUpdateFrequency(RobotScheduler.MILLI_SECOND*10);
		schedueler.setPauseLength(RobotScheduler.MILLI_SECOND);
		Thread tSched = new Thread(schedueler);
		assertFalse(schedueler.stop());
		assertFalse(schedueler.pause());
		assertFalse(schedueler.unpause());
		tSched.start();
		
		try {
			Thread.sleep(RobotScheduler.MILLI_SECOND*50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		schedueler.stop();
		try {
			Thread.sleep(RobotScheduler.MILLI_SECOND*50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertFalse(tSched.isAlive());
	}
	
	/**
	 * Test the regular start, stop, pause and unpause for
	 * scheduler running one algorithm.
	 */
	@Test
	public void singleRunTest() {
				
		Log.setFilter(Log.TAG.DEBUG, true);
		Log.setFilter(Log.TAG.VERY_VERBOSE, true);
		
		Log.log(Log.TAG.NORMAL, "Starting singleRunTest");
		
		JUnitAlgorithm algorithm = new JUnitAlgorithm(RobotScheduler.MILLI_SECOND);
		IPortfolio testPortfolio = new TestPortfolio("test1",algorithm);
		
		List<IPortfolio> portfolios = new LinkedList<IPortfolio>();
		portfolios.add(testPortfolio);
		
		TestPortfolioHandler pHandler = new TestPortfolioHandler(portfolios);
		RobotScheduler schedueler = new RobotScheduler(pHandler);
		schedueler.setUpdateFrequency(RobotScheduler.MILLI_SECOND*10);
		schedueler.setPauseLength(RobotScheduler.MILLI_SECOND);
		Thread tSched = new Thread(schedueler);
		tSched.start();
		try {
			Thread.sleep(RobotScheduler.MILLI_SECOND*500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(algorithm.getUpdatedNrTimes() > 0); //TODO: this test fails
		schedueler.stop();
		try {
			Thread.sleep(RobotScheduler.MILLI_SECOND*50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertFalse(tSched.isAlive());
	}
	
	/**
	 * Test the regular start, stop, pause and unpause for
	 * scheduler running multiple algorithm.
	 */
	@Test
	public void multipleRunTest() {
				
		Log.setFilter(Log.TAG.DEBUG, true);
		Log.setFilter(Log.TAG.VERY_VERBOSE, true);
		
		Log.log(Log.TAG.NORMAL, "Starting singleRunTest");
		
		JUnitAlgorithm algorithm = new JUnitAlgorithm(RobotScheduler.MILLI_SECOND);
		IPortfolio testPortfolio = new TestPortfolio("test1",algorithm);
		
		JUnitAlgorithm algorithm2 = new JUnitAlgorithm(RobotScheduler.MILLI_SECOND*3);
		IPortfolio testPortfolio2 = new TestPortfolio("test2",algorithm2);
		
		JUnitAlgorithm algorithm3 = new JUnitAlgorithm(RobotScheduler.MILLI_SECOND*10);
		IPortfolio testPortfolio3 = new TestPortfolio("test3",algorithm3);
		
		JUnitAlgorithm algorithm4 = new JUnitAlgorithm(RobotScheduler.MILLI_SECOND*100);
		IPortfolio testPortfolio4 = new TestPortfolio("test4",algorithm4);
		
		List<IPortfolio> portfolios = new LinkedList<IPortfolio>();
		portfolios.add(testPortfolio);
		portfolios.add(testPortfolio2);
		portfolios.add(testPortfolio3);
		portfolios.add(testPortfolio4);
		
		TestPortfolioHandler pHandler = new TestPortfolioHandler(portfolios);
		RobotScheduler schedueler = new RobotScheduler(pHandler);
		schedueler.setUpdateFrequency(RobotScheduler.MILLI_SECOND*10);
		schedueler.setPauseLength(RobotScheduler.MILLI_SECOND);
		Thread tSched = new Thread(schedueler);
		tSched.start();
		
		try {
			Thread.sleep(RobotScheduler.MILLI_SECOND*500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(algorithm.getUpdatedNrTimes() > 2); //TODO: these test fails
		assertTrue(algorithm2.getUpdatedNrTimes() > 2);
		assertTrue(algorithm3.getUpdatedNrTimes() > 2);
		assertTrue(algorithm4.getUpdatedNrTimes() > 1);

		
		schedueler.pause();
		schedueler.pause();
		assertTrue(tSched.isAlive());
		assertTrue(schedueler.isPaused());
		schedueler.unpause();
		assertFalse(schedueler.isPaused());
		schedueler.stop();
		try {
			Thread.sleep(RobotScheduler.MILLI_SECOND*50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertFalse(tSched.isAlive());
	}
	
	/**
	 * Scaled of portfolio 
	 */
	private class TestPortfolio implements IPortfolio{

		private IAlgorithm algorithm;
		private String name;
		
		public TestPortfolio(String name, IAlgorithm algorithm){
			this.algorithm = algorithm;
			this.name = name;
		}
		
		@Override
		public List<StockNames> getAvalibleStocks() {
			return null;
		}

		@Override
		public List<StockPrices> getCurrentStocks() {
			return null;
		}

		@Override
		public List<Pair<StockPrices, StockPrices>> getHistoryStocks() {
			return null;
		}
		
		@Override
		public boolean setAlgorithm(IAlgorithm algorithm) {
			this.algorithm = algorithm;
			
			return true;
		}

		@Override
		public long getInvestedAmount() {
			return 0;
		}

		@Override
		public long getUnusedAmount() {
			return 0;
		}

		@Override
		public boolean investAmount(long n) {
			return false;
		}

		@Override
		public void stopBuying(boolean flag) {
			
		}

		@Override
		public void stopSelling(boolean flag) {
		
		}

		@Override
		public boolean isStopBuyingFlagSet() {
			return false;
		}

		@Override
		public boolean isStopSellingFlagSet() {
			return false;
		}
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return "TestPortfolio " + name + "\n";
		}
		@Override
		public PortfolioEntity getPortfolioTable() {
			return null;
		}

		@Override
		public boolean updateAlgorithm() {
			this.algorithm.update();
			return false;
		}

		@Override
		public long getCurrentWorth() {
			return 0;
		}

		@Override
		public IAlgorithm getAlgorithm() {
			return algorithm;
		}

		@Override
		public void updateSettings() {
		}
	}
	

	private class TestPortfolioHandler implements IPortfolioHandler{

		private List<IPortfolio> listOfPortfolios = new ArrayList<IPortfolio>();
		
		public TestPortfolioHandler(List<IPortfolio> portfolios) {
					
			if(portfolios != null)
				listOfPortfolios.addAll(portfolios);
		}
		@Override
		public IPortfolio createNewPortfolio(String name) {
			PortfolioEntity pt = new PortfolioEntity(name);
			return new Portfolio(pt);
		}

		@Override
		public List<IPortfolio> getPortfolios() {
			return listOfPortfolios;
		}

		@Override
		public boolean removePortfolio(IPortfolio portfolio) {
		
			return false;
		}
	
		@Override
		public void addObserver(PropertyChangeListener listener) {
		}
		
		@Override
		public void removeObserver(PropertyChangeListener listener) {
		}
		
		@Override
		public boolean setAlgorithm(IPortfolio p, String algorithmName) { //NOPMD
			// Not important in this test
			return false;
		}
	}

	@Override
	public IJPAHelper getJPAHelper() { //NOPMD
		
		return null;
	}

	@Override
	public ITrader getTrader() { //NOPMD
		
		return null;
	}
}