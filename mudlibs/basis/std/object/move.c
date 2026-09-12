#include <move.h>

// The argument 'dest' is either a string or an object.

int move(mixed dest)
{
    object ob;

    if (stringp(dest)) {
        ob = find_object_or_load(dest);
        if (!ob) {
            return MOVE_FAILED;
        }
    } else if (objectp(dest)) {
        ob = dest;
    } else {
        return MOVE_FAILED;
    }
    efun::move_object(this_object(), ob);
    return MOVE_OK;
}
