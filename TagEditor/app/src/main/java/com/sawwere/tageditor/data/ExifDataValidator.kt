package com.sawwere.tageditor.data

class ExifDataValidator {

    fun validateForm(exifData: ExifData): ValidationResult {
        val coordinatesValid = validateCoordinates(exifData)
        val dateTimeValid = validateDateTime(exifData.dateTime)

        return ValidationResult(
            isValid = coordinatesValid.isValid && dateTimeValid.isValid,
            coordinatesError = coordinatesValid.errorMessage,
            dateTimeError = dateTimeValid.errorMessage
        )
    }

    fun validateCoordinates(exifData: ExifData): FieldValidationResult {
        val lat = exifData.latitude
        val lon = exifData.longitude

        if (lat == null && lon == null) return FieldValidationResult(isValid = true)

        if (lat == null != (lon == null)) {
            return FieldValidationResult(
                isValid = false,
                errorMessage = "Заполните оба поля координат"
            )
        }

        val latValid = lat != null && lat in -90.0..90.0
        val lonValid = lon != null && lon in -180.0..180.0

        return if (latValid && lonValid) {
            FieldValidationResult(isValid = true)
        } else {
            FieldValidationResult(
                isValid = false,
                errorMessage = "Координаты должны быть в допустимых диапазонах"
            )
        }
    }

    fun validateDateTime(dateTime: String): FieldValidationResult {
        if (dateTime.isEmpty()) return FieldValidationResult(isValid = true)

        val pattern = Regex("^\\d{4}:\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}$")
        if (!pattern.matches(dateTime)) {
            return FieldValidationResult(
                isValid = false,
                errorMessage = "Неверный формат даты. Используйте: ГГГГ:ММ:ДД ЧЧ:ММ:СС"
            )
        }

        return try {
            val parts = dateTime.split(" ", ":")
            if (parts.size != 6) {
                return FieldValidationResult(
                    isValid = false,
                    errorMessage = "Неверный формат даты"
                )
            }

            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val hour = parts[3].toInt()
            val minute = parts[4].toInt()
            val second = parts[5].toInt()

            val isValid = year in 1900..2100 &&
                    month in 1..12 &&
                    day in 1..31 &&
                    hour in 0..23 &&
                    minute in 0..59 &&
                    second in 0..59

            if (isValid) {
                FieldValidationResult(isValid = true)
            } else {
                FieldValidationResult(
                    isValid = false,
                    errorMessage = "Некорректные значения даты/времени"
                )
            }
        } catch (e: Exception) {
            FieldValidationResult(
                isValid = false,
                errorMessage = "Ошибка при разборе даты"
            )
        }
    }

    fun isCoordinateValid(coord: Double?, min: Double, max: Double): Boolean {
        if (coord == null) return true
        return coord in min..max
    }

    fun isCoordinateEmpty(exifData: ExifData): Boolean {
        return exifData.latitude == null && exifData.longitude == null
    }

    fun isCoordinatePartiallyFilled(exifData: ExifData): Boolean {
        return (exifData.latitude == null) != (exifData.longitude == null)
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val coordinatesError: String? = null,
    val dateTimeError: String? = null
)

data class FieldValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)