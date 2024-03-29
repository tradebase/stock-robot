package model.portfolio;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import utils.global.Log;
import utils.global.Log.TAG;

import model.database.jpa.IJPAHelper;
import model.database.jpa.JPAHelper;
import model.database.jpa.tables.PortfolioEntity;


/**
 * @author Daniel
 *
 * Will manage all portfolios.
 * It will start by creating all the portfolio objects.
 * Each portfolio will then create a algorithm instance.
 */
public final class PortfolioHandler implements IPortfolioHandler{

	private static PortfolioHandler instance = null;
	private final transient List<IPortfolio> listOfPortfolios = new ArrayList<IPortfolio>();
	private final transient IJPAHelper jpaHelper;
	private final transient PropertyChangeSupport pChangeSuport = new PropertyChangeSupport(this);
	private final transient AlgortihmLoader algorithmLoader = AlgortihmLoader.getInstance();
	private final transient IRobot_Algorithms robot;
	
	private PortfolioHandler(final IRobot_Algorithms robot) {
		this.robot = robot;
		jpaHelper = JPAHelper.getInstance();
		final List<PortfolioEntity> portfolioTables = jpaHelper.getAllPortfolios();
		
		for (PortfolioEntity pt : portfolioTables) {
			final IPortfolio p = createExistingPortfolio(pt);
			Log.log(TAG.VERY_VERBOSE, "Portfolio created: " + p.getName());
		}
	}
	
	private IPortfolio createExistingPortfolio(PortfolioEntity portfolioTable) {
		final Portfolio p = new Portfolio(portfolioTable);
		
		if (portfolioTable.getAlgortihmSettings().getAlgorithmName() != null) {
			final IAlgorithm algorithm = algorithmLoader.loadAlgorithm(portfolioTable.getAlgortihmSettings().getAlgorithmName());
			
			if (algorithm != null) {
				algorithm.setRobot(robot);
				algorithm.setPortfolio(p);
				
				if (!portfolioTable.getAlgortihmSettings().isInitiated()) {
					portfolioTable.getAlgortihmSettings().initiate(algorithm.getDefaultDoubleSettings(), algorithm.getDefaultLongSettings());
					jpaHelper.updateObject(portfolioTable);
				}
				algorithm.giveDoubleSettings(portfolioTable.getAlgortihmSettings().getCurrentDoubleSettings());
				algorithm.giveLongSettings(portfolioTable.getAlgortihmSettings().getCurrentLongSettings());
				
				p.setAlgorithm(algorithm);
				listOfPortfolios.add(p);
				
				Log.log(TAG.VERY_VERBOSE, p.getName() + " algorithm set to: " + algorithm.getName());
			}
			else {
				Log.log(TAG.ERROR, p.getName() + " couldent set algorithm to: " + portfolioTable.getAlgortihmSettings().getAlgorithmName() + " == null");
			}
		}
		else {
			Log.log(TAG.VERY_VERBOSE, p.getName() + " dosent have a algorithm set yet.");
		}
		return p;
	}
	@Override
	public IPortfolio createNewPortfolio(final String name) {
		final PortfolioEntity pt = new PortfolioEntity(name);
		jpaHelper.storeObject(pt);
		
		final IPortfolio p = new Portfolio(pt);
		
		listOfPortfolios.add(p);
		
		return p;
	}
	
	@Override
	public boolean setAlgorithm(final IPortfolio p, final String algorithmName) {
		final IAlgorithm algorithm = algorithmLoader.loadAlgorithm(algorithmName);
		if (algorithm != null) {
			p.setAlgorithm(algorithm);
			
			algorithm.setRobot(robot);
			algorithm.setPortfolio(p);
			
			PortfolioEntity pe = p.getPortfolioTable();
			pe.setAlgorithm(algorithmName);
			
			pe.getAlgortihmSettings().initiate(algorithm.getDefaultDoubleSettings(), algorithm.getDefaultLongSettings());
			
			jpaHelper.updateObject(pe);
			
			pChangeSuport.firePropertyChange(MSG_PORTFOLIO_ADDED,null,p.getName());
			
			return true;
		}
		return false;
	}
		
	@Override
	public List<IPortfolio> getPortfolios() {
		return listOfPortfolios;
	}

	@Override
	public boolean removePortfolio(IPortfolio portfolio) {
		if (portfolio.getPortfolioTable().getBalance() == 0) {
			listOfPortfolios.remove(portfolio);
			pChangeSuport.firePropertyChange(MSG_PORTFOLIO_ADDED,null,portfolio.getName());
			jpaHelper.remove(portfolio.getPortfolioTable());
			return true;
		}
		return false;
	}
	public static IPortfolioHandler getInstance(IRobot_Algorithms robot) {
		synchronized (PortfolioHandler.class) {
			if (instance == null) {
				instance = new PortfolioHandler(robot);
			}
		}
		return instance;
	}
	public static IPortfolioHandler getInstance() {
		return instance;
	}
	@Override
	public void addObserver(PropertyChangeListener listener) {
		pChangeSuport.addPropertyChangeListener(listener);
	}
	@Override
	public void removeObserver(PropertyChangeListener listener) {
		pChangeSuport.addPropertyChangeListener(listener);
	}
}
