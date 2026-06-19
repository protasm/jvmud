mixed *allocate(int size) {
    return jvmud_allocate(size);
}

mapping filter_indices(mapping values, function callback) {
    return jvmud_filter_indices(values, callback);
}

mapping filter_indices(mapping values, function callback, mixed arg1) {
    return jvmud_filter_indices(values, callback, arg1);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2) {
    return jvmud_filter_indices(values, callback, arg1, arg2);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2,
    mixed arg3) {
    return jvmud_filter_indices(values, callback, arg1, arg2, arg3);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2,
    mixed arg3, mixed arg4) {
    return jvmud_filter_indices(values, callback, arg1, arg2, arg3, arg4);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2,
    mixed arg3, mixed arg4, mixed arg5) {
    return jvmud_filter_indices(values, callback, arg1, arg2, arg3, arg4, arg5);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2,
    mixed arg3, mixed arg4, mixed arg5, mixed arg6) {
    return jvmud_filter_indices(values, callback, arg1, arg2, arg3, arg4, arg5, arg6);
}

int sscanf(mixed input, mixed format, mixed capture1) {
    return jvmud_sscanf(input, format, capture1);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2) {
    return jvmud_sscanf(input, format, capture1, capture2);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2, mixed capture3) {
    return jvmud_sscanf(input, format, capture1, capture2, capture3);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2, mixed capture3,
    mixed capture4) {
    return jvmud_sscanf(input, format, capture1, capture2, capture3, capture4);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2, mixed capture3,
    mixed capture4, mixed capture5) {
    return jvmud_sscanf(input, format, capture1, capture2, capture3, capture4, capture5);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2, mixed capture3,
    mixed capture4, mixed capture5, mixed capture6) {
    return jvmud_sscanf(input, format, capture1, capture2, capture3, capture4, capture5,
        capture6);
}
