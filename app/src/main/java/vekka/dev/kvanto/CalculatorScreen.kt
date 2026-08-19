package vekka.dev.kvanto

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import vekka.dev.kvanto.ui.theme.CalcGreen
import vekka.dev.kvanto.ui.theme.CalcRed

@Composable
fun LandscapeButton(
    label: String,
    isOperator: Boolean = false,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
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
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(54.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = label, fontSize = 18.sp)
    }
}

@Composable
fun CalculatorScreen(
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    var expression by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }
    var justCalculated by rememberSaveable { mutableStateOf(false) }

    val historySaver = listSaver<List<HistoryItem>, String>(
        save = { list -> list.flatMap { listOf(it.expression, it.result) } },
        restore = { list -> list.chunked(2).map { HistoryItem(it[0], it[1]) } }
    )
    var history by rememberSaveable(stateSaver = historySaver) { mutableStateOf(listOf()) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showOptions by rememberSaveable { mutableStateOf(false) }
    var isHapticEnabled by rememberSaveable { mutableStateOf(false) }
    
    val errorText = stringResource(R.string.error_text)
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val keyboardBg = if (isDarkTheme) Color(0xFF3E4044) else Color(0xFFDFE1E5)
    val buttonColor = if (isDarkTheme) Color(0xFF2D2E32) else Color(0xFFF1F3F4)
    val toggleActiveColor = if (isDarkTheme) Color(0xFF4A4D51) else Color(0xFFE0E0E0)
    val operatorColor = if (isDarkTheme) CalcRed else CalcGreen
    
    val haptic = LocalHapticFeedback.current
    val uriHandler = LocalUriHandler.current

    fun triggerHaptic() {
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val annotatedExpression = remember(expression, isDarkTheme) {
        buildAnnotatedString {
            expression.forEach { char ->
                if (char in "+-×÷") {
                    withStyle(style = SpanStyle(color = operatorColor)) {
                        append(char)
                    }
                } else {
                    append(char)
                }
            }
        }
    }

    fun updateResult() {
        var cleanExpression = expression
        if (cleanExpression.isNotEmpty() && !cleanExpression.last().isDigit()) {
            cleanExpression = cleanExpression.dropLast(1)
        }
        
        val hasActualOperation = cleanExpression.any { it in listOf('+', '-', '×', '÷') }
        
        if (!hasActualOperation || cleanExpression.isEmpty()) {
            result = ""
            return
        }
        try {
            val calculated = evaluate(cleanExpression)
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
        triggerHaptic()
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
                updateResult()
            }
            "+/-" -> {
                if (expression.isEmpty()) return
                val tokens = expression.split("+", "-", "×", "÷")
                if (tokens.isEmpty()) return
                val lastToken = tokens.last()
                if (lastToken.isEmpty()) return
                
                val newLastToken = if (lastToken.startsWith("(-")) {
                    lastToken.substring(2, lastToken.length - 1)
                } else {
                    "(-$lastToken)"
                }
                expression = expression.substring(0, expression.length - lastToken.length) + newLastToken
                updateResult()
            }
            "%" -> {
                if (expression.isEmpty()) return
                val tokens = expression.split("+", "-", "×", "÷")
                if (tokens.isEmpty()) return
                val lastToken = tokens.last()
                if (lastToken.isEmpty()) return
                
                try {
                    val value = lastToken.toDouble() / 100.0
                    val newValue = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
                    expression = expression.substring(0, expression.length - lastToken.length) + newValue
                    updateResult()
                } catch (e: Exception) {}
            }
            "=" -> {
                if (expression.isEmpty()) return
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
                    expression = errorText
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
        if (!showHistory && !showOptions) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
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
                            color = if (!isDarkTheme) toggleActiveColor
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
                        contentDescription = stringResource(R.string.light_theme),
                        tint = if (!isDarkTheme) CalcGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isDarkTheme) toggleActiveColor
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
                        contentDescription = stringResource(R.string.dark_theme),
                        tint = if (isDarkTheme) CalcRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (!showHistory && !showOptions) {
            if (isLandscape) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 88.dp)
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = annotatedExpression,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            fontSize = 32.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            softWrap = false
                        )
                        Text(
                            text = result,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            maxLines = 1,
                            fontSize = 24.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            softWrap = false
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = keyboardBg,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LandscapeButton("7", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("7") }
                            LandscapeButton("8", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("8") }
                            LandscapeButton("9", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("9") }
                            LandscapeButton("AC", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("AC") }
                            LandscapeButton("×", isOperator = true, modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("×") }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LandscapeButton("4", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("4") }
                            LandscapeButton("5", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("5") }
                            LandscapeButton("6", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("6") }
                            LandscapeButton("+/-", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("+/-") }
                            LandscapeButton("-", isOperator = true, modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("-") }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LandscapeButton("1", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("1") }
                            LandscapeButton("2", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("2") }
                            LandscapeButton("3", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("3") }
                            LandscapeButton("%", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("%") }
                            LandscapeButton("+", isOperator = true, modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("+") }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LandscapeButton("0", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("0") }
                            LandscapeButton(".", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick(".") }
                            LandscapeButton("⌫", modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("⌫") }
                            LandscapeButton("÷", isOperator = true, modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("÷") }
                            LandscapeButton("=", isOperator = true, modifier = Modifier.weight(1f), containerColor = buttonColor) { onButtonClick("=") }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .windowInsetsPadding(WindowInsets.statusBars),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = annotatedExpression,
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
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = keyboardBg,
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                            )
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                            CalculatorButton("AC", containerColor = buttonColor) { onButtonClick("AC") }
                            CalculatorButton("+/-", containerColor = buttonColor) { onButtonClick("+/-") }
                            CalculatorButton("%", containerColor = buttonColor) { onButtonClick("%") }
                            CalculatorButton("÷", isOperator = true, containerColor = buttonColor) { onButtonClick("÷") }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                            CalculatorButton("7", containerColor = buttonColor) { onButtonClick("7") }
                            CalculatorButton("8", containerColor = buttonColor) { onButtonClick("8") }
                            CalculatorButton("9", containerColor = buttonColor) { onButtonClick("9") }
                            CalculatorButton("×", isOperator = true, containerColor = buttonColor) { onButtonClick("×") }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                            CalculatorButton("4", containerColor = buttonColor) { onButtonClick("4") }
                            CalculatorButton("5", containerColor = buttonColor) { onButtonClick("5") }
                            CalculatorButton("6", containerColor = buttonColor) { onButtonClick("6") }
                            CalculatorButton("-", isOperator = true, containerColor = buttonColor) { onButtonClick("-") }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                            CalculatorButton("1", containerColor = buttonColor) { onButtonClick("1") }
                            CalculatorButton("2", containerColor = buttonColor) { onButtonClick("2") }
                            CalculatorButton("3", containerColor = buttonColor) { onButtonClick("3") }
                            CalculatorButton("+", isOperator = true, containerColor = buttonColor) { onButtonClick("+") }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                            CalculatorButton("0", containerColor = buttonColor) { onButtonClick("0") }
                            CalculatorButton(".", containerColor = buttonColor) { onButtonClick(".") }
                            CalculatorButton("⌫", containerColor = buttonColor) { onButtonClick("⌫") }
                            CalculatorButton("=", isOperator = true, containerColor = buttonColor) { onButtonClick("=") }
                        }
                    }
                }
            }
        }

        if (showHistory) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 72.dp)
                    .padding(horizontal = if (isLandscape) 120.dp else 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.history_title),
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { history = emptyList() }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.clear_history),
                                tint = CalcRed
                            )
                        }
                    }
                }
                
                if (history.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_history),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                } else {
                    history.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    expression = item.result
                                    showHistory = false
                                    updateResult()
                                }
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

        if (showOptions) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 72.dp)
                    .padding(horizontal = if (isLandscape) 120.dp else 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.vibration_label),
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.vibration_desc),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isHapticEnabled,
                        onCheckedChange = { isHapticEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onBackground,
                            checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 32.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )

                Text(
                    text = stringResource(R.string.about_title),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.about_app_name))
                        }
                        append(stringResource(R.string.about_desc))
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { uriHandler.openUri("https://github.com/vekkadev") },
                        modifier = Modifier.background(
                            color = toggleActiveColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_github),
                            contentDescription = stringResource(R.string.github_cd),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = { uriHandler.openUri("https://x.com/vekka_dev") },
                        modifier = Modifier.background(
                            color = toggleActiveColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_twitter),
                            contentDescription = stringResource(R.string.twitter_cd),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = { 
                if (showHistory) showHistory = false
                showOptions = !showOptions 
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (showOptions) Icons.AutoMirrored.Rounded.ArrowBack else Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.options_cd),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = { 
                if (showOptions) showOptions = false
                showHistory = !showHistory 
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (showHistory) Icons.Rounded.Close else Icons.Rounded.History,
                contentDescription = stringResource(R.string.history_cd),
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