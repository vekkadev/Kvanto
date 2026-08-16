package vekka.dev.kvanto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    var displayValue by remember { mutableStateOf("0") }
    var firstNumber by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("") }
    var newNumber by remember { mutableStateOf(true) }

    fun onButtonClick(label: String) {
        when (label) {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                if (newNumber) {
                    displayValue = label
                    newNumber = false
                } else {
                    displayValue += label
                }
            }
            "." -> {
                if (!displayValue.contains(".")) {
                    displayValue += "."
                }
            }
            "⌫" -> {
                displayValue = if (displayValue.length > 1) {
                    displayValue.dropLast(1)
                } else {
                    "0"
                }
            }
            "AC" -> {
                displayValue = "0"
                firstNumber = ""
                operator = ""
                newNumber = true
            }
            "+", "-", "×", "÷" -> {
                firstNumber = displayValue
                operator = label
                newNumber = true
            }
            "=" -> {
                if (operator.isNotEmpty() && firstNumber.isNotEmpty()) {
                    val a = firstNumber.toDouble()
                    val b = displayValue.toDouble()
                    val result = when (operator) {
                        "+" -> a + b
                        "-" -> a - b
                        "×" -> a * b
                        "÷" -> if (b != 0.0) a / b else Double.NaN
                        else -> b
                    }
                    displayValue = if (result % 1.0 == 0.0) {
                        result.toLong().toString()
                    } else {
                        result.toString()
                    }
                    firstNumber = ""
                    operator = ""
                    newNumber = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    .clickable { if (isDarkTheme) onToggleTheme() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "☀️", fontSize = 18.sp)
            }
            Box(
                modifier = Modifier
                    .background(
                        color = if (isDarkTheme) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { if (!isDarkTheme) onToggleTheme() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌙", fontSize = 18.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (operator.isNotEmpty()) "$firstNumber $operator" else "",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.End
            )
            Text(
                text = displayValue,
                fontSize = 64.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.End
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CalculatorButton("AC") { onButtonClick("AC") }
                CalculatorButton("+/-") { onButtonClick("+/-") }
                CalculatorButton("%") { onButtonClick("%") }
                CalculatorButton("÷", isOperator = true) { onButtonClick("÷") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CalculatorButton("7") { onButtonClick("7") }
                CalculatorButton("8") { onButtonClick("8") }
                CalculatorButton("9") { onButtonClick("9") }
                CalculatorButton("×", isOperator = true) { onButtonClick("×") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CalculatorButton("4") { onButtonClick("4") }
                CalculatorButton("5") { onButtonClick("5") }
                CalculatorButton("6") { onButtonClick("6") }
                CalculatorButton("-", isOperator = true) { onButtonClick("-") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CalculatorButton("1") { onButtonClick("1") }
                CalculatorButton("2") { onButtonClick("2") }
                CalculatorButton("3") { onButtonClick("3") }
                CalculatorButton("+", isOperator = true) { onButtonClick("+") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CalculatorButton("0") { onButtonClick("0") }
                CalculatorButton(".") { onButtonClick(".") }
                CalculatorButton("⌫") { onButtonClick("⌫") }
                CalculatorButton("=", isOperator = true) { onButtonClick("=") }
            }
        }
    }
}