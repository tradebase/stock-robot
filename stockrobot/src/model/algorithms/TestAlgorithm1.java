package model.algorithms;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.global.Log;
import utils.global.Pair;
import utils.global.Log.TAG;

import model.database.jpa.IJPAAlgortihm;
import model.database.jpa.tables.AlgorithmSettingDouble;
import model.database.jpa.tables.AlgorithmSettingLong;
import model.database.jpa.tables.PortfolioHistory;
import model.database.jpa.tables.StockNames;
import model.database.jpa.tables.StockPrices;
import model.portfolio.IPortfolio;
import model.robot.IRobot_Algorithms;
import model.trader.ITrader;


/**
 * A simple test algorithm based on a algorithm Kristian has come up with
 * 
 * @author daniel
 */
@AlgorithmPlugin(name="TestAlgorithm1")
public class TestAlgorithm1 implements IAlgorithm{

	IRobot_Algorithms robot;
	IPortfolio portfolio;
	ITrader trader = null;
	IJPAAlgortihm jpaHelper = null;
	private long buySetting = 3;
	private long sellSetting = 3;

	public TestAlgorithm1() {
		Log.instance().log(TAG.VERY_VERBOSE, "Inside TestAlgorithm constructor");
	}
	public TestAlgorithm1(IRobot_Algorithms robot, IPortfolio portfolio, ITrader trader) {
		this.robot = robot;
		this.portfolio = portfolio;
		this.trader = trader;
		this.jpaHelper = robot.getJPAHelper();

		Log.instance().log(TAG.VERY_VERBOSE, "Inside TestAlgorithm constructor");
	}

	@Override
	public boolean update() {

		Log.instance().log( Log.TAG.VERY_VERBOSE, "Algo1: UPDATE!" );

		if (portfolio.getPortfolioTable().getBalance() < 1000000) {
			return false;
		}



		for (PortfolioHistory ph : jpaHelper.getCurrentStocksHistory(portfolio.getPortfolioTable())) {
			List<StockPrices> cs = jpaHelper.getNLatest(ph.getStockPrice(), (int)sellSetting);
			if (cs.size() == sellSetting) {
				long last = Long.MAX_VALUE;
				boolean sell = true;
				for (int i = (int)sellSetting-1; i >= 0; i--) {
					if (cs.get(i).getBuy() < last) {
						last = cs.get(i).getBuy();
					}
					else
						sell = false;
				}

				if (sell) {
					trader.sellStock(ph, portfolio.getPortfolioTable());
				}
			}
		}
		for (Pair<StockNames, List<StockPrices>> stockInfo: jpaHelper.getStockInfo((int)buySetting)) {
			if (stockInfo.getRight().size() == (int) buySetting) {

				boolean buy = true;
				long last = Long.MAX_VALUE;
				for (int i = 0; i < stockInfo.getRight().size(); i++) {

					if (stockInfo.getRight().get(i).getBuy() >= last)
						buy = false;
					last = stockInfo.getRight().get(i).getBuy();
				}

				//Buy!
				if (buy) {
					Log.instance().log( Log.TAG.VERY_VERBOSE, "Algo1: BUY!" );
					//Buy a couple of stock, if the stockprice is NOT zero (avoid divide by zero)
					long firstStockBuyPrice = stockInfo.getRight().get(0).getBuy();
					if( firstStockBuyPrice != 0 ) {

						long amount = (long) (portfolio.getPortfolioTable().getBalance()/10/firstStockBuyPrice); 

						if (amount > 0)
							buyStock( stockInfo.getRight().get(0), amount );
					}
				}
			}
		}

		return true;
	}
	private void buyStock(StockPrices stockPrices, long amount) {
		trader.buyStock(stockPrices, amount, portfolio.getPortfolioTable());

	}

	@Override
	public String getName() {
		return this.getClass().getAnnotation(AlgorithmPlugin.class).name();
	}
	@Override
	public String getDescription() {
		return "Test algorithm1\n\nBuys when a stock has gone up 3 times in a row\nSells when a stock has gone down 3 times in a row";
	}

	@Override
	public Set<AlgorithmSettingDouble> getDefaultDoubleSettings() {
		Set<AlgorithmSettingDouble> doubleSettings = new HashSet<AlgorithmSettingDouble>();
		return doubleSettings;
	}

	@Override
	public Set<AlgorithmSettingLong> getDefaultLongSettings() {
		Set<AlgorithmSettingLong> longSettings = new HashSet<AlgorithmSettingLong>();
		longSettings.add(new AlgorithmSettingLong("buy", 3, "Number of times a stock has to climb before buying", 1, 1, 100));
		longSettings.add(new AlgorithmSettingLong("sell", 3, "Number of times a stock has to drop before selling", 2, 1, 100));
		return longSettings;
	}

	@Override
	public boolean giveDoubleSettings(List<Pair<String, Double>> doubleSettings) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean giveLongSettings(List<Pair<String, Long>> longSettings) {
		for (Pair<String, Long> setting : longSettings) {
			if (setting.getLeft().contentEquals("buy")) {
				Log.instance().log(TAG.VERY_VERBOSE, "Buy set to: " + setting.getRight());
				this.buySetting = setting.getRight();
			}
			else if (setting.getLeft().contentEquals("sell")) {
				Log.instance().log(TAG.VERY_VERBOSE, "Sell set to: " + setting.getRight());
				this.sellSetting = setting.getRight();
			}
			else 
				return false;
		}
		return true;
	}
	@Override
	public void setRobot(IRobot_Algorithms robot) {
		this.robot = robot;
		this.jpaHelper = robot.getJPAHelper();
		this.trader = robot.getTrader();
	}
	@Override
	public void setPortfolio(IPortfolio p) {
		this.portfolio = p;
	}
}