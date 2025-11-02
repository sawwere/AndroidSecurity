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

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.AppSettingsManager
import com.example.inventory.data.EncryptedFileManager
import com.example.inventory.data.Item
import com.example.inventory.data.ItemSource
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat

/**
 * ViewModel to validate and insert items in the Room database.
 */
class ItemEntryViewModel(
    private val itemsRepository: ItemsRepository,
    private val settingsManager: AppSettingsManager,
    private val encryptedFileManager: EncryptedFileManager
) : ViewModel() {

    /**
     * Holds current item ui state
     */
    private val _itemUiState = mutableStateOf(ItemUiState())
    val itemUiState: State<ItemUiState> get() = _itemUiState

    init {
        if (settingsManager.useDefaultQuantity) {
            _itemUiState.value = _itemUiState.value.copy(
                itemDetails = _itemUiState.value.itemDetails.copy(
                    quantity = settingsManager.defaultQuantity
                )
            )
        }
    }

    fun updateUiState(itemDetails: ItemDetails) {
        val originalSource = _itemUiState.value.itemDetails.source
        _itemUiState.value = ItemUiState(
            itemDetails = itemDetails.copy(
                // нельзя изменять источник через UI
                source = originalSource
            ),
            errors = validateInput(itemDetails)
        )
    }

    suspend fun saveItem(): Boolean {
        val currentErrors = validateInput(_itemUiState.value.itemDetails)
        _itemUiState.value = _itemUiState.value.copy(errors = currentErrors)

        return if (currentErrors.isEmpty()) {
            itemsRepository.insertItem(_itemUiState.value.itemDetails.toItem())
            true
        } else {
            false
        }
    }


    /**
     * Загружает товар из зашифрованного файла
     */
    fun loadItemFromFile(uri: Uri) {
        viewModelScope.launch {
            val item = encryptedFileManager.loadItemFromFile(uri)
            if (item != null) {
                val itemDetails = item.toItemDetails().copy(
                    source = ItemSource.FILE,
                    id = 0
                )
                _itemUiState.value = ItemUiState(
                    itemDetails = itemDetails,
                    errors = validateInput(itemDetails)
                )
            } else {
                Log.e("ItemEntryViewModel", "Failed to load item from file")
            }
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
    val source: ItemSource = ItemSource.MANUAL,
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
    supplierPhone = supplierPhone,
    source = source
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
    supplierPhone = supplierPhone,
    source = source
)
