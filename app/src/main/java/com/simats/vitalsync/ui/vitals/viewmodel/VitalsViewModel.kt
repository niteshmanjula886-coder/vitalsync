package com.simats.vitalsync.ui.vitals.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.model.vitals.AddVitalReadingRequest
import com.simats.vitalsync.data.model.vitals.BloodPressureResponse
import com.simats.vitalsync.data.model.vitals.BmiDetailData
import com.simats.vitalsync.data.model.vitals.HeartRateDetailData
import com.simats.vitalsync.data.model.vitals.Spo2Response
import com.simats.vitalsync.data.model.vitals.VitalReadingItem
import com.simats.vitalsync.data.model.vitals.VitalsSummaryResponse
import com.simats.vitalsync.data.repository.Resource
import com.simats.vitalsync.data.repository.VitalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VitalsSummaryUiState(
    val isLoading: Boolean = false,
    val summary: VitalsSummaryResponse? = null,
    val errorMessage: String? = null
)

data class HeartRateUiState(
    val isLoading: Boolean = false,
    val data: HeartRateDetailData? = null,
    val errorMessage: String? = null
)

data class BloodPressureUiState(
    val isLoading: Boolean = false,
    val data: BloodPressureResponse? = null,
    val errorMessage: String? = null
)

data class Spo2UiState(
    val isLoading: Boolean = false,
    val data: Spo2Response? = null,
    val errorMessage: String? = null
)

data class BmiUiState(
    val isLoading: Boolean = false,
    val data: BmiDetailData? = null,
    val errorMessage: String? = null
)

data class ReadingsUiState(
    val isLoading: Boolean = false,
    val readings: List<VitalReadingItem> = emptyList(),
    val errorMessage: String? = null
)

data class AddReadingUiState(
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class VitalsViewModel(
    private val vitalsRepository: VitalsRepository
) : ViewModel() {

    private val _summaryState = MutableStateFlow(VitalsSummaryUiState())
    val summaryState: StateFlow<VitalsSummaryUiState> = _summaryState.asStateFlow()

    private val _heartRateState = MutableStateFlow(HeartRateUiState())
    val heartRateState: StateFlow<HeartRateUiState> = _heartRateState.asStateFlow()

    private val _bloodPressureState = MutableStateFlow(BloodPressureUiState())
    val bloodPressureState: StateFlow<BloodPressureUiState> = _bloodPressureState.asStateFlow()

    private val _spo2State = MutableStateFlow(Spo2UiState())
    val spo2State: StateFlow<Spo2UiState> = _spo2State.asStateFlow()

    private val _bmiState = MutableStateFlow(BmiUiState())
    val bmiState: StateFlow<BmiUiState> = _bmiState.asStateFlow()

    private val _readingsState = MutableStateFlow(ReadingsUiState())
    val readingsState: StateFlow<ReadingsUiState> = _readingsState.asStateFlow()

    private val _addReadingState = MutableStateFlow(AddReadingUiState())
    val addReadingState: StateFlow<AddReadingUiState> = _addReadingState.asStateFlow()

    fun fetchSummary(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            vitalsRepository.getSummary(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _summaryState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _summaryState.update {
                            it.copy(isLoading = false, summary = resource.data, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _summaryState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun fetchHeartRate(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            vitalsRepository.getHeartRate(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _heartRateState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _heartRateState.update {
                            it.copy(isLoading = false, data = resource.data, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _heartRateState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun fetchBloodPressure(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            vitalsRepository.getBloodPressure(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _bloodPressureState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _bloodPressureState.update {
                            it.copy(isLoading = false, data = resource.data, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _bloodPressureState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun fetchSpo2(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            vitalsRepository.getSpo2(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _spo2State.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _spo2State.update {
                            it.copy(isLoading = false, data = resource.data, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _spo2State.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun fetchBmi(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            vitalsRepository.getBmi(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _bmiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _bmiState.update {
                            it.copy(isLoading = false, data = resource.data, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _bmiState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun fetchReadings(userId: Int? = null, email: String? = null, type: String? = null) {
        viewModelScope.launch {
            vitalsRepository.getReadings(userId, email, type).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _readingsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _readingsState.update {
                            it.copy(isLoading = false, readings = resource.data, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _readingsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun addReading(
        readingType: String,
        valueNumeric: Float? = null,
        valueSecondary: Float? = null,
        valueText: String? = null,
        unit: String? = null,
        statusLabel: String? = null,
        notes: String? = null,
        userId: Int? = null,
        email: String? = null
    ) {
        viewModelScope.launch {
            val req = AddVitalReadingRequest(
                userId = userId,
                email = email,
                readingType = readingType,
                valueNumeric = valueNumeric,
                valueSecondary = valueSecondary,
                valueText = valueText,
                unit = unit,
                statusLabel = statusLabel,
                notes = notes
            )
            vitalsRepository.addReading(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _addReadingState.update { it.copy(isSaving = true, errorMessage = null, isSuccess = false) }
                    }
                    is Resource.Success -> {
                        _addReadingState.update {
                            it.copy(isSaving = false, isSuccess = true, errorMessage = null)
                        }
                        // Refresh summary and readings after successful recording
                        fetchSummary(userId, email)
                        fetchReadings(userId, email, readingType)
                    }
                    is Resource.Error -> {
                        _addReadingState.update {
                            it.copy(isSaving = false, isSuccess = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun deleteReading(id: Int, userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            vitalsRepository.deleteReading(id, userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _readingsState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _readingsState.update {
                            it.copy(
                                isLoading = false,
                                readings = it.readings.filter { r -> r.id != id }
                            )
                        }
                        fetchSummary(userId, email)
                    }
                    is Resource.Error -> {
                        _readingsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun resetAddReadingState() {
        _addReadingState.value = AddReadingUiState()
    }
}

