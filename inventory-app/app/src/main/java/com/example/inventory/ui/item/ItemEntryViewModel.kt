/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.inventory.data.AppSettingsManager
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import java.text.NumberFormat

/**
 * ViewModel to validate and insert items in the Room database.
 */
class ItemEntryViewModel(
    private val itemsRepository: ItemsRepository,
    private val settingsManager: AppSettingsManager
) : ViewModel() {

    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    init {
        if (settingsManager.useDefaultQuantity) {
            itemUiState = itemUiState.copy(
                itemDetails = itemUiState.itemDetails.copy(quantity = settingsManager.defaultQuantity),
                errors = itemUiState.errors
            )
        }
    }

    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState = ItemUiState(
            itemDetails = itemDetails,
            errors = emptyMap()
        )
    }

    suspend fun saveItem(): Boolean {
        val currentErrors = validateInput(itemUiState.itemDetails)
        itemUiState = itemUiState.copy(errors = currentErrors)

        return if (currentErrors.isEmpty()) {
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
            true
        } else {
            false
        }
    }
}

/**
 * Represents Ui State for an Item.
 */
data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    var errors: Map<String, String> = HashMap()
)

data class ItemDetails(
    val id: Int = 0,
    val name: String = "",
    val price: String = "",
    val quantity: String = "",
    val supplierName: String = "",
    val supplierPhone: String = "",
    val supplierEmail: String = "",
)

/**
 * Extension function to convert [ItemDetails] to [Item]. If the value of [ItemDetails.price] is
 * not a valid [Double], then the price will be set to 0.0. Similarly if the value of
 * [ItemDetails.quantity] is not a valid [Int], then the quantity will be set to 0
 */
fun ItemDetails.toItem(): Item = Item(
    id = id,
    name = name,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity.toIntOrNull() ?: 0,
    supplierName = supplierName,
    supplierEmail = supplierEmail,
    supplierPhone = supplierPhone
)

fun Item.formatedPrice(): String {
    return NumberFormat.getCurrencyInstance().format(price)
}

/**
 * Extension function to convert [Item] to [ItemUiState]
 */
fun Item.toItemUiState(): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    errors = emptyMap()
)

/**
 * Extension function to convert [Item] to [ItemDetails]
 */
fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    name = name,
    price = price.toString(),
    quantity = quantity.toString(),
    supplierName = supplierName,
    supplierEmail = supplierEmail,
    supplierPhone = supplierPhone
)
