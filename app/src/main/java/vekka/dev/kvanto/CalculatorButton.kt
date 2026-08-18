package vekka.dev.kvanto

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vekka.dev.kvanto.ui.theme.CalcGreen
import vekka.dev.kvanto.ui.theme.CalcRed

@Composable
fun CalculatorButton(
    label: String,
    isOperator: Boolean = false,
    size: Dp = 88.dp,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit
) {
    val contentColor = when (label) {
        "AC", "+/-", "%" -> CalcGreen
        "÷", "×", "-", "+", "=", "⌫" -> CalcRed
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "." -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.size(size),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = label, fontSize = if (size < 80.dp) 16.sp else 20.sp)
    }
}
