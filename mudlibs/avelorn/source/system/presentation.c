string pending_text;

void initialize(mixed first_load) {
  pending_text = "";
}

void write(mixed value) {
  string text;
  string line;
  int index;
  int start;

  text = pending_text + jvmud_to_string(value);
  pending_text = "";
  start = 0;
  index = 0;
  while (index < jvmud_size(text)) {
    if (text[index] == '\n') {
      if (index > start) {
        line = jvmud_extract_text(text, start, index - 1);
      } else {
        line = "";
      }
      write_complete_line(line);
      start = index + 1;
    }
    index += 1;
  }
  if (start < jvmud_size(text)) {
    pending_text = jvmud_extract_text(text, start);
  }
}

void avelorn_prompt(mixed value) {
  flush_pending_text();
  jvmud_write(value);
}

void avelorn_write_to(object target, mixed value) {
  jvmud_write_to_lpc_object(target, jvmud_wrap_text(value, 80));
}

void avelorn_emit_at(mixed place, mixed value) {
  jvmud_emit_perceivable_at(place, jvmud_wrap_text(value, 80));
}

void avelorn_emit_except(mixed place, mixed value, mixed excluded) {
  jvmud_emit_perceivable_except(place, jvmud_wrap_text(value, 80), excluded);
}

void flush_pending_text() {
  if (jvmud_size(pending_text) > 0) {
    jvmud_write(jvmud_wrap_text(pending_text, 80));
    pending_text = "";
  }
}

void write_complete_line(string line) {
  string content;
  string prefix;
  string segment;
  string wrapped;
  int index;
  int start;

  prefix = "";
  index = 0;
  while (index < jvmud_size(line) && line[index] == ' ') {
    prefix += " ";
    index += 1;
  }
  if (index == jvmud_size(line)) {
    jvmud_write("\n");
  } else {
    content = jvmud_extract_text(line, index);
    wrapped = jvmud_wrap_text(content, 80 - jvmud_size(prefix));
    start = 0;
    index = 0;
    while (index < jvmud_size(wrapped)) {
      if (wrapped[index] == '\n') {
        if (index > start) {
          segment = jvmud_extract_text(wrapped, start, index - 1);
          jvmud_write(prefix + segment + "\n");
        } else {
          jvmud_write("\n");
        }
        start = index + 1;
      }
      index += 1;
    }
  }
  if (is_room_heading(line)) {
    jvmud_write(room_ruler() + "\n");
  }
}

string room_ruler() {
  string ruler;
  string segment;
  int repeats;

  segment = "+=========";
  ruler = "";
  repeats = 0;
  while (repeats < 8) {
    ruler += segment;
    repeats += 1;
  }
  return ruler;
}

int is_room_heading(string line) {
  object actor;
  object place;

  actor = jvmud_current_actor();
  if (!actor) {
    return 0;
  }
  place = jvmud_entity_location(actor);
  if (!place || !jvmud_method_exists("short", place)) {
    return 0;
  }
  return line == jvmud_invoke_lpc_object(place, "short");
}
