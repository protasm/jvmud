/* File: creator_file(), a simul_efun.
// Description: Gives the name of the domain of a file. 
*/

string domain_file(string str)
{
   string *path;
   int i;
   
   path = explode(str, "/");
   if (!path) return 0;
   
	switch (path[0]) {
	case "adm":
		return "Adm";
	case "std":
		return "Std";
	case "bin":
		return "Bin";
	case "obj":
		return "Obj";
	case "room":
		return "Room";
	case "u":
		return "User";
	case "d":
		return capitalize(path[1]);
	default:
	}
	return "Backbone";
}
