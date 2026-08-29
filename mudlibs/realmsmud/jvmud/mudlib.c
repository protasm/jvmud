void runtime_error(mixed actor, mixed context, mixed operation, mixed detail) {
    object master = jvmud_load_lpc_object("/secure/master.c");
    master->runtime_error(jvmud_to_string(detail), jvmud_to_string(context),
        jvmud_to_string(operation), 0);
}

void jvmud_startup() {
    object master = jvmud_load_lpc_object("/secure/master.c");
    master->inaugurate_master(0);
}
