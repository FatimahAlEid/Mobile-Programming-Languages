package com.example.brideline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val editPage = Color(0xFFECEDF3)
private val editRose = Color(0xFFD2647D)
private val editBlush = Color(0xFFEDBEC8)
private val editLightPink = Color(0xFFF3D6DC)
private val editInk = Color(0xFF45353A)
private val editGreen = Color(0xFF387953)
private val editLightGreen = Color(0xFFE5F3E8)
private val editRed = Color(0xFFAA303D)
private val editLightRed = Color(0xFFFCE8EA)

private val editFont = FontFamily(
    Font(R.font.archivo_semibold, FontWeight.SemiBold)
)

private data class EditCategory(
    val id: String,
    val name: String
)

private val editCategories = listOf(
    EditCategory("hair", "Hair Stylists"),
    EditCategory("makeup", "Makeup Artists"),
    EditCategory("venues", "Venues"),
    EditCategory("buffet", "Buffet"),
    EditCategory("dj", "DJ"),
    EditCategory("photo", "Photography")
)

private val amountPattern = Regex("[0-9]{0,12}(\\.[0-9]{0,2})?")

private fun inputAmount(amount: Double): String {
    if (amount == 0.0) return ""
    return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()
}

private fun String.amount(): BigDecimal =
    toBigDecimalOrNull() ?: BigDecimal.ZERO

private fun normalizeAmount(input: String): String = buildString {
    for (character in input) {
        when {
            character.isDigit() -> append(character.digitToInt())
            character == '٫' -> append('.')
            character == ',' || character == '٬' -> Unit
            else -> append(character)
        }
    }
}

@Composable
fun EditBudgetScreen(
    onBack: () -> Unit,
    onSave: (Double, List<BudgetCategoryUi>) -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
    roleLabel: String = "Bride Line.Groom",
    initialTotalBudget: Double = 0.0,
    initialCategories: List<BudgetCategoryUi> = emptyList()
) {
    val existing = remember(initialCategories) {
        initialCategories.associateBy { it.id }
    }

    val initialAmounts = remember(existing) {
        editCategories.map { category ->
            inputAmount(existing[category.id]?.allocatedAmount ?: 0.0)
        }
    }

    var totalText by rememberSaveable(initialTotalBudget) {
        mutableStateOf(inputAmount(initialTotalBudget))
    }

    var amounts by rememberSaveable(initialAmounts) {
        mutableStateOf(ArrayList(initialAmounts))
    }

    val totalBudget = totalText.amount()
    val totalAllocated = amounts.fold(BigDecimal.ZERO) { sum, value ->
        sum + value.amount()
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(editPage)
            .safeDrawingPadding()
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = editInk
                )
            }

            EditLabel("Budget & Payments", 22)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column {
                    EditLabel(roleLabel, 13, editRose)
                    EditLabel("Edit Budget", 26)
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .background(editBlush, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    EditLabel("Total Budget", 16)
                    Spacer(Modifier.height(8.dp))

                    EditAmountField(
                        value = totalText,
                        onValueChange = { totalText = it },
                        label = "Total budget in SAR",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(0.84f)
                    )
                }
            }

            item {
                EditLabel(
                    text = "Category Allocation",
                    size = 16,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                )
            }

            itemsIndexed(
                items = editCategories,
                key = { _, category -> category.id }
            ) { index, category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(editBlush, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        BudgetIcon(category.name, 32.dp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        EditLabel(category.name, 15)

                        EditAmountField(
                            value = amounts[index],
                            onValueChange = { newValue ->
                                amounts = ArrayList(amounts).apply {
                                    set(index, newValue)
                                }
                            },
                            label = "${category.name} allocation in SAR",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(editPage)
                .padding(top = 8.dp)
        ) {
            AllocationStatus(
                totalBudget = totalBudget,
                totalAllocated = totalAllocated
            )

            Button(
                onClick = {
                    focusManager.clearFocus()

                    val updatedCategories = editCategories.mapIndexed { index, category ->
                        BudgetCategoryUi(
                            id = category.id,
                            category = category.name,
                            allocatedAmount = amounts[index].amount().toDouble(),
                            spentAmount = existing[category.id]?.spentAmount ?: 0.0
                        )
                    }

                    onSave(totalBudget.toDouble(), updatedCategories)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = editRose)
            ) {
                EditLabel("Save Changes", 16, Color.White)
            }

            BridelineBottomBar(
                selectedTab = BridelineTab.Budget,
                onTabSelected = { tab ->
                    when (tab) {
                        BridelineTab.Home -> onHome()
                        BridelineTab.Budget -> Unit
                        BridelineTab.Profile -> onProfile()
                    }
                }
            )
        }
    }
}

@Composable
private fun AllocationStatus(
    totalBudget: BigDecimal,
    totalAllocated: BigDecimal
) {
    val formatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
        }
    }

    val excessAmount = totalAllocated - totalBudget
    val isOverBudget = excessAmount.signum() > 0
    val textColor = if (isOverBudget) editRed else editGreen
    val backgroundColor = if (isOverBudget) editLightRed else editLightGreen

    AnimatedVisibility(visible = totalAllocated.signum() > 0) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(backgroundColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isOverBudget) {
                    Icons.Outlined.Warning
                } else {
                    Icons.Outlined.CheckCircle
                },
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )

            EditLabel(
                text = if (isOverBudget) {
                    "Budget exceeded by SAR ${formatter.format(excessAmount)}"
                } else {
                    "Total allocated: SAR ${formatter.format(totalAllocated)}"
                },
                size = 14,
                color = textColor
            )
        }
    }
}

