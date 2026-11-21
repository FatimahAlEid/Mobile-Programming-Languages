package com.example.brideline

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
import java.text.NumberFormat
import java.util.Locale

data class BudgetCategoryUi(
    val id: String,
    val category: String,
    val allocatedAmount: Double,
    val spentAmount: Double
)

private val ArchivoSemiBold = FontFamily(
    Font(R.font.archivo_semibold, FontWeight.SemiBold)
)

private val BudgetRose = Color(0xFFD2647D)
private val BudgetBlush = Color(0xFFEDBEC8)
private val BudgetInk = Color(0xFF30272A)
private val BudgetMuted = Color(0xFF71676B)
private val BudgetGreen = Color(0xFF42634E)
private val BudgetPage = Color(0xFFF4F2F2)
private val BudgetTrack = Color(0xFFF3DEE3)
private val BudgetCardShape = RoundedCornerShape(16.dp)

@Composable
private fun BudgetText(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    color: Color = BudgetInk,
    centered: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = ArchivoSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = size.sp,
        lineHeight = (size + 4).sp,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start
    )
}

private fun sar(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 2
    return "SAR ${formatter.format(amount)}"
}

private fun usedRatio(spent: Double, allocated: Double): Double =
    if (allocated > 0.0) spent / allocated else 0.0

private fun percentage(ratio: Double): String {
    val formatter = NumberFormat.getPercentInstance(Locale.US)
    formatter.maximumFractionDigits = 1
    return formatter.format(ratio)
}

@Composable
fun BudgetOverviewRoute(
    weddingId: String,
    totalBudget: Double,
    spentAmount: Double,
    categories: List<BudgetCategoryUi>,
    roleLabel: String,
    onBack: () -> Unit,
    onAllocate: () -> Unit,
    onEdit: () -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    onAddPayment: (String) -> Unit,
    onViewInvoice: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryId by rememberSaveable(weddingId) {
        mutableStateOf<String?>(null)
    }

    val categoryId = selectedCategoryId
    val returnToOverview = { selectedCategoryId = null }

    BackHandler(
        enabled = categoryId != null,
        onBack = returnToOverview
    )

    if (categoryId == null) {
        BudgetOverviewScreen(
            totalBudget = totalBudget,
            spentAmount = spentAmount,
            categories = categories,
            roleLabel = roleLabel,
            onBack = onBack,
            onAllocate = onAllocate,
            onEdit = onEdit,
            onCategoryClick = { selectedCategoryId = it },
            onHome = onHome,
            onProfile = onProfile,
            modifier = modifier
        )
    } else {
        BudgetCategoryDetailsScreen(
            weddingId = weddingId,
            categoryId = categoryId,
            roleLabel = roleLabel,
            onBack = returnToOverview,
            onHome = onHome,
            onBudget = returnToOverview,
            onProfile = onProfile,
            onAddPayment = onAddPayment,
            onViewInvoice = onViewInvoice,
            modifier = modifier
        )
    }
}

