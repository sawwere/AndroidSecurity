package com.example.inventory.ui.item

fun validateInput(itemDetails: ItemDetails): Map<String, String> {
    val errors = hashMapOf<String, String>()

    with(itemDetails) {
        if (name.isBlank()) {
            errors["name"] = "Name is required"
        }

        if (price.isBlank()) {
            errors["price"] = "Price is required"
        } else {
            try {
                val priceValue = price.toDouble()
                if (priceValue <= 0) {
                    errors["price"] = "Price must be greater than 0"
                }
            } catch (e: NumberFormatException) {
                errors["price"] = "Invalid price format"
            }
        }

        if (quantity.isBlank()) {
            errors["quantity"] = "Quantity is required"
        } else {
            try {
                val quantityValue = quantity.toInt()
                if (quantityValue <= 0) {
                    errors["quantity"] = "Quantity must be greater than 0"
                }
            } catch (e: NumberFormatException) {
                errors["quantity"] = "Quantity must be a whole number"
            }
        }

        if (supplierName.isBlank()) {
            errors["supplierName"] = "Supplier name is required"
        }

        if (supplierEmail.isBlank()) {
            errors["supplierEmail"] = "Supplier email is required"
        } else if (!isValidEmail(supplierEmail)) {
            errors["supplierEmail"] = "Invalid email format"
        }

        if (supplierPhone.isBlank()) {
            errors["supplierPhone"] = "Supplier phone is required"
        } else if (!isValidPhone(supplierPhone)) {
            errors["supplierPhone"] = "Invalid phone number format"
        }
    }
    return errors
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
    return email.matches(emailRegex.toRegex())
}

private fun isValidPhone(phone: String): Boolean {
    val phoneRegex = "^[+]?[0-9\\s\\-\\(\\)]{10,}\$"
    return phone.matches(phoneRegex.toRegex())
}