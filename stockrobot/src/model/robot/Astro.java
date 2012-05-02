package model.robot;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import utils.global.Log;
import utils.global.Log.TAG;
import view.PortfolioController;
import viewfactory.ViewFactory;

import model.database.jpa.IJPAHelper;
import model.database.jpa.JPAHelper;
import model.database.jpa.tables.PortfolioEntity;
import model.database.jpa.tables.StockNames;
import model.database.jpa.tables.StockPrices;
import model.portfolio.IPortfolio;
import model.portfolio.IPortfolioHandler;
import model.portfolio.PortfolioHandler;
import model.trader.ITrader;
import model.trader.TraderSimulator;


/**
 * @author Daniel
 *
 * The starting point of the ASTRo System.
 * 
 * It will currently insert a new stock in the system each second, 
 * and then start the algorithms in all portfolios.
 * 
 * This is the very very first basic prototype of the system.
 */
public class Astro implements IRobot_Algorithms{

	IPortfolioHandler portfolioHandler = null;
	JFrame portfolioGui = null;
	PortfolioController portfolioController = null;
	ITrader trader = TraderSimulator.getInstance();
	IJPAHelper jpaHelper = JPAHelper.getInstance();
	Random rand = new Random(System.currentTimeMillis());
	//PluginAlgortihmLoader algorithmLoader = PluginAlgortihmLoader.getInstance();
	RobotScheduler rs = null;

	private static boolean simulate = false;
	private static int timeBetweenUpdates = 1500;
	
	private static String serverAdress;
	private static int serverPort = -1;
	private List<StockNames> simulatedStocks = null;
	/**
	 * Starts the system up
	 */
	//TODO: In a new thread?
	private void start() {
		
		System.out.println("ASTRo is starting up.");
		portfolioHandler 	= PortfolioHandler.getInstance(this);
		
		if (serverPort > 0) {
			rs = new RobotScheduler(portfolioHandler, serverAdress, serverPort);
		}
		else {
			rs = new RobotScheduler(portfolioHandler);
		}
		
		Thread apa = new Thread(rs);
		apa.start();
		
		if (simulate) {
			simulatedStocks = new ArrayList<StockNames>();
			initSimulationState();
		}
		
		portfolioGui 		= ViewFactory.getPortfolioView(portfolioHandler,trader);
		//portfolioController = new PortfolioController(portfolioGui,portfolioHandler,trader);
		
		while(true) {
			for (IPortfolio p : portfolioHandler.getPortfolios()) {
				if (simulate) {
					if (rand.nextInt(10) == 1) {
						long newInvestment = ((long)rand.nextInt(1000)*1000000);

						p.investAmount(newInvestment);
						Log.instance().log(TAG.VERBOSE, "More money invested: " + newInvestment + " to portfolio: " + p);
					}
				}

				//p.updateAlgorithm();
			}
			try {
				Thread.sleep(timeBetweenUpdates);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (simulate && serverPort == -1)
				simulateNewStocks();
		}

	}

	/**
	 * Will setup a working simulation state.
	 * Creating two portfolios and 10 stocks
	 */
	private void initSimulationState() {
		boolean alreadyExists = false;
		for (PortfolioEntity p : jpaHelper.getAllPortfolios()) {
			if (p.getName().contains("sim portfolio")) {
				alreadyExists = true;
				break;
			}
		}

		if (!alreadyExists) {
			Log.instance().log(TAG.VERBOSE, "Creating simulation portfolios");
			for (int i = 1; i <= 2; i++) {
				
				IPortfolio portfolio = portfolioHandler.createNewPortfolio("sim portfolio " + i);
				
				portfolioHandler.setAlgorithm(portfolio, portfolioHandler.getAlgorithmNames().get(i-1));
				
				jpaHelper.investMoney(10000000, portfolio.getPortfolioTable());
			}
		}
		if (serverPort == -1) {
			alreadyExists = false;
			for (StockNames s : jpaHelper.getAllStockNames()) {
				if (s.getName().contains("sim stock")) {
					alreadyExists = true;
					simulatedStocks.add(s);
				}
			}
			if (!alreadyExists) {
				for (int i = 1; i <= 10; i++) {
					simulatedStocks.add(new StockNames("sim stock" + i, "Market" + i%3));
					jpaHelper.storeObject(simulatedStocks.get(i-1));
				}
				simulateNewStocks();
			}
		}
		
	}
	@Override
	public boolean reportToUser(String message) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * Just add a new stock
	 */
	private void simulateNewStocks() {
		for (StockNames sn : simulatedStocks) {
			StockPrices sp = new StockPrices(sn, rand.nextInt(100000000), rand.nextInt(100000000), rand.nextInt(100000000), rand.nextInt(100000000), new Date(System.currentTimeMillis()));
			jpaHelper.storeObjectIfPossible(sp);
		}
	}
	public static void main(String args[]) {
		Astro astro = new Astro();

		for (int i = 0; i < args.length; i++) {
			String s = args[i];

			if (s.contentEquals("--simulate") || s.contentEquals("-s"))
				simulate = true;
			else if (s.contentEquals("-v")) {
				Log.instance().setFilter(TAG.VERBOSE, true);
				System.out.println("Verbose mode on.");
			}
			else if (s.contentEquals("-vv")) {
				Log.instance().setFilter(TAG.VERY_VERBOSE, true);
				System.out.println("VeryVerbose mode on.");
			}
			else if (s.contentEquals("-t") || s.contentEquals("--time")) {
				if (args.length > i+1) {
					try {
						timeBetweenUpdates = Math.abs(Integer.parseInt(args[i+1]));
						System.out.println("Algorithm update time set to: " + timeBetweenUpdates + "ms.");
						i++;
					} catch (NumberFormatException e) {
						System.out.println("No valid update time.");
						System.exit(1);
					}
				}
			}
			else if (s.contentEquals("--server")) {
				if (args.length > i+1) {
					try {
						String[] serverInput = args[i+1].split(":");
						
						
						if (serverInput.length == 2) {
							serverAdress = serverInput[0];
							serverPort = Integer.parseInt(serverInput[1]);
							
							System.out.println("Server Adress: " + serverAdress + ":" + serverPort);
						}
						i++;
					} catch (NumberFormatException e) {
						System.out.println("Not a valid server format.");
						System.exit(1);
					}
				}
			}
			
			
			else if (s.contentEquals("--help")) {
				System.out.println("ASTRo\nAlgorithm Stock Trading Robot\n\n\t-s or --simulate" + 
						"\tTo simulate new stocks and more investments.\n" +
						"\t--time x or -t x\tSet the time between algorithm updates to x ms.\n" + 
						"\t -v for more output.\n" +
						"\t -vv for even more output\n"
						);
				System.exit(0);
			}
			else {
				System.out.println("Unknown parameter " + s + " , aborting\n");
				System.exit(1);
			}
		}

		astro.start();
	}

	@Override
	public IJPAHelper getJPAHelper() {
		return jpaHelper;
	}
	@Override
	public ITrader getTrader() {
		return trader;
	}
}
