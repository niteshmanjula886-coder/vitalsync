package com.simats.vitalsync.ui.vitals.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.model.vitals.AddVitalReadingRequest
import com.simats.vitalsync.data.model.vitals.BloodPressureResponse
import com.simats.vitalsync.data.model.vitals.BmiDetailData
import com.simats.vitalsync.data.model.vitals.HeartRateDetailData
import com.simats.vitalsync.data.model.vitals.ManualVitalsLogRequest
import com.simats.vitalsync.data.model.vitals.Spo2Response
import com.simats.vitalsync.data.model.vitals.VitalDetailGuidanceResponse
import com.simats.vitalsync.data.model.vitals.VitalReadingItem
import com.simats.vitalsync.data.model.vitals.VitalsSummaryResponse
import com.simats.vitalsync.data.network.ApiConstants
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.data.repository.Resource
import com.simats.vitalsync.data.repository.VitalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.ConnectException
import java.net.SocketTimeoutException

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

data class VitalGuidanceUiState(
    val isLoading: Boolean = false,
    val data: VitalDetailGuidanceResponse? = null,
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

    private val _manualLogState = MutableStateFlow(AddReadingUiState())
    val manualLogState: StateFlow<AddReadingUiState> = _manualLogState.asStateFlow()

    private val _aiInsightState = MutableStateFlow<AiInsightResponse?>(null)
    val aiInsightState: StateFlow<AiInsightResponse?> = _aiInsightState.asStateFlow()

    private val _hrDetailState = MutableStateFlow(VitalGuidanceUiState())
    val hrDetailState: StateFlow<VitalGuidanceUiState> = _hrDetailState.asStateFlow()

    private val _spo2DetailState = MutableStateFlow(VitalGuidanceUiState())
    val spo2DetailState: StateFlow<VitalGuidanceUiState> = _spo2DetailState.asStateFlow()

    private val _bpDetailState = MutableStateFlow(VitalGuidanceUiState())
    val bpDetailState: StateFlow<VitalGuidanceUiState> = _bpDetailState.asStateFlow()

    private fun collectVitalDetail(
        targetState: MutableStateFlow<VitalGuidanceUiState>,
        flowProvider: () -> kotlinx.coroutines.flow.Flow<Resource<VitalDetailGuidanceResponse>>
    ) {
        viewModelScope.launch {
            flowProvider().collect { resource ->
                when (resource) {
                    is Resource.Loading -> targetState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> targetState.update { it.copy(isLoading = false, data = resource.data, errorMessage = null) }
                    is Resource.Error -> targetState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                }
            }
        }
    }

    fun fetchHrDetails(userId: Int? = null, email: String? = null) {
        collectVitalDetail(_hrDetailState) { vitalsRepository.getHrDetails(userId, email) }
    }

    fun fetchSpo2Details(userId: Int? = null, email: String? = null) {
        collectVitalDetail(_spo2DetailState) { vitalsRepository.getSpo2Details(userId, email) }
    }

    fun fetchBpDetails(userId: Int? = null, email: String? = null) {
        collectVitalDetail(_bpDetailState) { vitalsRepository.getBpDetails(userId, email) }
    }

    private val _isAiInsightLoading = MutableStateFlow(false)
    val isAiInsightLoading: StateFlow<Boolean> = _isAiInsightLoading.asStateFlow()

    fun fetchAiInsight(
        userId: Int = 1,
        isBtConnected: Boolean = false,
        hydrationMl: Int? = null,
        sleepText: String? = null,
        sleepScore: Int? = null,
        steps: Int? = null,
        stepGoal: Int? = null,
        targetWaterMl: Int? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiInsightLoading.value = true
            try {
                val response = withTimeout(12_000L) {
                    RetrofitClient.aiApiService.getAiInsight(
                        userId = userId,
                        isBtConnected = isBtConnected,
                        hydrationMl = hydrationMl,
                        sleepText = sleepText,
                        sleepScore = sleepScore,
                        steps = steps,
                        stepGoal = stepGoal,
                        targetWaterMl = targetWaterMl
                    )
                }
                if (response.isSuccessful && response.body() != null) {
                    _aiInsightState.value = response.body()
                }
            } catch (_: Exception) {
                // Non-critical background insight: silently retain cached state on timeout/network failure
            } finally {
                _isAiInsightLoading.value = false
            }
        }
    }

    fun fetchSummary(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            try {
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
            } catch (e: SocketTimeoutException) {
                _summaryState.update { it.copy(isLoading = false, errorMessage = ApiConstants.SERVER_UNREACHABLE_MESSAGE) }
            } catch (e: ConnectException) {
                _summaryState.update { it.copy(isLoading = false, errorMessage = ApiConstants.SERVER_UNREACHABLE_MESSAGE) }
            } catch (e: Exception) {
                _summaryState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            }
        }
    }

    fun clearSummaryError() {
        _summaryState.update { it.copy(errorMessage = null) }
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

    fun logManualVitals(
        heartRate: Int? = null,
        spo2: Int? = null,
        systolic: Int? = null,
        diastolic: Int? = null,
        notes: String? = null,
        userId: Int? = null,
        email: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val req = ManualVitalsLogRequest(
                userId = userId,
                email = email,
                heartRate = heartRate,
                spo2 = spo2,
                systolic = systolic,
                diastolic = diastolic,
                notes = notes?.takeIf { it.isNotBlank() }
            )
            vitalsRepository.logManualVitals(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _manualLogState.update { it.copy(isSaving = true, errorMessage = null, isSuccess = false) }
                    }
                    is Resource.Success -> {
                        _manualLogState.update { it.copy(isSaving = false, isSuccess = true, errorMessage = null) }
                        fetchSummary(userId, email)
                        if (heartRate != null) fetchHrDetails(userId, email)
                        if (spo2 != null) fetchSpo2Details(userId, email)
                        if (systolic != null && diastolic != null) fetchBpDetails(userId, email)
                        onSuccess()
                    }
                    is Resource.Error -> {
                        _manualLogState.update {
                            it.copy(isSaving = false, isSuccess = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun resetManualLogState() {
        _manualLogState.value = AddReadingUiState()
    }

    fun resetAddReadingState() {
        _addReadingState.value = AddReadingUiState()
    }
}

typealias VitalSignsViewModel = VitalsViewModel

