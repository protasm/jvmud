string type;
string code;


string short()
{
    return "A " + type + " key";
}

int set_key_data( str)
{
    if ( sscanf(str, "%s %s", type, code) == 2)
        return 1;
    return 2;
}
void long()
{
    write("\nThis a " + type + " key, wonder where it fits?\n");
}

int id( strang)
{
    if ( ( strang == "key" )||( strang == type + " key")||( strang == "H_key") )
        return 1;
    return 0;
}

int get()
{
    return 1;
}

int query_value()
{
    return 10;
}

string query_type() { return type; }
string query_code() { return code; }

void set_type( str) { type = str; }
void set_code( str) { code = str; }

void init()
{
}

void reset( arg)
{
    if(arg)
        return;
    type = 0;
    code = 0;
}

