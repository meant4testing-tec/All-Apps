package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LogDatabase
import com.example.data.LogEntry
import com.example.data.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToLong

enum class CalculatorMode {
    LOG, ANTILOG
}

enum class BasePreset(val label: String, val baseValue: Double, val displaySymbol: String) {
    BASE_10("log₁₀", 10.0, "10"),
    BASE_E("ln (e)", Math.E, "e"),
    BASE_2("log₂", 2.0, "2"),
    CUSTOM("Custom", Double.NaN, "b")
}

data class CalculationResult(
    val resultValue: Double,
    val formattedResult: String,
    val formattedExpression: String,
    val exponentialForm: String,
    val changeOfBaseLn: String,
    val changeOfBaseLog10: String,
    val characteristic: Long?,
    val mantissa: Double?,
    val scientificNotation: String?,
    val isExact: Boolean,
    val base: Double,
    val argument: Double
)

data class CalculatorUiState(
    val mode: CalculatorMode = CalculatorMode.LOG,
    val basePreset: BasePreset = BasePreset.BASE_10,
    val customBaseInput: String = "10",
    val argumentInput: String = "100",
    val activeBaseValue: Double = 10.0,
    val activeBaseDisplay: String = "10",
    val result: CalculationResult? = null,
    val errorMessage: String? = null,
    val isIdentitiesSheetOpen: Boolean = false,
    val isHistorySheetOpen: Boolean = false,
    val selectedDetailTab: Int = 0 // 0: Steps & Formulas, 1: Graph, 2: Properties
)

class LogCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LogRepository

    val historyEntries: StateFlow<List<LogEntry>>

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        val database = LogDatabase.getDatabase(application)
        repository = LogRepository(database.logDao())
        historyEntries = repository.allEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        // Perform initial calculation
        calculate()
    }

    fun setMode(mode: CalculatorMode) {
        _uiState.update { it.copy(mode = mode) }
        calculate()
    }

    fun setBasePreset(preset: BasePreset) {
        _uiState.update {
            val baseVal = if (preset == BasePreset.CUSTOM) {
                it.customBaseInput.toDoubleOrNull() ?: 10.0
            } else {
                preset.baseValue
            }
            val baseDisp = if (preset == BasePreset.CUSTOM) {
                if (it.customBaseInput.isNotBlank()) it.customBaseInput else "b"
            } else {
                preset.displaySymbol
            }
            it.copy(
                basePreset = preset,
                activeBaseValue = baseVal,
                activeBaseDisplay = baseDisp
            )
        }
        calculate()
    }

    fun onCustomBaseChanged(input: String) {
        val filtered = filterNumericInput(input)
        _uiState.update {
            val baseVal = filtered.toDoubleOrNull() ?: Double.NaN
            it.copy(
                customBaseInput = filtered,
                activeBaseValue = baseVal,
                activeBaseDisplay = if (filtered.isNotBlank()) filtered else "b"
            )
        }
        calculate()
    }

    fun onArgumentChanged(input: String) {
        val filtered = filterNumericInput(input)
        _uiState.update { it.copy(argumentInput = filtered) }
        calculate()
    }

    fun appendToArgument(char: String) {
        val current = _uiState.value.argumentInput
        val updated = when {
            char == "." && current.contains(".") -> current
            current == "0" && char != "." -> char
            else -> current + char
        }
        onArgumentChanged(updated)
    }

    fun appendToCustomBase(char: String) {
        val current = _uiState.value.customBaseInput
        val updated = when {
            char == "." && current.contains(".") -> current
            current == "0" && char != "." -> char
            else -> current + char
        }
        onCustomBaseChanged(updated)
    }

    fun backspaceArgument() {
        val current = _uiState.value.argumentInput
        if (current.isNotEmpty()) {
            val updated = current.dropLast(1)
            onArgumentChanged(updated)
        }
    }

    fun clearAll() {
        _uiState.update {
            it.copy(
                argumentInput = "",
                result = null,
                errorMessage = null
            )
        }
    }

    fun setPresetArgument(value: String) {
        onArgumentChanged(value)
    }

    fun setDetailTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedDetailTab = tabIndex) }
    }

    fun setIdentitiesSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isIdentitiesSheetOpen = isOpen) }
    }

    fun setHistorySheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isHistorySheetOpen = isOpen) }
    }

    fun restoreFromHistory(entry: LogEntry) {
        val mode = if (entry.mode == "ANTILOG") CalculatorMode.ANTILOG else CalculatorMode.LOG
        val preset = when (entry.baseDisplay) {
            "10" -> BasePreset.BASE_10
            "e" -> BasePreset.BASE_E
            "2" -> BasePreset.BASE_2
            else -> BasePreset.CUSTOM
        }
        _uiState.update {
            it.copy(
                mode = mode,
                basePreset = preset,
                customBaseInput = if (preset == BasePreset.CUSTOM) formatNumber(entry.base) else it.customBaseInput,
                activeBaseValue = entry.base,
                activeBaseDisplay = entry.baseDisplay,
                argumentInput = formatNumber(entry.argument),
                isHistorySheetOpen = false
            )
        }
        calculate(saveToHistory = false)
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun commitCurrentToHistory() {
        val currentResult = _uiState.value.result ?: return
        val state = _uiState.value
        viewModelScope.launch {
            repository.insert(
                LogEntry(
                    mode = if (state.mode == CalculatorMode.LOG) "LOG" else "ANTILOG",
                    base = currentResult.base,
                    baseDisplay = state.activeBaseDisplay,
                    argument = currentResult.argument,
                    result = currentResult.resultValue,
                    expression = currentResult.formattedExpression
                )
            )
        }
    }

    private fun calculate(saveToHistory: Boolean = false) {
        val state = _uiState.value
        val argText = state.argumentInput.trim()

        if (argText.isEmpty()) {
            _uiState.update { it.copy(result = null, errorMessage = null) }
            return
        }

        val argVal = argText.toDoubleOrNull()
        if (argVal == null) {
            _uiState.update { it.copy(result = null, errorMessage = "Invalid input number") }
            return
        }

        val baseVal: Double = when (state.basePreset) {
            BasePreset.BASE_10 -> 10.0
            BasePreset.BASE_E -> Math.E
            BasePreset.BASE_2 -> 2.0
            BasePreset.CUSTOM -> state.customBaseInput.toDoubleOrNull() ?: Double.NaN
        }

        if (baseVal.isNaN() || baseVal <= 0.0 || abs(baseVal - 1.0) < 1e-12) {
            _uiState.update {
                it.copy(
                    result = null,
                    errorMessage = "Base must be positive and not equal to 1"
                )
            }
            return
        }

        if (state.mode == CalculatorMode.LOG) {
            // Logarithm Mode: log_b(x)
            if (argVal <= 0.0) {
                _uiState.update {
                    it.copy(
                        result = null,
                        errorMessage = "Argument (x) must be greater than 0"
                    )
                }
                return
            }

            val resultVal: Double = when {
                abs(baseVal - 10.0) < 1e-12 -> log10(argVal)
                abs(baseVal - Math.E) < 1e-12 -> ln(argVal)
                abs(baseVal - 2.0) < 1e-12 -> log2(argVal)
                else -> ln(argVal) / ln(baseVal)
            }

            val isExact = isCloseToInteger(resultVal)
            val formattedRes = formatNumber(resultVal)
            val baseSub = toSubscript(state.activeBaseDisplay)
            val argDisp = formatNumber(argVal)

            val expr = when (state.basePreset) {
                BasePreset.BASE_E -> "ln($argDisp) = $formattedRes"
                BasePreset.BASE_10 -> "log₁₀($argDisp) = $formattedRes"
                BasePreset.BASE_2 -> "log₂($argDisp) = $formattedRes"
                BasePreset.CUSTOM -> "log$baseSub($argDisp) = $formattedRes"
            }

            val expForm = "${state.activeBaseDisplay}${toSuperscript(formattedRes)} = $argDisp"
            val lnArg = ln(argVal)
            val lnBase = ln(baseVal)
            val stepLn = "log$baseSub($argDisp) = ln($argDisp) / ln(${state.activeBaseDisplay}) = ${formatNumber(lnArg, 6)} / ${formatNumber(lnBase, 6)} ≈ $formattedRes"
            val log10Arg = log10(argVal)
            val log10Base = log10(baseVal)
            val stepLog10 = "log$baseSub($argDisp) = log₁₀($argDisp) / log₁₀(${state.activeBaseDisplay}) = ${formatNumber(log10Arg, 6)} / ${formatNumber(log10Base, 6)} ≈ $formattedRes"

            // Characteristic & Mantissa for base 10 or common view
            val charVal = floor(log10(argVal)).toLong()
            val mantissaVal = log10(argVal) - charVal
            val sciNot = formatScientific(argVal)

            val calculationResult = CalculationResult(
                resultValue = resultVal,
                formattedResult = formattedRes,
                formattedExpression = expr,
                exponentialForm = expForm,
                changeOfBaseLn = stepLn,
                changeOfBaseLog10 = stepLog10,
                characteristic = charVal,
                mantissa = mantissaVal,
                scientificNotation = sciNot,
                isExact = isExact,
                base = baseVal,
                argument = argVal
            )

            _uiState.update { it.copy(result = calculationResult, errorMessage = null) }

            if (saveToHistory) {
                commitCurrentToHistory()
            }
        } else {
            // Antilogarithm Mode: b^y = x
            val resultVal = baseVal.pow(argVal)
            val formattedRes = formatNumber(resultVal)
            val baseSub = toSubscript(state.activeBaseDisplay)
            val argDisp = formatNumber(argVal)
            val isExact = isCloseToInteger(resultVal)

            val expr = "${state.activeBaseDisplay}${toSuperscript(argDisp)} = $formattedRes"
            val expForm = when (state.basePreset) {
                BasePreset.BASE_E -> "ln($formattedRes) = $argDisp"
                BasePreset.BASE_10 -> "log₁₀($formattedRes) = $argDisp"
                BasePreset.BASE_2 -> "log₂($formattedRes) = $argDisp"
                BasePreset.CUSTOM -> "log$baseSub($formattedRes) = $argDisp"
            }

            val calculationResult = CalculationResult(
                resultValue = resultVal,
                formattedResult = formattedRes,
                formattedExpression = expr,
                exponentialForm = expForm,
                changeOfBaseLn = "Antilog: ${state.activeBaseDisplay}^($argDisp) = $formattedRes",
                changeOfBaseLog10 = "Inverse of log: log$baseSub($formattedRes) = $argDisp",
                characteristic = null,
                mantissa = null,
                scientificNotation = formatScientific(resultVal),
                isExact = isExact,
                base = baseVal,
                argument = argVal
            )

            _uiState.update { it.copy(result = calculationResult, errorMessage = null) }

            if (saveToHistory) {
                commitCurrentToHistory()
            }
        }
    }

    private fun filterNumericInput(input: String): String {
        val cleaned = input.filter { it.isDigit() || it == '.' || it == '-' }
        // Ensure at most one minus at start, at most one dot
        var hasDot = false
        val sb = StringBuilder()
        cleaned.forEachIndexed { index, c ->
            if (c == '-' && index == 0) {
                sb.append(c)
            } else if (c == '.') {
                if (!hasDot) {
                    sb.append(c)
                    hasDot = true
                }
            } else if (c.isDigit()) {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun isCloseToInteger(d: Double): Boolean {
        if (d.isInfinite() || d.isNaN()) return false
        val rounded = d.roundToLong()
        return abs(d - rounded) < 1e-9
    }

    private fun formatNumber(d: Double, maxDecimals: Int = 8): String {
        if (d.isNaN()) return "NaN"
        if (d.isInfinite()) return if (d > 0) "∞" else "-∞"
        if (isCloseToInteger(d)) {
            return d.roundToLong().toString()
        }
        if (abs(d) >= 1e12 || (abs(d) > 0 && abs(d) < 1e-6)) {
            return String.format(java.util.Locale.US, "%.6e", d)
        }
        val s = String.format(java.util.Locale.US, "%.${maxDecimals}f", d)
        return s.trimEnd('0').trimEnd('.')
    }

    private fun formatScientific(d: Double): String {
        if (d <= 0.0 || d.isNaN() || d.isInfinite()) return "N/A"
        val exp = floor(log10(d)).toInt()
        val mant = d / 10.0.pow(exp.toDouble())
        return String.format(java.util.Locale.US, "%.4f × 10%s", mant, toSuperscript(exp.toString()))
    }

    companion object {
        fun toSubscript(str: String): String {
            return str.map { char ->
                when (char) {
                    '0' -> '₀'
                    '1' -> '₁'
                    '2' -> '₂'
                    '3' -> '₃'
                    '4' -> '₄'
                    '5' -> '₅'
                    '6' -> '₆'
                    '7' -> '₇'
                    '8' -> '₈'
                    '9' -> '₉'
                    'e' -> 'ₑ'
                    '.' -> '.'
                    else -> char
                }
            }.joinToString("")
        }

        fun toSuperscript(str: String): String {
            return str.map { char ->
                when (char) {
                    '0' -> '⁰'
                    '1' -> '¹'
                    '2' -> '²'
                    '3' -> '³'
                    '4' -> '⁴'
                    '5' -> '⁵'
                    '6' -> '⁶'
                    '7' -> '⁷'
                    '8' -> '⁸'
                    '9' -> '⁹'
                    '-' -> '⁻'
                    '.' -> '·'
                    else -> char
                }
            }.joinToString("")
        }
    }
}
