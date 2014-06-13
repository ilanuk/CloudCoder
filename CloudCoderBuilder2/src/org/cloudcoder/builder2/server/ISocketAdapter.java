// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.builder2.server;

import java.io.IOException;

/**
 * Variation of {@link ISocket} where the {@link #connect()} method
 * must be called before the connection can be used.
 * 
 * @author David Hovemeyer
 */
public interface ISocketAdapter extends ISocket {
	/**
	 * Create the ssh tunnel and socket connection.
	 * Note that if this method throws an exception, the caller
	 * is responsible for calling {@link #close()} to ensure
	 * that all resources are cleaned up (including the ssh
	 * tunnel process.) 
	 * 
	 * @throws IOException
	 */
	public void connect() throws IOException;
}
