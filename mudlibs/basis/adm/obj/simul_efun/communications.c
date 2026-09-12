// file:   /adm/obj/simul_efun/communications.c
// mudlib: Basis
// desc:   communications simul_efuns written to use message()
// note:   originally from Portals mudlib

varargs void say(string msg, mixed exclude)
{
	if (!exclude)
		exclude = ({this_player()});
	else if (objectp(exclude))
		exclude = ({ this_player(), exclude });
	else if (pointerp(exclude))
		exclude += ({ this_player() });
	message(mc_say, msg, environment(this_player()), exclude);
}

varargs void shout(string msg, mixed exclude)
{
	if (!exclude)
		exclude = ({ this_player()} );
	else if (objectp(exclude))
		exclude = ({ this_player(), exclude });
	else if (pointerp(exclude))
		exclude += ({ this_player() });
	message(mc_shout, msg, users(), exclude);
}

void tell_object(mixed ob, string msg)
{
	message(mc_tell, msg, ob);
}

varargs void tell_room(mixed room, string msg, mixed exclude)
{
	message(mc_tell, msg, room, exclude);
}
