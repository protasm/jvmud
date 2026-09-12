/* File: author_file(), a simul_efun.
// Description: Gives the name of the author of a file. 
*/

string author_file(string str)
{
   string *path;
   int i;
   
   path = explode(str, "/");
   if (!path) return 0;
   
	switch (path[0]) {
	case "u":
		if (path[2] && path[3]) {
			return path[2];
		}
		break;
	default:
		return 0;
	}
	return 0;
}
