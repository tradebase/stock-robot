package model.simulation;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.global.Log;
import utils.global.Pair;
import utils.global.Log.TAG;

import model.algorithms.IAlgorithm;
import model.algorithms.loader.PluginAlgortihmLoader;
import model.database.jpa.IJPAHelper;
import model.database.jpa.JPAHelper;
import model.database.jpa.tables.PortfolioEntity;
import model.database.jpa.tables.PortfolioHistory;
import model.database.jpa.tables.StockNames;
import model.database.jpa.tables.StockPrices;
import model.robot.IRobot_Algorithms;


/**
 * This will be the handler for a simulation of an algorithm.
 * 
 * It will work by creating a separate database for testing and simply 
 * inject all prices from the original stock price tables.
 * 
 * The result will then be given back to the user, but for now sysout will do.
 * 
 * @author Daniel
 */
public class SimulationHandler {
	PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private IJPAHelper jpaSimHelper;
	private IJPAHelper jpaHelper = JPAHelper.getInstance();
	
	private PortfolioSimulator portfolio = null;

	private IRobot_Algorithms robotSim = new RobotSimulator();
	
	private int progress = 0;
	
	public SimulationHandler() {
		jpaSimHelper = robotSim.getJPAHelper();
		
		clearTestDatabase();
	}
	private void initSimulation(String algorithmToSimulate, List<Pair<String, Long>> longSettings, List<Pair<String, Double>> doubleSettings) {
		PortfolioEntity portfolioEntity = new PortfolioEntity("Simulated Portfolio");
		portfolioEntity.setAlgorithm(algorithmToSimulate);
		
		jpaSimHelper.storeObject(portfolioEntity);
		
		portfolio = new PortfolioSimulator(portfolioEntity, jpaSimHelper);
		
		PluginAlgortihmLoader algorithmLoader = PluginAlgortihmLoader.getInstance();
		
		IAlgorithm algorithm = algorithmLoader.loadAlgorithm(algorithmLoader.getAlgorithmNames().get(0));
		
		algorithm.setPortfolio(portfolio);
		algorithm.setRobot(robotSim);
		
		portfolio.setAlgorithm(algorithm);
		
		if (longSettings != null)
			algorithm.giveLongSettings(longSettings);
		if (doubleSettings != null)
			algorithm.giveDoubleSettings(doubleSettings);
	}
	/**
	 * Tests a given algorithm with real data copied from the original database.
	 * @param algorithmToSimulate Algorithm to simulate
	 * @param howManyStocksBack How many stocks back in time should be copied.
	 * @return Returns the % difference
	 */
	public double simulateAlgorithm(String algorithmToSimulate, int howManyStocksBack,
			List<Pair<String, Long>> longSettings, List<Pair<String, Double>> doubleSettings) {
		
		
		initSimulation(algorithmToSimulate, longSettings, doubleSettings);
		
		long startingBalance = Long.valueOf("10000000000000");
		
		PortfolioEntity port =  portfolio.getPortfolioTable();
		port.invest(startingBalance, true);
		
		jpaSimHelper.updateObject(port);
		
		Map<String, StockNames> nameStockNameMap = new HashMap<String, StockNames>();
		
		for (StockNames ns : jpaHelper.getAllStockNames()) {
			StockNames n = new StockNames(ns.getName(), ns.getMarket());
			jpaSimHelper.storeObject(n);
			nameStockNameMap.put(n.getName(), n);
		}
		
		Date lastSeenTime = null;
		long curr = 0;
		long max = jpaHelper.getStockPricesReverseOrdered(howManyStocksBack).size();
		
		Log.instance().log(TAG.VERY_VERBOSE, max + " stocks will be tested.");
		
		List<StockPrices> stockPrices = new ArrayList<StockPrices>();
		for (StockPrices p : jpaHelper.getStockPricesReverseOrdered(howManyStocksBack)) {
			curr++;
			if (curr%5 == 0) {
				setProgress((int) (((double)curr/(double)max)*100));
			}
			
			if (p.getTime().equals(lastSeenTime) || lastSeenTime == null) {
				StockPrices sp = new StockPrices(nameStockNameMap.get(p.getStockName().getName()), 
						p.getVolume(), p.getLatest(), p.getBuy(), p.getSell(), new Date(p.getTime().getTime()));
				stockPrices.add(sp);
				
				lastSeenTime = p.getTime();
			}
			else {
				lastSeenTime = p.getTime();
				
				StockPrices sp = new StockPrices(nameStockNameMap.get(p.getStockName().getName()), 
						p.getVolume(), p.getLatest(), p.getBuy(), p.getSell(), new Date(p.getTime().getTime()));
				
				stockPrices.add(sp);
				
				jpaSimHelper.storeListOfObjects(stockPrices);
				
				updateAlgorithm();
			}
			if (curr>=max)
				break;
		}
		setProgress(100);
		Log.instance().log(TAG.VERBOSE, "Simulation before selling of stocks: Current balance: " + portfolio.getPortfolioTable().getBalance());
		
		for (PortfolioHistory ph : portfolio.getPortfolioTable().getHistory()) {
			if (ph.getSoldDate() == null) {
				Log.instance().log(TAG.VERY_VERBOSE, "Simulation: Selling " + ph.getAmount() + " of " + ph.getStockPrice().getStockName().getName());
				robotSim.getTrader().sellStock(ph.getStockPrice(), ph.getAmount(), portfolio.getPortfolioTable());
			}
		}

		Log.instance().log(TAG.VERBOSE, "Simulation balance: " + portfolio.getPortfolioTable().getBalance());

		return ((double)portfolio.getPortfolioTable().getBalance()/(double)startingBalance)*100;
	}
	private void setProgress(int i) {
		firePropertyChange("Progress", progress, i);
		progress = i;
		System.out.println(i);
	}
	public void clearTestDatabase() {
		while (jpaSimHelper.getAllPortfolios().size() > 0) {
			PortfolioEntity p = jpaSimHelper.getAllPortfolios().get(0);
			
			jpaSimHelper.remove(p);
		}
	    
	    for (StockPrices sp : jpaSimHelper.getAllStockPrices()) {
	    	jpaSimHelper.remove(sp);
	    }
		for (StockNames sn : jpaSimHelper.getAllStockNames()) {
			jpaSimHelper.remove(sn);
		}
	}
	private void updateAlgorithm() {
		portfolio.updateAlgorithm();
	}
	private void end() {
		jpaHelper.getEntityManager().close();
		jpaSimHelper.getEntityManager().close();
	}
	
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}