@Composable
fun BudgetOverviewScreen(
    totalBudget: Double,
    spentAmount: Double,
    categories: List<BudgetCategoryUi>,
    roleLabel: String,
    onBack: () -> Unit,
    onAllocate: () -> Unit,
    onEdit: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryListState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BudgetPage,
        bottomBar = {
            BudgetBottomBar(
                onHome = onHome,
                onProfile = onProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            BudgetTopBar(
                showEdit = categories.isNotEmpty(),
                onBack = onBack,
                onEdit = onEdit
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val scrollHeader =
                    maxHeight < 420.dp || LocalDensity.current.fontScale > 1.4f

                Column(Modifier.fillMaxSize()) {
                    if (!scrollHeader) {
                        BudgetHeader(roleLabel, totalBudget, spentAmount)
                    }

                    LazyColumn(
                        state = categoryListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (scrollHeader) {
                            item {
                                BudgetHeader(roleLabel, totalBudget, spentAmount)
                            }
                        }

                        if (categories.isEmpty()) {
                            item {
                                EmptyBudgetCard(onAllocate)
                            }
                        } else {
                            items(
                                items = categories,
                                key = { category -> category.id }
                            ) { category ->
                                CategoryBudgetCard(
                                    item = category,
                                    onClick = { onCategoryClick(category.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetTopBar(
    showEdit: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit
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
            BudgetIcon("back", 24.dp, description = "Back")
        }

        BudgetText(
            text = "Budget & Payments",
            size = 22,
            modifier = Modifier.padding(horizontal = 48.dp),
            centered = true
        )

        if (showEdit) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                BudgetIcon("edit", 22.dp, BudgetRose, "Edit category budgets")
            }
        }
    }
}

@Composable
private fun BudgetHeader(
    roleLabel: String,
    total: Double,
    spent: Double
) {
    val ratio = usedRatio(spent, total)
    val remaining = total - spent

    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        BudgetText(roleLabel, 16, color = BudgetRose)

        Spacer(Modifier.height(4.dp))
        BudgetText("Budget overview", 28)

        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = BudgetCardShape,
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BudgetText("Total Budget", 16, color = BudgetMuted)
                        BudgetText(sar(total), 30)
                        BudgetAmountLine("Spent", spent, BudgetRose)

                        BudgetAmountLine(
                            label = "Remaining",
                            amount = remaining,
                            color = if (remaining >= 0.0) BudgetGreen else BudgetRose
                        )
                    }

                    Box(
                        modifier = Modifier.size(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val strokeWidth = 9.dp.toPx()

                            drawCircle(
                                color = BudgetRose,
                                radius = (size.minDimension - strokeWidth) / 2f,
                                style = Stroke(width = strokeWidth)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            BudgetText(percentage(ratio), 18, color = BudgetRose)
                            BudgetText("Used", 12, color = BudgetMuted)
                        }
                    }
                }

                BudgetProgressBar(ratio, height = 7.dp)
            }
        }

        BudgetText(
            text = "Categories",
            size = 18,
            modifier = Modifier.padding(
                start = 16.dp,
                top = 20.dp,
                bottom = 12.dp
            )
        )
    }
}

@Composable
private fun BudgetAmountLine(
    label: String,
    amount: Double,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        BudgetText("$label:", 14, color = BudgetMuted)
        BudgetText(sar(amount), 14, color = color)
    }
}

@Composable
private fun BudgetProgressBar(
    ratio: Double,
    height: Dp = 5.dp
) {
    val fraction = ratio.toFloat().coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
            }
            .clip(CircleShape)
            .background(BudgetTrack)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(CircleShape)
                .background(BudgetRose)
        )
    }
}

@Composable
private fun EmptyBudgetCard(onAllocate: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = BudgetCardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(BudgetBlush.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                BudgetIcon("wallet", 76.dp)
            }

            Spacer(Modifier.height(16.dp))

            BudgetText(
                text = "Set your category budgets",
                size = 18,
                centered = true
            )

            Spacer(Modifier.height(4.dp))

            BudgetText(
                text = "Choose how much to set aside for each category",
                size = 13,
                modifier = Modifier.fillMaxWidth(),
                color = BudgetMuted,
                centered = true
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAllocate,
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .heightIn(min = 40.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 6.dp
                ),
                colors = ButtonDefaults.buttonColors(containerColor = BudgetRose)
            ) {
                BudgetText("Allocate budget", 16, color = Color.White)
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    item: BudgetCategoryUi,
    onClick: () -> Unit
) {
    val ratio = usedRatio(item.spentAmount, item.allocatedAmount)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = BudgetCardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(BudgetBlush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                BudgetIcon(item.category, 32.dp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BudgetText(item.category, 16, Modifier.weight(1f))
                    BudgetText(sar(item.spentAmount), 16)
                    BudgetIcon("next", 18.dp, BudgetRose)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BudgetText(
                        text = "Allocated: ${sar(item.allocatedAmount)}",
                        size = 14,
                        modifier = Modifier.weight(1f),
                        color = BudgetMuted
                    )

                    BudgetText(
                        text = if (item.allocatedAmount > 0.0) percentage(ratio) else "—",
                        size = 14,
                        color = BudgetMuted
                    )
                }

                BudgetProgressBar(ratio)
            }
        }
    }
}

