void initialize(mixed first_load) {
}

string format_money(int copper) {
  int gold;
  int silver;
  int remainder;
  string result;

  if (copper <= 0) {
    return "no coin";
  }
  gold = copper / 100;
  remainder = copper % 100;
  silver = remainder / 10;
  remainder = remainder % 10;
  result = "";
  if (gold > 0) {
    result = gold + " gold";
  }
  if (silver > 0) {
    if (jvmud_size(result) > 0) {
      result += ", ";
    }
    result += silver + " silver";
  }
  if (remainder > 0) {
    if (jvmud_size(result) > 0) {
      result += ", ";
    }
    result += remainder + " copper";
  }
  return result;
}
