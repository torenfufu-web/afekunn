package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.ChalkboardBorder
import com.example.ui.theme.ChalkboardGreen
import com.example.ui.theme.ChalkboardGreenLight
import com.example.ui.theme.ChalkboardText
import com.example.viewmodel.CafeViewModel
import com.example.viewmodel.CalculatorAction

@Composable
fun CalculatorScreen(
    viewModel: CafeViewModel,
    modifier: Modifier = Modifier
) {
    val expr by viewModel.calcExpression.collectAsStateWithLifecycle()
    val result by viewModel.calcResult.collectAsStateWithLifecycle()
    val history by viewModel.calcHistory.collectAsStateWithLifecycle()
    val historyListState = rememberLazyListState()

    // Automatically scroll to latest calculation in receipt
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            historyListState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Wooden Board Frame containing the Chalkboard Screen
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp))
                .border(8.dp, ChalkboardBorder, RoundedCornerShape(16.dp)) // Cozy Wood Frame
                .clip(RoundedCornerShape(16.dp))
                .background(ChalkboardGreen) // Chalkboard Slate
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Calculation History (Log styled like a coffee shop register receipt)
                Text(
                    text = "🗒️ RECEIPT LOG",
                    style = MaterialTheme.typography.bodySmall,
                    color = ChalkboardText.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                LazyColumn(
                    state = historyListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = ChalkboardText.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(ChalkboardGreenLight.copy(alpha = 0.2f))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (history.isEmpty()) {
                        item {
                            Text(
                                text = "まだお会計がありません（ログがここに表示されます）",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChalkboardText.copy(alpha = 0.4f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        items(history) { record ->
                            Text(
                                text = record,
                                style = MaterialTheme.typography.bodySmall,
                                color = ChalkboardText.copy(alpha = 0.85f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current Expression Input Line
                Text(
                    text = expr.ifEmpty { " " },
                    style = MaterialTheme.typography.titleLarge,
                    color = ChalkboardText,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calculator_expression_display"),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Calculated Result Output Line
                Text(
                    text = if (result == "Error") "Error" else "= $result",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (result == "Error") Color(0xFFE57373) else ChalkboardText,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calculator_result_display"),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 32.sp
                )
            }
        }

        // Chalkboard Keyboard Grid (4 columns)
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: C, Delete/Backspace, Division
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChalkButton(
                        text = "C",
                        color = Color(0xFFC62828).copy(alpha = 0.2f),
                        textColor = Color(0xFFFF8A80),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_key_clear")
                    ) {
                        viewModel.onCalculatorAction(CalculatorAction.Clear)
                    }

                    ChalkButton(
                        icon = true,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        textColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_key_delete")
                    ) {
                        viewModel.onCalculatorAction(CalculatorAction.Delete)
                    }

                    ChalkButton(
                        text = "÷",
                        color = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_key_op_divide")
                    ) {
                        viewModel.onCalculatorAction(CalculatorAction.Operation("÷"))
                    }
                }

                // Row 2: 7, 8, 9, Multiply
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7, 8, 9).forEach { num ->
                        ChalkButton(
                            text = num.toString(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_key_$num")
                        ) {
                            viewModel.onCalculatorAction(CalculatorAction.Number(num))
                        }
                    }

                    ChalkButton(
                        text = "×",
                        color = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_key_op_multiply")
                    ) {
                        viewModel.onCalculatorAction(CalculatorAction.Operation("×"))
                    }
                }

                // Row 3: 4, 5, 6, Subtract
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(4, 5, 6).forEach { num ->
                        ChalkButton(
                            text = num.toString(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_key_$num")
                        ) {
                            viewModel.onCalculatorAction(CalculatorAction.Number(num))
                        }
                    }

                    ChalkButton(
                        text = "-",
                        color = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_key_op_subtract")
                    ) {
                        viewModel.onCalculatorAction(CalculatorAction.Operation("-"))
                    }
                }

                // Row 4: 1, 2, 3, Add
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3).forEach { num ->
                        ChalkButton(
                            text = num.toString(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_key_$num")
                        ) {
                            viewModel.onCalculatorAction(CalculatorAction.Number(num))
                        }
                    }

                    ChalkButton(
                        text = "+",
                        color = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_key_op_add")
                    ) {
                        viewModel.onCalculatorAction(CalculatorAction.Operation("+"))
                    }
                }

                // Row 5: 0, Decimal, Calculate (=)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChalkButton(
                        text = "0",
                        modifier = Modifier
                            .weight(2f)
                            .testTag("calc_key_0")
                    ) {
                        viewModel.onCalculatorAction(CalculatorAction.Number(0))
                    }

                    ChalkButton(
                        text = ".",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_key_decimal")
                    ) {
                        viewModel.onCalculatorAction(CalculatorAction.Decimal)
                    }

                    ChalkButton(
                        text = "=",
                        color = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_key_calculate")
                    ) {
                        viewModel.onCalculatorAction(CalculatorAction.Calculate)
                    }
                }
            }
        }
    }
}

@Composable
fun ChalkButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: Boolean = false,
    color: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bouncy physical scale feedback on touch!
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = tween(100),
        label = "clickScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (icon) {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Backspace",
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 20.sp
            )
        }
    }
}