@Composable
private fun BudgetBottomBar(
    onHome: () -> Unit,
    onProfile: () -> Unit
) {
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

private val budgetPaths = mapOf(
    "back" to "M20,12 H4 M4,12 L11,5 M4,12 L11,19",
    "next" to "M9,5 L16,12 L9,19",
    "edit" to "M13,5 H4 V21 H20 V12 M10,16 L11,12 L19,4 L22,7 L14,15 Z",
    "home" to "M2,11 L12,3 L22,11 M5,10 V21 H10 V15 H14 V21 H19 V10",
    "profile" to "M16,7 A4,4 0,1 1,8,7 A4,4 0,1 1,16,7 M3,21 Q3,14 12,14 Q21,14 21,21 Z",
    "wallet" to "M3,7 V6 Q3,3 6,3 H17 Q19,3 19,6 M4,7 H20 Q22,7 22,9 V19 Q22,21 20,21 H5 Q3,21 3,19 V7 Z M22,12 H16 A2,2 0,0 0,16,16 H22 M17,14 H17.3",
    "hair stylists" to "M7,19 C4,18 4,15 5,12 C4,7 7,3 12,3 C17,3 19,7 18,11 C19,14 21,18 18,20 Q16,21 15,17 M8,10 Q11,9 13,6 Q14,9 16,10 V12 C16,17 10,19 8,13 Z M10,16 Q10,19 7,20 L6,22 M14,16 Q14,19 16,20 L18,22 M4,5 L5,3 L6,5 L8,6 L6,7 L5,9 L4,7 L2,6 Z",
    "makeup artists" to "M2,15 A6,6 0,1 1,14,15 A6,6 0,1 1,2,15 M4,15 A4,4 0,1 1,12,15 A4,4 0,1 1,4,15 M2,22 H14 M17,22 V13 H22 V22 Z M18,13 V8 L21,5 V13",
    "venues" to "M2,10 L12,3 L22,10 Z M4,11 V21 H20 V11 M2,21 H22 M7,13 H9 V16 H7 Z M15,13 H17 V16 H15 Z M10,21 V18 A2,2 0,0 1,14,18 V21",
    "buffet" to "M2,16 H22 M4,15 A8,8 0,0 1,20,15 M11,6 A1,1 0,1 1,13,6 M3,19 L6,18 L11,20 H15 L19,17 Q22,16 21,18 L16,22 H10 L5,20",
    "dj" to "M8,17 V5 L19,3 V15 M8,8 L19,6 M8,17 A3,2.2 0,1 1,2,17 A3,2.2 0,1 1,8,17 M19,15 A3,2.2 0,1 1,13,15 A3,2.2 0,1 1,19,15",
    "photography" to "M2,8 H7 L9,5 H15 L17,8 H22 V20 H2 Z M16,14 A4,4 0,1 1,8,14 A4,4 0,1 1,16,14 M18,10 H20"
)

@Composable
internal fun BudgetIcon(
    name: String,
    iconSize: Dp,
    tint: Color = BudgetInk,
    description: String? = null
) {
    val data = budgetPaths[name.trim().lowercase(Locale.ROOT)]
        ?: budgetPaths.getValue("wallet")

    val path = remember(data) {
        requireNotNull(PathParser.createPathFromPathData(data)).asComposePath()
    }

    val labelModifier = if (description == null) {
        Modifier
    } else {
        Modifier.semantics { contentDescription = description }
    }

    Canvas(Modifier.size(iconSize).then(labelModifier)) {
        val factor = size.minDimension / 24f

        scale(factor, factor, pivot = Offset.Zero) {
            drawPath(
                path = path,
                color = tint,
                style = Stroke(
                    width = 1.4f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
private fun BudgetExample(categories: List<BudgetCategoryUi>) {
    var editing by remember { mutableStateOf(false) }
    var savedTotal by remember { mutableStateOf(17000.0) }
    var savedCategories by remember(categories) { mutableStateOf(categories) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }

    val categoryId = selectedCategoryId
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
        if (categoryId != null) {
            SampleBudgetCategoryDetails(
                categoryId = categoryId,
                onBack = { selectedCategoryId = null }
            )
        } else if (editing) {
            EditBudgetScreen(
                initialTotalBudget = savedTotal,
                initialCategories = savedCategories,
                roleLabel = "Bride Line.Groom",
                onBack = { editing = false },
                onSave = { total, allocations ->
                    savedTotal = total
                    savedCategories = allocations
                    editing = false
                },
                onHome = {},
                onProfile = {}
            )
        } else {
            BudgetOverviewScreen(
                totalBudget = savedTotal,
                spentAmount = 7580.0,
                categories = savedCategories,
                roleLabel = "Bride Line.Groom",
                onBack = {},
                onAllocate = { editing = true },
                onEdit = { editing = true },
                onCategoryClick = { selectedCategoryId = it },
                onHome = {},
                onProfile = {}
            )
        }
    }
}

@Preview(
    name = "Before allocation",
    widthDp = 402,
    heightDp = 760,
    showBackground = true
)
@Composable
private fun BudgetBeforePreview() {
    BudgetExample(emptyList())
}

@Preview(
    name = "After allocation",
    widthDp = 402,
    heightDp = 760,
    showBackground = true
)
@Composable
private fun BudgetAfterPreview() {
    BudgetExample(
        listOf(
            BudgetCategoryUi("hair", "Hair Stylists", 2000.0, 700.0),
            BudgetCategoryUi("makeup", "Makeup Artists", 2000.0, 1500.0),
            BudgetCategoryUi("venues", "Venues", 3000.0, 2800.0),
            BudgetCategoryUi("buffet", "Buffet", 1000.0, 580.0),
            BudgetCategoryUi("dj", "DJ", 3000.0, 2000.0),
            BudgetCategoryUi("photo", "Photography", 2000.0, 0.0)
        )
    )
}