package vekka.dev.kvanto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen(
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var justCalculated by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(listOf<HistoryItem>()) }
    var showHistory by remember { mutableStateOf(false) }

    fun updateResult() {
        val hasOperator = expression.any { it in listOf('+', '-', '×', '÷') }
        if (!hasOperator) {
            result = ""
            return
        }
        try {
            val calculated = evaluate(expression)
            result = if (calculated % 1.0 == 0.0) {
                calculated.toLong().toString()
            } else {
                calculated.toString()
            }
        } catch (e: Exception) {
            result = ""
        }
    }

    fun onButtonClick(label: String) {
        when (label) {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                if (expression.isEmpty() || justCalculated) {
                    expression = label
                    justCalculated = false
                } else {
                    expression += label
                }
                updateResult()
            }
            "." -> {
                val lastNumber = expression.split("+", "-", "×", "÷").last()
                if (!lastNumber.contains(".")) {
                    expression += "."
                }
            }
            "⌫" -> {
                expression = if (expression.length > 1) {
                    expression.dropLast(1)
                } else {
                    ""
                }
                justCalculated = false
                updateResult()
            }
            "AC" -> {
                expression = ""
                result = ""
                justCalculated = false
            }
            "+", "-", "×", "÷" -> {
                if (justCalculated) justCalculated = false
                if (expression.isNotEmpty() && !expression.last().isDigit()) {
                    expression = expression.dropLast(1)
                }
                expression += label
                result = ""
            }
            "=" -> {
                try {
                    val calculated = evaluate(expression)
                    val resultStr = if (calculated % 1.0 == 0.0) {
                        calculated.toLong().toString()
                    } else {
                        calculated.toString()
                    }
                    history = listOf(HistoryItem(expression, resultStr)) + history
                    expression = resultStr
                    result = ""
                    justCalculated = true
                } catch (e: Exception) {
                    expression = "Error"
                    justCalculated = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toggle de tema
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(50.dp)
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (!isDarkTheme) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { if (isDarkTheme) onToggleTheme() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LightMode,
                    contentDescription = "Tema claro",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        color = if (isDarkTheme) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { if (!isDarkTheme) onToggleTheme() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DarkMode,
                    contentDescription = "Tema oscuro",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Calculadora principal
        if (!showHistory) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = expression,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    fontSize = when {
                        expression.length > 12 -> 32.sp
                        expression.length > 9 -> 42.sp
                        expression.length > 6 -> 52.sp
                        else -> 64.sp
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    textAlign = TextAlign.End,
                    softWrap = false
                )
                Text(
                    text = result,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 1,
                    fontSize = 36.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.End,
                    softWrap = false
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    CalculatorButton("AC") { onButtonClick("AC") }
                    CalculatorButton("+/-") { onButtonClick("+/-") }
                    CalculatorButton("%") { onButtonClick("%") }
                    CalculatorButton("÷", isOperator = true) { onButtonClick("÷") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    CalculatorButton("7") { onButtonClick("7") }
                    CalculatorButton("8") { onButtonClick("8") }
                    CalculatorButton("9") { onButtonClick("9") }
                    CalculatorButton("×", isOperator = true) { onButtonClick("×") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    CalculatorButton("4") { onButtonClick("4") }
                    CalculatorButton("5") { onButtonClick("5") }
                    CalculatorButton("6") { onButtonClick("6") }
                    CalculatorButton("-", isOperator = true) { onButtonClick("-") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    CalculatorButton("1") { onButtonClick("1") }
                    CalculatorButton("2") { onButtonClick("2") }
                    CalculatorButton("3") { onButtonClick("3") }
                    CalculatorButton("+", isOperator = true) { onButtonClick("+") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    CalculatorButton("0") { onButtonClick("0") }
                    CalculatorButton(".") { onButtonClick(".") }
                    CalculatorButton("⌫") { onButtonClick("⌫") }
                    CalculatorButton("=", isOperator = true) { onButtonClick("=") }
                }
            }
        }

        // Panel de historial
        if (showHistory) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 72.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Historial",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (history.isEmpty()) {
                    Text(
                        text = "No hay operaciones todavía",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                } else {
                    history.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = item.expression,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "= ${item.result}",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 24.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Boton de historial arriba a la derecha - siempre visible
        IconButton(
            onClick = { showHistory = !showHistory },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (showHistory) Icons.Rounded.Close else Icons.Rounded.History,
                contentDescription = "Historial",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

fun evaluate(expression: String): Double {
    val tokens = mutableListOf<String>()
    var current = ""
    for (char in expression) {
        if (char in listOf('+', '×', '÷') || (char == '-' && current.isNotEmpty())) {
            if (current.isNotEmpty()) tokens.add(current)
            tokens.add(char.toString())
            current = ""
        } else {
            current += char
        }
    }
    if (current.isNotEmpty()) tokens.add(current)

    var result = tokens[0].toDouble()
    var i = 1
    while (i < tokens.size - 1) {
        val op = tokens[i]
        val next = tokens[i + 1].toDouble()
        result = when (op) {
            "+" -> result + next
            "-" -> result - next
            "×" -> result * next
            "÷" -> if (next != 0.0) result / next else Double.NaN
            else -> result
        }
        i += 2
    }
    return result
}