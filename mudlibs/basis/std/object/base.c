//  mudlib:   Basis
//  file:     base.c
//  author:   John Garnett
//  modified: 1992/12/13
//  purpose:  this is the basic object to be inherited by most typical
//            objects (excluding daemons and bin commands etc).

#include <config.h>
#include <attributes.h>
#include <daemons.h>

inherit CORE;
inherit MOVE;

static string this_euid;

mixed
query(mixed key)
{
	mixed attr;

    if (functionp(attr = core::query(key))) {
		return (*attr)(key);
	}
	return attr;
}

static void
set_lfuns(mixed key, mixed value)
{
	switch (key) {
		case a_filename :
			if (strcmp(getuid(previous_object()), ADMIN_UID)) {
				return;
			}
			PLAYER_D->load_data((string)this_object()->query(a_name));
			PLAYER_D->set(key, value);
			PLAYER_D->save_data();
			break;
		default :
			break;
	}
}

void
set(mixed key, mixed value)
{
	string euid;

	switch (key) {
	case ADMIN_RANGE :  // attributes only settable by commands in /bin/admin
		if (strcmp(getuid(previous_object()), ADMIN_UID)) {
			return;
		}
		core::set(key, value);
		break;
	case EFUNS :
		break;
	case LFUNS :
		set_lfuns(key, value);
		break;
	default :
		euid = geteuid(previous_object());
		if (!euid) {
			euid = getuid(previous_object());
		}
		if (strcmp(euid, ROOT_UID) && strcmp(euid, this_euid)) {
			return;
		}
		core::set(key, value);
		break;
	}
}

int
id(string str)
{
	string *ids;

	if (!(ids = query(a_ids))) {
		return 0;
	}
	return (member_array(str, ids) != -1);
}

void
setup_efun_attributes()
{
    function qef, qlf;

    // set up attributes that are currently mapped to efuns
    qef = (: SIMUL_EFUN_OB, "query_efuns" :);
    core::set(a_contains, qef);    // all_inventory
    core::set(a_super, qef);       // environment
    core::set(a_uid, qef);         // getuid
    core::set(a_ip_address, qef);  // query_ip_address
    qlf = (: SIMUL_EFUN_OB, "query_lfuns" :);
    core::set(a_filename, qlf);
}

void
create()
{
	this_euid = geteuid(this_object());
	if (!this_euid) {
		this_euid = getuid(this_object());
	}
	core::create();
	setup_efun_attributes();
}

// EOF
