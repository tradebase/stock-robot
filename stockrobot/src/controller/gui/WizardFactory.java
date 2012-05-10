package controller.gui;


import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;


import model.portfolio.IPortfolioHandler;
import model.wizard.WizardModel;
import model.wizard.portfolio.PortfolioWizardModel;
import view.wizard.WizardPage;
import view.wizard.portfolio.PortfolioFromNewPage;

public class WizardFactory {
	
	public static WizardContoller buildPortfolioWizard(){
			
	 	WizardContoller wizard = new WizardContoller();
		WizardModel wizardModel = wizard.getModel();
		PortfolioWizardModel pageModel = new PortfolioWizardModel(wizardModel);
		
		WizardPage startPage = buildStartPage(wizardModel,pageModel);
    	wizard.getView().registerPage(1,startPage);
    	wizardModel.setNextPage(1);
    	wizardModel.goNextPage();
    	
    	WizardPage fromNewPage = buildFromNewPage(wizardModel, pageModel);
    	wizard.getView().registerPage(2,fromNewPage);
							
		return wizard;
	}
	
	private static WizardPage buildStartPage(WizardModel wizardModel, PortfolioWizardModel pageModel){
		
		WizardStartPageController controller = new WizardStartPageController(wizardModel,pageModel);
		
		return controller.getView();
	}
	
	private static WizardPage buildFromNewPage(WizardModel wizardModel, PortfolioWizardModel pageModel){

		
		WizardFromNewPageController controller = new WizardFromNewPageController(wizardModel, pageModel);
		
		return controller.getView();
	}
}
