/* _whatis.c - Jubal@TMI */

#include <daemons.h>
inherit "/bin/bin_m";

int
cmd_whatis( string arg )
{
	string name;

	if( ! arg )
	{
		notify_fail( "usage: whatis [secnum] <topic>\n" );
		return 0;
	}
	sscanf( arg, "%s %s", arg, name );
	write( "" + WHATIS_D -> whatis( arg, name ) );
	return 1;
}


/* EOF */
