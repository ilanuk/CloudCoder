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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;

/**
 * A TestCase for a {@link Problem}.
 * Specifies input(s) and expected output.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class TestCase extends TestCaseData implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int problemId;
	public TestCase() {
		
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
	public int getProblemId() {
		return problemId;
	}

	/**
	 * Factory method for creating an empty TestCase.
	 * (I.e., where all of the fields are initialized to empty values,
	 * as opposed to null values.)
	 * 
	 * @return an empty TestCase
	 */
	public static TestCase createEmpty() {
		TestCase empty = new TestCase();
		empty.setId(-1);
		empty.setProblemId(-1);
		empty.setTestCaseName("");
		empty.setInput("");
		empty.setOutput("");
		empty.setSecret(false);
		return empty;
	}
}