package com.example.brideline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brideline.R

enum class ServicesNavTab(
    val label: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Outlined.Home),
    Services("Services", Icons.Outlined.Assignment),
    Profile("Profile", Icons.Outlined.PersonOutline)
}

private val servicesNavInk = Color(0xFF45353A)
private val servicesNavBlush = Color(0xFFEDBEC8)
private val servicesNavRose = Color(0xFFD2647D)
private val servicesNavBackground = Color(0xFFECEDF3)

private val servicesNavFont = FontFamily(
    Font(R.font.archivo_semibold, FontWeight.SemiBold)
)

@Composable
fun BridelineServicesBottomBar(
    selectedTab: ServicesNavTab,
    onTabSelected: (ServicesNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(servicesNavBackground)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = servicesNavRose.copy(alpha = 0.55f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ServicesNavTab.values().forEach { tab ->
                val selected = tab == selectedTab

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (selected) servicesNavBlush.copy(alpha = 0.52f)
                            else Color.Transparent
                        )
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = servicesNavInk,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = tab.label,
                        color = servicesNavInk,
                        fontFamily = servicesNavFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(name = "Services selected", showBackground = true, widthDp = 402)
@Composable
private fun BridelineServicesBottomBarPreview() {
    BridelineServicesBottomBar(
        selectedTab = ServicesNavTab.Services,
        onTabSelected = {}
    )
}
