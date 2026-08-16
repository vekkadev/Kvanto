package vekka.dev.kvanto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vekka.dev.kvanto.ui.theme.KvantoTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KvantoTheme {
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(80.dp)
    ) {
        Text(text = label, fontSize = 20.sp)
    }
}

@Composable
fun CalculatorScreen() {
    // Estado de la calculadora
    var displayValue by remember { mutableStateOf("0") }
    var firstNumber by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("") }
    var newNumber by remember { mutableStateOf(true) }

    // Logica de los botones
    fun onButtonClick(label: String) {
        when (label) {
            // Numeros
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                if (newNumber) {
                    displayValue = label
                    newNumber = false
                } else {
                    displayValue += label
                }
            }
            // Decimal
            "." -> {
                if (!displayValue.contains(".")) {
                    displayValue += "."
                }
            }
            // Borrar
            "⌫" -> {
                displayValue = if (displayValue.length > 1) {
                    displayValue.dropLast(1)
                } else {
                    "0"
                }
            }
            // Limpiar
            "AC" -> {
                displayValue = "0"
                firstNumber = ""
                operator = ""
                newNumber = true
            }
            // Operadores
            "+", "-", "×", "÷" -> {
                firstNumber = displayValue
                operator = label
                newNumber = true
            }
            // Igual
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Pantalla
// Operacion en curso
        Text(
            text = if (operator.isNotEmpty()) "$firstNumber $operator" else "",
            fontSize = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.End
        )

// Numero principal
        Text(
            text = displayValue,
            fontSize = 64.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.End
        )

        // Fila 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalculatorButton("AC") { onButtonClick("AC") }
            CalculatorButton("+/-") { onButtonClick("+/-") }
            CalculatorButton("%") { onButtonClick("%") }
            CalculatorButton("÷") { onButtonClick("÷") }
        }

        // Fila 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalculatorButton("7") { onButtonClick("7") }
            CalculatorButton("8") { onButtonClick("8") }
            CalculatorButton("9") { onButtonClick("9") }
            CalculatorButton("×") { onButtonClick("×") }
        }

        // Fila 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalculatorButton("4") { onButtonClick("4") }
            CalculatorButton("5") { onButtonClick("5") }
            CalculatorButton("6") { onButtonClick("6") }
            CalculatorButton("-") { onButtonClick("-") }
        }

        // Fila 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalculatorButton("1") { onButtonClick("1") }
            CalculatorButton("2") { onButtonClick("2") }
            CalculatorButton("3") { onButtonClick("3") }
            CalculatorButton("+") { onButtonClick("+") }
        }

        // Fila 5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalculatorButton("0") { onButtonClick("0") }
            CalculatorButton(".") { onButtonClick(".") }
            CalculatorButton("⌫") { onButtonClick("⌫") }
            CalculatorButton("=") { onButtonClick("=") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    KvantoTheme {
        CalculatorScreen()
    }
}