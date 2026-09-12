/*
// Thanks to Huthar for this!
*/

string resolv_path(string curr, string new) {
    int i;
    string *tmp;
    string t1,t2,t3,t4;

    if(!new || new == ".") return curr;
    if(new == "here")
    {
        return file_name(environment(this_object())) + ".c";
    }
    if(new == "~" || new == "~/" )
      new = user_path((string)this_player()->query_name());
    if(sscanf(new,"~/%s",t1))
      new = user_path((string)this_player()->query_name()) + t1;
    else if(sscanf(new,"~%s",t1))
      new = user_path(t1); 
    else if(new[0] != '/')
      new = curr + "/" + new;

    if(new[strlen(new) - 1] != '/')
        new += "/";
    tmp = explode(new,"/");
    if (!tmp) tmp = ({"/"});
    for(i = 0; i < sizeof(tmp); i++)
        if(tmp[i] == "..") {
            if(sizeof(tmp) > 2) {
                tmp = tmp[0..(i-2)] + tmp[(i+1)..(sizeof(tmp)-1)];
                i -= 2;
            } else {
                tmp = tmp[2 ..(sizeof(tmp)-1)];
                i = 0;
            }
        }
     new = "/" + implode(tmp,"/");
     if(new == "//") new = "/";
     return new;
}
