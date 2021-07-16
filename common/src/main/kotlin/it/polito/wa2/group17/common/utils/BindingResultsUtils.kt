package it.polito.wa2.group17.common.utils

import org.springframework.validation.BindingResult

fun BindingResult.extractErrors() =
    allErrors.map { e -> "${e.defaultMessage}" }
