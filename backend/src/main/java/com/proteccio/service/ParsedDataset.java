package com.proteccio.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ParsedDataset {
    private final List<String> headers;
    private final List<List<String>> rows;
}
