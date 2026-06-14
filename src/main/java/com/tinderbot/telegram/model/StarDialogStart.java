package com.tinderbot.telegram.model;

public record StarDialogStart(
        Star star,
        String starKey,
        String photoKey,
        String prompt,
        String instruction
) {}
