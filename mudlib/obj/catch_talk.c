string match, fun, ob_str, type;
object next;
string super;
/*
short() { return type + "#" + match; }

*/
collaps() {
  if(next)
    call_other(next, "collaps");

  destruct(this_object());
}

link(ob) {
  next = ob;
}

remove_match(str) {
  if (str == match) {
    destruct(this_object());

    return next;
  }

  if (next)
    next = call_other(next, "remove_match", str);

  return this_object();
}

/*
* The function to call.
*/
set_function(f) {
  fun = f;
}

/*
* The string to match.
*/
set_match(str) {
  match = str;
}

/*
* The object to call.
*/
set_object(ob) {  /* NOTE: a string */
  ob_str = ob;
}

/*
* The function to call.
*/
set_type(t) {
  type = t;
}

test_match(str) {
  string who,str1;

  if(sscanf(str,"%s " + type + match + " %s\n",who,str1) == 2 ||
    sscanf(str,"%s " + type + match + "\n",who) == 1 ||

  sscanf(str,"%s " + type + match + "%s\n",who,str1) == 2 ||
  sscanf(str,"%s " + type + " " + match + "\n",who) == 1 ||
  sscanf(str,"%s " + type + " " + match + " %s\n",who,str1) == 2) {
    return call_other(ob_str, fun, str);
  }
  if (next)
    return call_other(next, "test_match", str);

  else
    return 0;
}
drop() { return 1; }
