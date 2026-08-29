object compile_world_room(string path) {
  object room;
  string number;
  int index;

  if (jvmud_size(path) != 18 || jvmud_extract_text(path, 0, 12) != "place/world/r") {
    return 0;
  }
  number = jvmud_extract_text(path, 13);
  index = jvmud_to_int(number);
  if (index < 0 || index >= 99935 || jvmud_format_text("%05d", index) != number) {
    return 0;
  }
  room = jvmud_clone_lpc_object("system/world_room");
  jvmud_invoke_lpc_object(room, "configure", index);
  return room;
}
