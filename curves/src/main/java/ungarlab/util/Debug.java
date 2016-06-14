/* c 2001 Andrew Schein and Alexandrin Popescul

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 
*/


package ungarlab.util;

//This is a Debug class.  It lets me turn on assert statements and 
// Debug printings at runtime.

public class Debug {
	
    // you can set this to false to speed things up.
    public final static boolean status = true;
    
    public static void println(String str) {

	if (status) {

	    System.out.println(str);

	}

    }

    public static void assertm(boolean b) {
	if (!b) {
		
	    throw new RuntimeException();

	}

    }

}
