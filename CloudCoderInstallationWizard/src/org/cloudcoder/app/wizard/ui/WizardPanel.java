package org.cloudcoder.app.wizard.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;
import org.cloudcoder.app.wizard.model.validators.IValidator;
import org.cloudcoder.app.wizard.model.validators.ValidationException;

public class WizardPanel extends JPanel {
	private static final String INSTALL_BUTTON_TEXT = "Install";
	private static final String NEXT_BUTTON_TEXT = "Next >>";
	private static final String PREV_BUTTON_TEXT = "<< Previous";

	private static final long serialVersionUID = 1L;
	
	private Document document;
	private List<IWizardPagePanel> wizardPagePanels;
	private int currentPage;
	private JLabel pageLabel;
	private JButton prevButton;
	private JButton nextButton;
	private JPanel pagePanel;
	private JLabel errorLabel;

	public WizardPanel() {
		wizardPagePanels = new ArrayList<IWizardPagePanel>();
		
		setPreferredSize(new Dimension(800, 600));

		setLayout(new BorderLayout());
		
		JPanel errorMessageAndButtonPanel = new JPanel();
		errorMessageAndButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		this.errorLabel = new JLabel();
		errorLabel.setPreferredSize(new Dimension(720, 32));
		errorLabel.setForeground(Color.RED);
		errorMessageAndButtonPanel.add(errorLabel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.prevButton = new JButton(PREV_BUTTON_TEXT);
		prevButton.setPreferredSize(new Dimension(140, 26));
		this.nextButton = new JButton(NEXT_BUTTON_TEXT);
		nextButton.setPreferredSize(new Dimension(140, 26));
		buttonPanel.add(prevButton);
		buttonPanel.add(nextButton);
		buttonPanel.setPreferredSize(new Dimension(780, 32));
		errorMessageAndButtonPanel.add(buttonPanel);
		
		errorMessageAndButtonPanel.setPreferredSize(new Dimension(800, 96));
		add(errorMessageAndButtonPanel, BorderLayout.PAGE_END);
		
		this.pageLabel = new JLabel();
		pageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		pageLabel.setPreferredSize(new Dimension(720, 64));
		pageLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 24));
		add(pageLabel, BorderLayout.PAGE_START);
		
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onNext();
			}
		});
		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onPrevious();
			}
		});
		
		// Panel with CardLayout for displaying the WizardPagePanels
		this.pagePanel = new JPanel();
		pagePanel.setLayout(new CardLayout());
		add(pagePanel, BorderLayout.CENTER);
	}

	protected void onNext() {
		goToPage(currentPage + 1);
	}

	protected void onPrevious() {
		goToPage(currentPage - 1);
	}

	private void goToPage(int nextPage) {
		if (doValidate()) {
			commitCurrentValues();
			currentPage = nextPage;
			changePage();
		}
	}

	private boolean doValidate() {
		Page page = document.get(currentPage);
		IWizardPagePanel p = wizardPagePanels.get(currentPage);
		if (p.getType() != IWizardPagePanel.Type.CONFIG) {
			return true;
		}
		ConfigPanel pp = p.asConfigPanel();
		pp.markAllValid();
		errorLabel.setText("");
		try {
			// Get Page with current UI values
			Page current = pp.getCurrentValues();
			
			// Validate all fields that haven't been selectively disabled
			for (int i = 0; i < page.getNumValues(); i++) {
				IValue origValue = page.get(i);
				if (current.isEnabled(page.get(i).getName())) {
					IValue updatedValue = current.get(i);
					IValidator validator = page.getValidator(i);
					validator.validate(origValue, updatedValue);
				}
			}
			return true;
		} catch (ValidationException e) {
			// Highlight the field that failed to validate
			for (int i = 0; i < page.getNumValues(); i++) {
				if (e.getOrigValue() == page.get(i)) {
					IPageField field = pp.getField(i);
					field.markInvalid();
					errorLabel.setText(e.getMessage());
				}
			}
			return false;
		}
	}
	
	private void commitCurrentValues() {
		Page page = document.get(currentPage);
		IWizardPagePanel p = wizardPagePanels.get(currentPage);
		if (p.getType() != IWizardPagePanel.Type.CONFIG) {
			return;
		}
		ConfigPanel pp = p.asConfigPanel();
		for (int i = 0; i < page.getNumValues(); i++) {
			IValue currentValue = pp.getField(i).getCurrentValue();
			page.set(i, currentValue);
		}
	}

	public void setDocument(Document document) {
		this.document = document;
		
		// Create WizardPagePanels
		for (int i = 0; i < document.getNumPages(); i++) {
			Page p = document.get(i);
			IWizardPagePanel pp;
			
			if (p.getPageName().equals("install")) {
				pp = new InstallPanel();
			} else {
				pp = new ConfigPanel();
			}
			
			pp.setPage(p);
			wizardPagePanels.add(pp);
			pagePanel.add(pp.asComponent(), String.valueOf(i));
		}
		
		currentPage = 0;
		changePage();
	}

	private void changePage() {
		Page page = document.get(currentPage);
		pageLabel.setText(page.getLabel());
		CardLayout cl = (CardLayout) pagePanel.getLayout();
		cl.show(pagePanel, String.valueOf(currentPage));
		boolean isInstallPage = page.getPageName().equals("install");
		prevButton.setEnabled(!isInstallPage && currentPage > 0);
		nextButton.setEnabled(!isInstallPage && currentPage < document.getNumPages() - 1);
		boolean isReadyPage = page.getPageName().equals("ready");
		nextButton.setText(isReadyPage ? INSTALL_BUTTON_TEXT : NEXT_BUTTON_TEXT);
	}
}
