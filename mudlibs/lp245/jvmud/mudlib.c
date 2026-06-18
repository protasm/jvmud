/*
* JVMud declarations for this mudlib.
*
* This object is intentionally separate from the vanilla LPMUD 2.4.5 files.
*/
string player_prompt() {
  return "> ";
}

void log_error(mixed file, mixed err) {
  jvmud_append_mudlib_text("/log/COMPILER", file + "\n");
  jvmud_append_mudlib_text("/log/COMPILER", err + "\n");
}

mixed prepare_destruct(mixed ob) {
  object item;
  object super;

  super = jvmud_entity_location(ob);
  if (super) {
    while (item = jvmud_first_entity_at(ob))
      jvmud_move_entity(item, super);
  } else {
    while (item = jvmud_first_entity_at(ob))
      jvmud_destroy_entity(item);
  }

  return 0;
}

void runtime_error(mixed actor, mixed context, mixed operation, mixed detail) {
  string message;

  message = "Your sensitive mind notices a wrongness in the fabric of space.\n";
  if (actor) {
    jvmud_send_to_entity(actor, message);
  } else {
    jvmud_write(message);
  }

  jvmud_append_mudlib_text("/log/RUNTIME", "context=" + context + " operation=" + operation + "\n");
  jvmud_append_mudlib_text("/log/RUNTIME", detail + "\n");
}

mixed heart_beat_error(mixed culprit, mixed err, mixed prg, mixed curobj, mixed line) {
  if (culprit) {
    jvmud_send_to_entity(culprit, "Game driver tells you: You have no heart beat !\n");
  }

  jvmud_append_mudlib_text("/log/HEART_BEAT", "culprit=" + curobj + " program=" + prg + " line=" + line + "\n");
  jvmud_append_mudlib_text("/log/HEART_BEAT", err + "\n");

  return 0;
}

void notify_shutdown(mixed crash_reason) {
  if (crash_reason) {
    jvmud_append_mudlib_text("/log/SHUTDOWN", "PANIC! " + crash_reason + "\n");
  } else {
    jvmud_append_mudlib_text("/log/SHUTDOWN", "LPmud shutting down immediately.\n");
  }
}
