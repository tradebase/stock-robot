package testsuiteforeverything;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import controller.gui.GraphControllerTest;
import controller.gui.SimulationControllerTest;
import database.jpa.JPATest;
import database.jpa.MainBasicJPATest;
import controller.wizard.portfolio.WizardFromNewPageControllerTest;
import portfolio.PortfolioHandlerTest;
import portfolio.PortfolioTest;
import robot.AstroRecieverTest;
import robot.RobotSchedulerTest;
import robot.TraderTest;
import simulation.SimulationHandlerTest;
import utils.FinancialLongConverterTest;
import utils.PairTest;
import view.components.ItemCmbPortfolioTest;
import model.scraping.ParserRunnerTest;
import model.scraping.ParserStockTest;
import model.scraping.SchedulerTest;
import model.scraping.parser.AvanzaParserTest;
import model.scraping.parser.SimulationRunnerTest;
import model.wizard.WizardModelTest;
import model.wizard.WizardPageModelTest;

/**
 * Class to wrap other unit test classes, so we can test everything we want to in one single run
 * 
 * <p>
 * 
 * Right now JPA needs to have the -javaagent 
 * -javaagent:/PATH-TO-YOUR-OPENJPA2/stock-robot/stockrobot/jar/openjpa-all-2.2.0.jar
 * 
 * </p>
 * 
 * @author kristian
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	FinancialLongConverterTest.class,
	PairTest.class,
	SimulationControllerTest.class,
	GraphControllerTest.class,
	JPATest.class,
	MainBasicJPATest.class,
	SimulationHandlerTest.class,
	SimulationRunnerTest.class,
	ItemCmbPortfolioTest.class,
	AstroRecieverTest.class,
	ParserStockTest.class,
	PortfolioHandlerTest.class,
	TraderTest.class,
	PortfolioTest.class,
	RobotSchedulerTest.class,
	WizardFromNewPageControllerTest.class,
	WizardModelTest.class,
	WizardPageModelTest.class,
	SchedulerTest.class,
	AvanzaParserTest.class,
	ParserRunnerTest.class,
})

public class TestSuiteForEverything {
	//All glory to the Hypnotoad!
}