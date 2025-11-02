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

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve and update an item from the [ItemsRepository]'s data source.
 */
class ItemEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    /**
     * Holds current item ui state
     */
    private val _itemUiState = mutableStateOf(ItemUiState())
    val itemUiState: State<ItemUiState> get() = _itemUiState

    private val itemId: Int = checkNotNull(savedStateHandle[ItemEditDestination.itemIdArg])

    init {
        viewModelScope.launch {
            itemsRepository.getItemStream(itemId)
                .filterNotNull()
                .first()
                .toItemUiState()
                .let { loadedState ->
                    _itemUiState.value = loadedState
                }
        }
    }


    /**
     * Update the item in the [ItemsRepository]'s data source
     */
    suspend fun updateItem(): Boolean {
        val currentErrors = validateInput(_itemUiState.value.itemDetails)
        _itemUiState.value = _itemUiState.value.copy(errors = currentErrors)

        return if (currentErrors.isEmpty()) {
            itemsRepository.updateItem(_itemUiState.value.itemDetails.toItem())
            true
        } else {
            false
        }
    }

    /**
     * Updates the [itemUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(itemDetails: ItemDetails) {
        val originalSource = _itemUiState.value.itemDetails.source
        _itemUiState.value = ItemUiState(
            itemDetails = itemDetails.copy(source = originalSource),
            errors = validateInput(itemDetails)
        )
    }
}
