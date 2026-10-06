package com.example.demo.retirement.api.dto;

import com.example.demo.retirement.models.MaturityOption;

/** Customer's maturity option selection. */
public record MaturityOptionRequest(MaturityOption maturityOption) {
}