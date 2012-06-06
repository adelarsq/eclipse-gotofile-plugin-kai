/*
    GotoFile Eclipse Plugin - Quicksearch for files in Eclipse IDE
    Copyright (C) 2004 Max Muermann

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.muermann.gotofile;

import java.util.Comparator;

/**
 * @author max
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MatchComparatorFuzzy implements Comparator
{
	private static MatchComparatorFuzzy instance = new MatchComparatorFuzzy();

	public static MatchComparatorFuzzy getInstance()
	{
		return instance;
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2)
	{
		SearchResult r1 = (SearchResult)o1;
		SearchResult r2 = (SearchResult)o2;

		if (r1.isAllCapsMatched() && !r2.isAllCapsMatched())
			return -1;
		if (r2.isAllCapsMatched() && !r1.isAllCapsMatched())
			return 1;

		if (r1.getMatchConsecutive() == r2.getMatchConsecutive())
		{
			return r1.getMatchPos() == r2.getMatchPos() ? 0 : r1.getMatchPos() < r2.getMatchPos() ? -1 : 1;
		}

		if (r1.getMatchConsecutive() == r2.getMatchConsecutive())
		{
			if (r1.getFile().getName().length() < r1.getFile().getName().length())
				return -1;
			if (r1.getFile().getName().length() > r1.getFile().getName().length())
				return 1;
		}

		return r1.getMatchConsecutive() == r2.getMatchConsecutive() ? 0 : r1.getMatchConsecutive() > r2.getMatchConsecutive() ? -1 : 1;
	}
}
