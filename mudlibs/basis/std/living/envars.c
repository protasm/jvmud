// mudlib: Basis
// file:   env_vars.c
// date:   1992/09/07
// note:   envars in ./living because path is in environment and NPCs
//         may wish to execute commands (and hence have a path)

/*
  $Locker:  $

  $Source: /usr/local/mud/libs/basis/std/living/RCS/envars.c,v $
  $Revision: 1.1 $
  $Author: garnett $
  $Date: 92/09/24 06:33:16 $
  $State: Exp $

  $Log:	envars.c,v $
 * Revision 1.1  92/09/24  06:33:16  garnett
 * Initial revision
 * 
 * Revision 1.1  92/09/24  06:30:16  garnett
 * Initial revision
 * 
*/

#include <config.h>

private static inherit CORE;

private mapping evars;

void
create()
{
	evars = ([]);
}

mixed
getenv(string key)
{
	return evars[key];
}

void
setenv(string key, mixed value)
{
	if (geteuid(previous_object()) != ROOT_UID)
		return;
	evars[key] = value;
}

string *
envars()
{
	return keys(evars);
}

void
remove_envar(string key)
{
	if (getuid(previous_object()) != ROOT_UID)
		return;
	map_delete(evars, key);
}
