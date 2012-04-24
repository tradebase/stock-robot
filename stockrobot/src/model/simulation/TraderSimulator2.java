package model.simulation;


import java.beans.PropertyChangeListener;

import utils.global.Log;
import utils.global.Log.TAG;

import model.database.jpa.IJPAHelper;
import model.database.jpa.tables.PortfolioEntity;
import model.database.jpa.tables.PortfolioHistory;
import model.database.jpa.tables.StockPrices;
import model.trader.ITrader;


/**
 * @author Daniel
 *
 */
public class TraderSimulator2 implements ITrader{

	private IJPAHelper jpaHelper;

	public TraderSimulator2(IJPAHelper jpaHelper) {
		this.jpaHelper = jpaHelper;
	}
	@Override
	public void addAddObserver(PropertyChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeObserver(PropertyChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean buyStock(StockPrices s, long amount, PortfolioEntity portfolio) {
		if (amount > 0 && s.getSell()*amount + getCourtagePrice(s, amount, true, portfolio) > portfolio.getBalance()) {
			return false;
		}

		portfolio.bougthFor(s.getSell()*amount + getCourtagePrice(s, amount, true, portfolio), jpaHelper);

		portfolio.addPortfolioHistory(new PortfolioHistory(s, s.getTime(), null, amount, portfolio));
		jpaHelper.updateObject(portfolio);

		return true;
	}

	@Override
	public boolean sellStock(StockPrices s, long amount, PortfolioEntity portfolio) {
		StockPrices latest = jpaHelper.getLatestStockPrice(s);


		for (PortfolioHistory ph : portfolio.getHistory()) {
			if (ph.getSoldDate() == null) {
				if (ph.getStockPrice().getTime() == s.getTime()) {
					if (ph.getStockPrice().getStockName().getName().contentEquals(s.getStockName().getName())) {
						if (ph.getAmount() == amount) {
							portfolio.soldFor(s.getBuy()*amount, jpaHelper);
							ph.setSoldDate(latest.getTime());
							jpaHelper.updateObject(portfolio);
							return true;
						}
					}
				}
			}
		}
		Log.instance().log(TAG.VERY_VERBOSE, "SellStock in traderSimulator2: Couldent find stock: " + s);
		return false;
	}

	@Override
	public long getCourtagePrice(StockPrices s, long amount, boolean buying, PortfolioEntity portfolio) {
		return (long) (s.getSell()*amount*0.09);
	}
	@Override
	public boolean sellStock(PortfolioHistory ph, PortfolioEntity portfolio) {
		if (ph.getSoldDate() != null) {
			Log.instance().log(TAG.ERROR, "Couldent sell stock: " + ph + " it already is sold");
			return false;
		}
		Log.instance().log(TAG.VERBOSE, "Selling " + ph.getAmount() + " of " + ph.getStockPrice().getStockName().getName() + " for: " + ph.getStockPrice().getBuy()*ph.getAmount());
		StockPrices latest = jpaHelper.getLatestStockPrice(ph.getStockPrice());
		portfolio.soldFor(ph.getStockPrice().getBuy()*ph.getAmount(), jpaHelper);
		ph.setSoldDate(latest.getTime());
		jpaHelper.updateObject(portfolio);
		
		return true;
	}
}