// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.client.page;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.view.EditDateField;
import org.cloudcoder.app.client.view.EditDateTimeField;
import org.cloudcoder.app.client.view.EditEnumField;
import org.cloudcoder.app.client.view.EditModelObjectField;
import org.cloudcoder.app.client.view.EditStringField;
import org.cloudcoder.app.client.view.EditStringFieldWithAceEditor;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.TestCaseEditor;
import org.cloudcoder.app.client.view.ViewUtil;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.EditProblemAdapter;
import org.cloudcoder.app.shared.model.IProblem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemLicense;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Page for editing a {@link ProblemAndTestCaseList}.
 * 
 * @author David Hovemeyer
 */
public class EditProblemPage extends CloudCoderPage {
		
	private class UI extends ResizeComposite implements SessionObserver {
		
		private DockLayoutPanel dockLayoutPanel;
		private Label pageLabel;
		private PageNavPanel pageNavPanel;
		private FlowPanel centerPanel;
		private List<EditModelObjectField<IProblem, ?>> problemFieldEditorList;
		private List<TestCaseEditor> testCaseEditorList;
		private Button addTestCaseButton;
		private FlowPanel addTestCaseButtonPanel;
		
		public UI() {
			this.dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			// At top of page, show name of course and a PageNavPanel
			LayoutPanel northPanel = new LayoutPanel();
			this.pageLabel = new Label("");
			pageLabel.setStyleName("cc-courseLabel");
			northPanel.add(pageLabel);
			northPanel.setWidgetLeftRight(pageLabel, 0.0, Unit.PX, PageNavPanel.WIDTH, PageNavPanel.WIDTH_UNIT);
			northPanel.setWidgetTopBottom(pageLabel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH, Unit.PX);
			northPanel.setWidgetTopBottom(pageNavPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT);
			
			// Create UI for editing problem and test cases
			problemFieldEditorList = new ArrayList<EditModelObjectField<IProblem, ?>>();
			createProblemFieldEditors();

			this.centerPanel = new FlowPanel();
			
			// Add editor widgets for Problem fields
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				centerPanel.add(editor.getUI());
			}
			
			dockLayoutPanel.add(new ScrollPanel(centerPanel));
			
			initWidget(dockLayoutPanel);
		}

		private void createProblemFieldEditors() {
			problemFieldEditorList.add(new EditEnumField<IProblem, ProblemType>("Problem type", ProblemType.class) {
				@Override
				protected void setField(ProblemType value) {
					getModelObject().setProblemType(value);
				}

				@Override
				protected ProblemType getField() {
					return getModelObject().getProblemType();
				}
			});
			
			problemFieldEditorList.add(new EditStringField<IProblem>("Problem name") {
				@Override
				protected void setField(String value) {
					getModelObject().setTestName(value);
				}
				
				@Override
				protected String getField() {
					return getModelObject().getTestName();
				}
			});
			
			problemFieldEditorList.add(new EditStringField<IProblem>("Brief description") {
				@Override
				protected void setField(String value) {
					getModelObject().setBriefDescription(value);
				}
				
				@Override
				protected String getField() {
					return getModelObject().getBriefDescription();
				}
				
			});
			
			EditStringFieldWithAceEditor<IProblem> descriptionEditor =
					new EditStringFieldWithAceEditor<IProblem>("Full description (HTML)") {
						@Override
						protected void setField(String value) {
							getModelObject().setDescription(value);
						}
						@Override
						protected String getField() {
							return getModelObject().getDescription();
						}
					};
			descriptionEditor.setEditorMode(AceEditorMode.HTML);
			descriptionEditor.setEditorTheme(AceEditorTheme.VIBRANT_INK);
			problemFieldEditorList.add(descriptionEditor);
			
			EditStringFieldWithAceEditor<IProblem> skeletonEditor =
					new EditStringFieldWithAceEditor<IProblem>("Skeleton code") {
						@Override
						public void update() {
							// Set the editor mode to match the ProblemType
							AceEditorMode editorMode = ViewUtil.getModeForLanguage(getModelObject().getProblemType().getLanguage());
							setEditorMode(editorMode);
							super.update();
						}
						@Override
						protected void setField(String value) {
							getModelObject().setSkeleton(value);
						}
						@Override
						protected String getField() {
							return getModelObject().getSkeleton();
						}
					};
			skeletonEditor.setEditorTheme(AceEditorTheme.VIBRANT_INK);
			problemFieldEditorList.add(skeletonEditor);
			
			// We don't need an editor for schema version - problems/testcases are
			// automatically converted to the latest version when they are imported.
			
			problemFieldEditorList.add(new EditStringField<IProblem>("Author name") {
				@Override
				protected void setField(String value) {
					getModelObject().setAuthorName(value);
				}
				@Override
				protected String getField() {
					return getModelObject().getAuthorName();
				}
			});
			
			problemFieldEditorList.add(new EditStringField<IProblem>("Author email") {
				@Override
				protected void setField(String value) {
					getModelObject().setAuthorEmail(value);
				}
				@Override
				protected String getField() {
					return getModelObject().getAuthorEmail();
				}
			});
			
			problemFieldEditorList.add(new EditStringField<IProblem>("Author website") {
				@Override
				protected void setField(String value) {
					getModelObject().setAuthorWebsite(value);
				}
				@Override
				protected String getField() {
					return getModelObject().getAuthorWebsite();
				}
			});
			
			problemFieldEditorList.add(new EditDateField<IProblem>("Creation date") {
				@Override
				protected void setField(Date value) {
					getModelObject().setTimestampUTC(value.getTime());
				}
				@Override
				protected Date getField() {
					return new Date(getModelObject().getTimestampUTC());
				}
			});
			
			problemFieldEditorList.add(new EditEnumField<IProblem, ProblemLicense>("License", ProblemLicense.class) {
				@Override
				protected void setField(ProblemLicense value) {
					getModelObject().setLicense(value);
				}

				@Override
				protected ProblemLicense getField() {
					return getModelObject().getLicense();
				}
			});
			
			problemFieldEditorList.add(new EditDateTimeField<IProblem>("When assigned") {
				@Override
				protected void setField(Date value) {
					getModelObject().setWhenAssigned(value.getTime());
				}
				@Override
				protected Date getField() {
					return getModelObject().getWhenAssignedAsDate();
				}
			});
			
			problemFieldEditorList.add(new EditDateTimeField<IProblem>("When due") {
				@Override
				protected void setField(Date value) {
					getModelObject().setWhenDue(value.getTime());
				}
				@Override
				protected Date getField() {
					return getModelObject().getWhenDueAsDate();
				}
			});
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
		 */
		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			// Activate views
			final Course course = session.get(Course.class);
			pageLabel.setText("Edit problem in " + course.toString());
			pageNavPanel.setBackHandler(new Runnable() {
				@Override
				public void run() {
					session.notifySubscribers(Session.Event.COURSE_ADMIN, course);
				}
			});
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			
			// The session should contain a ProblemAndTestCaseList.
			ProblemAndTestCaseList problemAndTestCaseList = session.get(ProblemAndTestCaseList.class);
			
			// Create a ProblemAdapter to serve as the IProblem edited by the problem editors.
			// Override the onChange() method to update all editors whenever the state of
			// of the underlying Problem changes.
			IProblem problemAdapter = new EditProblemAdapter(problemAndTestCaseList.getProblem()) {
				@Override
				protected void onChange() {
					for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
						editor.update();
					}
				}
			};
			
