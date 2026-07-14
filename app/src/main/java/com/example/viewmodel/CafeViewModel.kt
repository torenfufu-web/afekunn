package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.ChatMessageEntity
import com.example.data.database.MemoEntity
import com.example.data.repository.CafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Stack

// --- Calculator Actions ---
sealed class CalculatorAction {
    data class Number(val value: Int) : CalculatorAction()
    object Decimal : CalculatorAction()
    data class Operation(val op: String) : CalculatorAction() // "+", "-", "×", "÷"
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Calculate : CalculatorAction()
}

class CafeViewModel(private val repository: CafeRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            try {
                val firstList = repository.allMessages.first()
                if (firstList.isEmpty()) {
                    repository.insertMessage(
                        ChatMessageEntity(
                            sender = "barista",
                            message = "いらっしゃいませ！☕️ 「Cafe AI」へようこそ。私はマスターです。淹れたてのコーヒーでも飲みながら、ゆっくりお話ししましょう。お気に入りのメニューやレシピ、日常の雑談、何でも気軽に話しかけてくださいね。✨"
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // --- Memo / Notepad States ---
    val memos: StateFlow<List<MemoEntity>> = repository.allMemos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMemo(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insertMemo(
                MemoEntity(
                    title = title,
                    content = content,
                    category = category
                )
            )
        }
    }

    fun deleteMemo(id: Int) {
        viewModelScope.launch {
            repository.deleteMemoById(id)
        }
    }

    // --- Chat / Conversation States ---
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // Save User message
            val userMsg = ChatMessageEntity(sender = "user", message = text)
            repository.insertMessage(userMsg)

            // Trigger loading state
            _isChatLoading.value = true

            // Gather the full thread including current message for Gemini context
            val currentHistory = chatMessages.value.toMutableList()
            currentHistory.add(userMsg)

            // Query Barista response
            val replyText = repository.generateBaristaResponse(currentHistory)

            // Save Barista response
            repository.insertMessage(
                ChatMessageEntity(sender = "barista", message = replyText)
            )

            _isChatLoading.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            // Add a warm welcome message from the barista upon clearing
            repository.insertMessage(
                ChatMessageEntity(
                    sender = "barista",
                    message = "いらっしゃいませ！☕️ 「Cafe AI」へようこそ。私はマスターです。淹れたてのコーヒーでも飲みながら、ゆっくりお話ししましょう。お気に入りのメニューやレシピ、日常の雑談、何でも気軽に話しかけてくださいね。✨"
                )
            )
        }
    }

    // --- Calculator States ---
    private val _calcExpression = MutableStateFlow("")
    val calcExpression: StateFlow<String> = _calcExpression.asStateFlow()

    private val _calcResult = MutableStateFlow("0")
    val calcResult: StateFlow<String> = _calcResult.asStateFlow()

    private val _calcHistory = MutableStateFlow<List<String>>(emptyList())
    val calcHistory: StateFlow<List<String>> = _calcHistory.asStateFlow()

    fun onCalculatorAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> {
                val currentExpr = _calcExpression.value
                // If previous expression was evaluated or resulted in an error, reset on new number input
                if (_calcResult.value != "0" && currentExpr.isEmpty()) {
                    _calcResult.value = "0"
                }
                _calcExpression.value = currentExpr + action.value.toString()
            }
            CalculatorAction.Decimal -> {
                val currentExpr = _calcExpression.value
                if (currentExpr.isEmpty()) {
                    _calcExpression.value = "0."
                } else {
                    // Check if the current token already has a decimal
                    val lastToken = currentExpr.split(" ", "+", "-", "×", "÷").lastOrNull() ?: ""
                    if (!lastToken.contains(".")) {
                        _calcExpression.value = currentExpr + "."
                    }
                }
            }
            is CalculatorAction.Operation -> {
                val currentExpr = _calcExpression.value
                if (currentExpr.isNotEmpty()) {
                    val lastChar = currentExpr.trim().lastOrNull()
                    if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '÷') {
                        // Replace last operator
                        _calcExpression.value = currentExpr.dropLast(1) + action.op
                    } else {
                        _calcExpression.value = currentExpr + " " + action.op + " "
                    }
                } else {
                    // Use previous result as starting number
                    val prevResult = _calcResult.value
                    if (prevResult != "Error" && prevResult != "0") {
                        _calcExpression.value = prevResult + " " + action.op + " "
                    }
                }
            }
            CalculatorAction.Clear -> {
                _calcExpression.value = ""
                _calcResult.value = "0"
            }
            CalculatorAction.Delete -> {
                val currentExpr = _calcExpression.value
                if (currentExpr.isNotEmpty()) {
                    if (currentExpr.endsWith(" ")) {
                        _calcExpression.value = currentExpr.dropLast(3) // removes operator and spaces
                    } else {
                        _calcExpression.value = currentExpr.dropLast(1)
                    }
                }
            }
            CalculatorAction.Calculate -> {
                val expr = _calcExpression.value.trim()
                if (expr.isEmpty()) return
                try {
                    val result = evaluateInfix(expr)
                    val formattedResult = if (result % 1.0 == 0.0) {
                        result.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.2f", result)
                    }
                    _calcResult.value = formattedResult

                    // Log this cute calculation into receipt history
                    val emojiList = listOf("☕️ Coffee Blend", "🍰 Caramel Cake", "🥐 Butter Croissant", "🍵 Matcha Latte", "🍪 Cookie Pack")
                    val item = emojiList.random()
                    val newHistoryLine = "$item: $expr = $formattedResult"
                    _calcHistory.value = listOf(newHistoryLine) + _calcHistory.value

                    _calcExpression.value = "" // clear expression after calculation
                } catch (e: Exception) {
                    _calcResult.value = "Error"
                }
            }
        }
    }

    // --- Custom Mathematical Evaluator ---
    private fun evaluateInfix(expression: String): Double {
        val tokens = expression.replace("×", "*").replace("÷", "/").split("\\s+".toRegex())
        val values = Stack<Double>()
        val ops = Stack<Char>()

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token.isEmpty()) {
                i++
                continue
            }

            val firstChar = token[0]
            // If token is a number
            if (token.toDoubleOrNull() != null) {
                values.push(token.toDouble())
            } else if (token.length == 1 && (firstChar == '+' || firstChar == '-' || firstChar == '*' || firstChar == '/')) {
                while (!ops.empty() && hasPrecedence(firstChar, ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                }
                ops.push(firstChar)
            } else {
                throw IllegalArgumentException("Invalid token: $token")
            }
            i++
        }

        while (!ops.empty()) {
            if (values.size < 2) throw ArithmeticException("Malformed expression")
            values.push(applyOp(ops.pop(), values.pop(), values.pop()))
        }

        if (values.size != 1) throw ArithmeticException("Malformed expression")
        return values.pop()
    }

    private fun hasPrecedence(op1: Char, op2: Char): Boolean {
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false
        }
        return true
    }

    private fun applyOp(op: Char, b: Double, a: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> {
                if (b == 0.0) throw UnsupportedOperationException("Cannot divide by zero")
                a / b
            }
            else -> 0.0
        }
    }
}

// --- Factory for Creating the ViewModel ---
class CafeViewModelFactory(private val repository: CafeRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CafeViewModel::class.java)) {
            return CafeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
