package com.attendai.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class GraphData {

    private List<String> labels;
    private List<Integer> data;
}
