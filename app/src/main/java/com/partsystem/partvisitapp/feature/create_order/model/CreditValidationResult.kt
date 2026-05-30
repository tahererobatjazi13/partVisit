package com.partsystem.partvisitapp.feature.create_order.model

import com.partsystem.partvisitapp.core.utils.MessageKind

data class CreditValidationResult(
    var isValid: Boolean = true,
    var message: String = "",
    var messageKind: MessageKind
)