@Composable
private fun EditLabel(
    text: String,
    size: Int,
    color: Color = editInk,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = editFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = size.sp,
        lineHeight = (size + 4).sp
    )
}

@Composable
private fun EditAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    BasicTextField(
        value = value,
        onValueChange = { input ->
            val normalized = normalizeAmount(input)
            if (amountPattern.matches(normalized)) {
                onValueChange(normalized)
            }
        },
        modifier = modifier
            .clip(shape)
            .background(Color.White)
            .border(1.dp, editBlush, shape)
            .semantics { contentDescription = label },
        singleLine = true,
        textStyle = TextStyle(
            color = editInk,
            fontFamily = editFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        ),
        cursorBrush = SolidColor(editRose),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(editLightPink)
                        .heightIn(min = 38.dp)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EditLabel("SAR", 14)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 38.dp)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        EditLabel("0", 15, Color(0xFF71676B))
                    }

                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun EditBudgetPreviewFrame(
    initialTotalBudget: Double = 0.0,
    initialCategories: List<BudgetCategoryUi> = emptyList()
) {
    val frameShape = RoundedCornerShape(56.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(10.dp)
            .border(
                width = 2.dp,
                color = Color(0xFFC95772).copy(alpha = 0.7f),
                shape = frameShape
            )
            .clip(frameShape)
    ) {
        EditBudgetScreen(
            initialTotalBudget = initialTotalBudget,
            initialCategories = initialCategories,
            onBack = {},
            onSave = { _, _ -> },
            onHome = {},
            onProfile = {}
        )
    }
}

@Preview(
    name = "1 - Zero",
    showBackground = true,
    widthDp = 402,
    heightDp = 760
)
@Composable
private fun EditBudgetZeroPreview() {
    EditBudgetPreviewFrame()
}

@Preview(
    name = "2 - Within Budget",
    showBackground = true,
    widthDp = 402,
    heightDp = 760
)
@Composable
private fun EditBudgetAllocatedPreview() {
    EditBudgetPreviewFrame(
        initialTotalBudget = 18000.0,
        initialCategories = listOf(
            BudgetCategoryUi("hair", "Hair Stylists", 2000.0, 0.0),
            BudgetCategoryUi("makeup", "Makeup Artists", 2500.0, 0.0),
            BudgetCategoryUi("venues", "Venues", 3000.0, 0.0),
            BudgetCategoryUi("buffet", "Buffet", 1000.0, 0.0)
        )
    )
}

@Preview(
    name = "3 - Budget Exceeded",
    showBackground = true,
    widthDp = 402,
    heightDp = 760
)
@Composable
private fun EditBudgetExceededPreview() {
    EditBudgetPreviewFrame(
        initialTotalBudget = 1000.0,
        initialCategories = listOf(
            BudgetCategoryUi("hair", "Hair Stylists", 655.0, 0.0),
            BudgetCategoryUi("makeup", "Makeup Artists", 500.0, 0.0),
            BudgetCategoryUi("venues", "Venues", 888.0, 0.0)
        )
    )
}