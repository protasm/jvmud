// mudlib: Basis
// date:   1992/09/21

/*
// File: /adm/obj/simul_efun.c
// This file holds a list of simulated efuns, that is loaded when
// the game is booted.  
// Written by Buddha@TMI (2-19-92)
// This file is part of the TMI distribution mudlib.
// Please be considerate and retain the header file if you use it.
*/

/* do all the includes here (and not in the included files) */
#include <config.h>
#include <daemons.h>
#include <writef.h>
#undef DEBUG
#include <debug.h>
#include <message.h>

#include "/adm/obj/simul_efun/user_path.c"
#include "/adm/obj/simul_efun/creator_file.c"
#include "/adm/obj/simul_efun/query.c"
#include "/adm/obj/simul_efun/domain_file.c"
#include "/adm/obj/simul_efun/author_file.c"
#include "/adm/obj/simul_efun/dump_variable.c"
#include "/adm/obj/simul_efun/exclude_array.c"
#include "/adm/obj/simul_efun/find_object_or_load.c"
#include "/adm/obj/simul_efun/format_time.c"
#include "/adm/obj/simul_efun/path_file.c"
#include "/adm/obj/simul_efun/replace_string.c"
#include "/adm/obj/simul_efun/substr.c"
#include "/adm/obj/simul_efun/slice_array.c"
#include "/adm/obj/simul_efun/vt100.c"
#include "/adm/obj/simul_efun/writef.c"
#include "/adm/obj/simul_efun/base_name.c"
#include "/adm/obj/simul_efun/data.c"
#include "/adm/obj/simul_efun/getoid.c"
#include "/adm/obj/simul_efun/iobjects.c"
#include "/adm/obj/simul_efun/to_object.c"
#include "/adm/obj/simul_efun/communications.c"
#include "/adm/obj/simul_efun/existence.c"
#include "/adm/obj/simul_efun/resolv_path.c"
#include "/adm/obj/simul_efun/array.c"
#include "/adm/obj/simul_efun/pronouns.c"
#include "/adm/obj/simul_efun/temp_file.c"
#include "/adm/obj/simul_efun/help.c"
#include "/adm/obj/simul_efun/mkdirs.c"
#include "/adm/obj/simul_efun/pluralize.c"
#include "/adm/obj/simul_efun/extension.c"
#include "/adm/obj/simul_efun/overrides.c"
#include "/adm/obj/simul_efun/backcompat.c"
