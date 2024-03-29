package model.portfolio;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import utils.global.FinancialLongConverter;
import utils.global.LongContainer;
import utils.global.Pair;
import model.database.jpa.IJPAHelper;
import model.database.jpa.JPAHelper;
import model.database.jpa.tables.PortfolioEntity;
import model.database.jpa.tables.PortfolioHistory;
import model.database.jpa.tables.PortfolioInvestment;
import model.database.jpa.tables.StockNames;
import model.database.jpa.tables.StockPrices;

/**
 * The model for PortfolioHistory.
 * 
 * @author Daniel
 */
public class PortfolioHistoryModel implements PropertyChangeListener {
	private IJPAHelper jpaHelper;

	private PortfolioEntity selectedPortfolio;// = jpaHelper.getAllPortfolios().get(0);
	private List<PortfolioHistory> history;
	private Map<String, TimeSeries> allTimeSeries = new HashMap<String, TimeSeries>();
	private Pair<Object[][], Object[]> tableDate;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	private PortfolioHistoryModelStatistics historyStatistics;

	public static final String STATISTICSUPDATED = "StatisticsUpdated";

	public PortfolioHistoryModel(PortfolioEntity model) {
		this.selectedPortfolio = model;
	}
	public void startGeneratingPortfolioDate() {
		if (jpaHelper == null)
			jpaHelper = JPAHelper.getInstance();
		loadHistory();

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				generateTableData();
				propertyChangeSupport.firePropertyChange("Table Generated", null, getTable());
			}
		});
		t.start();

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				generateTimeSeries();
				propertyChangeSupport.firePropertyChange("Time Series", null, allTimeSeries);
			}
		});
		t2.start();

		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				generateStatistics();
			}
		});
		t3.start();

	}
	private void generateStatistics() {
		historyStatistics = new PortfolioHistoryModelStatistics();
		historyStatistics.addPropertyChangeListener(this);
		historyStatistics.generateStatistics(history, selectedPortfolio);
		historyStatistics.removePropertyChangeListener(this);
	}
	public PortfolioEntity getSelectedPortfolio() {
		return selectedPortfolio;
	}
	public void cleanup() {
		allTimeSeries.clear();
		this.history = null;
		historyStatistics = null;
	}
	private void generateTableData() {
		String[] tableColumnNames = {"Name","Market","Amount","Bought for","Sold for", "Profit %","BuyDate","SellDate"};
		Object[][] rows = new Object[history.size()][tableColumnNames.length];

		DecimalFormat df = new DecimalFormat("#.###");


		DateTimeFormatter fmt = DateTimeFormat.forPattern("yy/MM/ee HH:mm:ss");

		for (int i = 0; i < history.size(); i++) {
			StockPrices sp = history.get(i).getStockPrice();
			rows[i][0] = sp.getStockName().getName();
			rows[i][1] = sp.getStockName().getMarket();

			rows[i][2] = history.get(i).getAmount();

			rows[i][3] = FinancialLongConverter.toStringTwoDecimalPoints(history.get(i).getAmount()*sp.getBuy());


			rows[i][6] = new DateTime(sp.getTime()).toString(fmt);

			if (history.get(i).getSoldDate() != null) {
				StockPrices soldStockPrice = history.get(i).getSoldStockPrice();

				rows[i][5] = df.format((double)(history.get(i).getAmount()*soldStockPrice.getSell())/(history.get(i).getAmount()*sp.getBuy()));
				rows[i][7] = new DateTime(history.get(i).getSoldDate()).toString(fmt);
				rows[i][4] = FinancialLongConverter.toStringTwoDecimalPoints(history.get(i).getAmount()*soldStockPrice.getSell());
			}
		}

		tableDate = new Pair<Object[][], Object[]>(rows, tableColumnNames);
	}

	public TableModel getTable() {
		if (tableDate != null)
			return new DefaultTableModel(tableDate.getLeft(), tableDate.getRight());

		Object[][] v = {{"No data generated"}};
		return new DefaultTableModel(v, v);
	}
	private void loadHistory() {
		history = new ArrayList<PortfolioHistory>();
		history.addAll(selectedPortfolio.getHistory());
		sortHistory(history);
	}
	private TimeSeries getBalanceTimeSeries() {
		TimeSeries t1 = new TimeSeries("Balance");

		if (history.size() > 0) {
			TreeMap<Date, LongContainer> sortedMap = new TreeMap<Date, LongContainer>();

			// Add all purchases
			for (PortfolioHistory ph : history) {
				if (sortedMap.containsKey(ph.getBuyDate())) {
					LongContainer lc = sortedMap.get(ph.getBuyDate());
					lc.setValue(lc.getValue() - ph.getAmount()*ph.getStockPrice().getBuy());
				}
				else {
					long value = -(ph.getStockPrice().getBuy() * ph.getAmount());

					sortedMap.put(ph.getBuyDate(), new LongContainer(value));
				}
			}
			// Add all sells
			for (PortfolioHistory ph : history) {
				if (ph.getSoldDate() != null) {
					SortedMap<Date, LongContainer> apa = sortedMap.tailMap(ph.getSoldDate());

					Collection<LongContainer> bepa = apa.values();

					for (LongContainer l : bepa) {
						l.setValue(l.getValue() + ph.getSoldStockPrice().getSell()*ph.getAmount());
					}
				}
			}

			// Add all investments
			List<PortfolioInvestment> investments = new ArrayList<PortfolioInvestment>();
			investments.addAll(selectedPortfolio.getInvestments());
			Collections.sort(investments, new PortfolioInvestmentComparator());

			
			for (int i = 0; i < investments.size(); i++) {
				long invested = 0;
				if (investments.get(i).didInvest())
					invested += investments.get(i).getAmount();
				else
					invested -= investments.get(i).getAmount();

				Date startDate = investments.get(i).getDate();
				Date endDate = new Date(System.currentTimeMillis());

				SortedMap<Date, LongContainer> subMap = sortedMap.subMap(startDate, true, endDate, true);
				Collection<LongContainer> bepa = subMap.values();
				for (LongContainer l : bepa) {
					l.setValue(l.getValue() + invested);
				}
			}

			for (Entry<Date, LongContainer> apa : sortedMap.entrySet()) {
				t1.add(new Millisecond(apa.getKey()), FinancialLongConverter.toDouble(apa.getValue().getValue()));
			}
		}
		
		return t1;
	}
	private void generateTimeSeries() {
		TimeSeries worth = new TimeSeries("Worth");
		
		if (history.size() > 0) {
			
			List<Pair<DateTime, PortfolioHistory>> buyList = new ArrayList<Pair<DateTime, PortfolioHistory>>();
			List<Pair<DateTime, PortfolioHistory>> sellList = new ArrayList<Pair<DateTime,PortfolioHistory>>();
			Map<PortfolioHistory, DateTime> sellMap = new HashMap<PortfolioHistory, DateTime>();
			
			DateTime currentDate = new DateTime();
			for (PortfolioHistory ph : history) {
				buyList.add(new Pair<DateTime, PortfolioHistory>(new DateTime(ph.getBuyDate()), ph));
				if (ph.stillInPortFolio()) {
					sellList.add(new Pair<DateTime, PortfolioHistory>(currentDate, ph));
					sellMap.put(ph, currentDate);
				}
				else {
					sellList.add(new Pair<DateTime, PortfolioHistory>(new DateTime(ph.getSoldDate()), ph));
					sellMap.put(ph, new DateTime(ph.getSoldDate()));
				}
			}
			
			TreeMap<DateTime, LongContainer> worthMap = new TreeMap<DateTime, LongContainer>();
			
			for (Pair<DateTime, PortfolioHistory> buyPoint : buyList) {
				worthMap.put(buyPoint.getLeft(), new LongContainer(0));				
			}
			for (Pair<DateTime, PortfolioHistory> sellPoint : sellList) {
				worthMap.put(sellPoint.getLeft(), new LongContainer(0));				
			}
			
			for (Pair<DateTime, PortfolioHistory> buyPoint : buyList) {
				if (buyPoint.getLeft().isBefore(currentDate)) {
					SortedMap<DateTime, LongContainer> subMap = worthMap.subMap(buyPoint.getLeft(), true, currentDate, true);
					for (Entry<DateTime, LongContainer> entry : subMap.entrySet()) {
						//Add the current value
						StockNames stockName = buyPoint.getRight().getStockPrice().getStockName();
						StockPrices latestStockPrice = jpaHelper.getLastStock(stockName, entry.getKey().toDate());
						long amount = buyPoint.getRight().getAmount();
						if (latestStockPrice != null)
							entry.getValue().add(latestStockPrice.getSell()*amount);
					}
				}
			}
			for (Entry<DateTime, LongContainer> entry : worthMap.entrySet()) {
				if (!entry.getKey().isEqual(currentDate))
					worth.add(new Millisecond(entry.getKey().toDate()), FinancialLongConverter.toDouble(entry.getValue().getValue()));
			}

		}


		allTimeSeries.put("Worth", worth);
		allTimeSeries.put("Portfolio Balance", getBalanceTimeSeries());
	}
	private void sortHistory(List<PortfolioHistory> history) {
		Collections.sort(history, new PortfolioHistoryComparator());
	}
	class PortfolioHistoryComparator extends PortfolioHistory implements Comparator<PortfolioHistory> {
		@Override
		public int compare(PortfolioHistory o1, PortfolioHistory o2) {
			if (o1.getBuyDate().getTime() < o2.getBuyDate().getTime() )
				return -1;
			else if (o1.getBuyDate().getTime() > o2.getBuyDate().getTime() ) {
				return 1;
			}
			else if (o1.getBuyDate().getTime() == o2.getBuyDate().getTime() ) {
				if (o1.getSoldDate() == null)
					return 1;
				if (o2.getSoldDate() == null)
					return -1;
				if (o1.getSoldDate().getTime() < o2.getSoldDate().getTime())
					return -1;
				else 
					return 1;
			}
			return 0;
		}	
	}
	class PortfolioInvestmentComparator extends PortfolioInvestment implements Comparator<PortfolioInvestment> {
		@Override
		public int compare(PortfolioInvestment o1, PortfolioInvestment o2) {
			if (o1.getDate().getTime() < o2.getDate().getTime()) {
				return -1;
			}
			else if (o1.getDate().getTime() > o2.getDate().getTime()) {
				return 1;
			}
			return 0;
		}	
	}
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().contains(PortfolioHistoryModelStatistics.STATISTICSMODELUPDATED)) {
			propertyChangeSupport.firePropertyChange(STATISTICSUPDATED, evt.getOldValue(), evt.getNewValue());
		}
	}
}