			// Set the Problem in all problem field editors.
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				editor.setModelObject(problemAdapter);
			}
			
			// Add TestCaseEditors for test cases.
			testCaseEditorList = new ArrayList<TestCaseEditor>();
			for (TestCase testCase : problemAndTestCaseList.getTestCaseList()) {
				final TestCaseEditor testCaseEditor = new TestCaseEditor();
				testCaseEditor.setDeleteHandler(new Runnable() {
					@Override
					public void run() {
						handleDeleteTestCase(testCaseEditor);
					}
				});
				testCaseEditorList.add(testCaseEditor);
				testCaseEditor.setTestCase(testCase);
				centerPanel.add(testCaseEditor.getUI());
			}
			
			// Add a button to create a new TestCase and TestCaseEditor.
			// Put it in a FlowPanel to ensure that it's in its own div.
			// (Could also use this to style/position the button.)
			this.addTestCaseButtonPanel = new FlowPanel();
			addTestCaseButton = new Button("Add Test Case");
			addTestCaseButton.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					handleAddTestCase();
				}
			});
			addTestCaseButtonPanel.add(addTestCaseButton);
			centerPanel.add(addTestCaseButtonPanel);
		}

		/**
		 * @param testCaseEditor
		 */
		protected void handleDeleteTestCase(TestCaseEditor testCaseEditor) {
			getSession().get(ProblemAndTestCaseList.class).removeTestCase(testCaseEditor.getTestCase());
			centerPanel.remove(testCaseEditor.getUI());
			testCaseEditorList.remove(testCaseEditor);
		}

		protected void handleAddTestCase() {
			// Add the TestCase to the ProblemAndTestCaseList
			TestCase testCase = TestCase.createEmpty();
			getSession().get(ProblemAndTestCaseList.class).addTestCase(testCase);

			// Add a new TestCase editor and its UI widget
			TestCaseEditor testCaseEditor = new TestCaseEditor();
			testCaseEditorList.add(testCaseEditor);
			centerPanel.insert(testCaseEditor.getUI(), centerPanel.getWidgetIndex(addTestCaseButtonPanel));
			testCaseEditor.setTestCase(testCase);
		}
	}
	
	private UI ui;

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#createWidget()
	 */
	@Override
	public void createWidget() {
		ui = new UI();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
	 */
	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#deactivate()
	 */
	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#getWidget()
	 */
	@Override
	public IsWidget getWidget() {
		return ui;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#isActivity()
	 */
	@Override
	public boolean isActivity() {
		//return true;
		return false;
	}
	
}