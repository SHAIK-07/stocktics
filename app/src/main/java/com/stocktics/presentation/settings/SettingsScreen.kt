package com.stocktics.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.liquidGlassElevated
import com.stocktics.ui.components.StockticsDangerButton
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.GainGreenBg
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.stocktics.data.profile.SupabaseProfileClient
import com.stocktics.data.session.UserSessionManager
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.components.StockticsSecondaryButton
import com.stocktics.ui.components.StockticsTextField
import androidx.compose.runtime.collectAsState
import com.stocktics.data.indmoney.IndmoneySyncManager
import com.stocktics.presentation.indmoney.IndmoneyAuthDialog
import com.stocktics.ui.theme.ThemeManager
import com.stocktics.ui.theme.FintechSurface
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userEmail: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onConfiguringWeightsChange: (Boolean) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userNickname by remember { mutableStateOf(UserSessionManager.getLoggedInUserNickname(context)) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingNickname by remember { mutableStateOf(userNickname) }
    var isSavingNickname by remember { mutableStateOf(false) }

    // INDmoney MCP Sync State
    val indmoneyStatus by IndmoneySyncManager.status.collectAsState()
    val isIndmoneySyncing by IndmoneySyncManager.isSyncing.collectAsState()
    val indmoneyFeedback by IndmoneySyncManager.lastFeedback.collectAsState()
    var indmoneyAuthUrl by remember { mutableStateOf<String?>(null) }
    var isOpeningAuth by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        IndmoneySyncManager.checkStatus(context)
        val remote = SupabaseProfileClient.fetchUserProfile(context)
        if (!remote.isNullOrBlank()) {
            userNickname = remote
        }
    }

    // Simplified Investor Configuration
    var investorStyle by remember { mutableStateOf("Balanced") }
    var riskAppetite by remember { mutableStateOf("Moderate") }

    var isConfiguringWeights by remember { mutableStateOf(false) }
    var quantWeights by remember { mutableStateOf(com.stocktics.data.settings.QuantWeightsPreferences.getWeights(context)) }

    if (isConfiguringWeights) {
        QuantWeightsConfigScreen(
            onBack = {
                quantWeights = com.stocktics.data.settings.QuantWeightsPreferences.getWeights(context)
                isConfiguringWeights = false
                onConfiguringWeightsChange(false)
            },
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onBack != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FintechSurfaceElevated)
                            .border(1.dp, FintechBorder, CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }
                Column {
                    Text(
                        text = "Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Preferences, weights & account",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Appearance / Theme Card (Dark / White / System Buttons)
        item {
            val currentMode = ThemeManager.currentTheme
            StockticsCard(
                cornerRadius = 16.dp,
                contentPadding = 18.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = FintechPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Appearance & Theme",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Button Row: Dark Glass, Light Glass, System
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassElevated(12.dp)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val themeOptions = listOf(
                        Triple(ThemeManager.ThemeMode.DARK, "Dark Glass", "🌙"),
                        Triple(ThemeManager.ThemeMode.LIGHT, "Light Glass", "☀️"),
                        Triple(ThemeManager.ThemeMode.SYSTEM, "System", "📱")
                    )

                    themeOptions.forEach { (mode, label, icon) ->
                        val isSelected = currentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FintechPrimary else Color.Transparent)
                                .clickable {
                                    ThemeManager.setTheme(context, mode)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Account Profile Card
        // Profile Info Card (with Nickname, Email & Edit Action)
        item {
            StockticsCard(
                cornerRadius = 16.dp,
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GainGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = GainGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userNickname,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userEmail,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GainGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Authenticated & Connected",
                                fontSize = 11.sp,
                                color = GainGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Edit Nickname Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FintechSurfaceElevated)
                            .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                editingNickname = userNickname
                                showEditDialog = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Nickname",
                                tint = FintechPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Edit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FintechPrimary
                            )
                        }
                    }
                }
            }
        }

        // INDmoney MCP Portfolio Sync & Integration Card
        item {
            IndmoneySettingsCard(
                status = indmoneyStatus,
                isSyncing = isIndmoneySyncing || isOpeningAuth,
                lastFeedback = indmoneyFeedback,
                onConnectClick = {
                    scope.launch {
                        isOpeningAuth = true
                        try {
                            val url = IndmoneySyncManager.getAuthUrl(context)
                            if (!url.isNullOrBlank()) {
                                indmoneyAuthUrl = url
                            }
                        } finally {
                            isOpeningAuth = false
                        }
                    }
                },
                onSyncNowClick = {
                    scope.launch {
                        IndmoneySyncManager.performSync(context)
                    }
                },
                onDisconnectClick = {
                    scope.launch {
                        IndmoneySyncManager.disconnect(context)
                    }
                }
            )
        }

        // Investor Style Segmented Card
        item {
            StockticsCard(
                cornerRadius = 16.dp,
                contentPadding = 18.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = FintechPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Investor Style & Horizon",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    StockticsBadge(
                        text = investorStyle,
                        variant = BadgeVariant.GAIN
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Control (Explore style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechBackground)
                        .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Swing", "Balanced", "Long-Term").forEach { style ->
                        val isSelected = investorStyle == style
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) FintechPrimary else Color.Transparent)
                                .clickable { investorStyle = style }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = style,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (investorStyle) {
                        "Swing" -> "10–15 Days horizon. Prioritizes short-term momentum, meta-labeling, and fast profit targets."
                        "Long-Term" -> "6–12+ Months horizon. Prioritizes forensic governance, balance sheet health, and DCF intrinsic value."
                        else -> "1–6 Months horizon. Audited optimal blend of technicals, ML probability, and smart money flow."
                    },
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Risk Appetite Segmented Card
        item {
            StockticsCard(
                cornerRadius = 16.dp,
                contentPadding = 18.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = FintechPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Risk Appetite",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    StockticsBadge(
                        text = riskAppetite,
                        variant = when (riskAppetite) {
                            "Conservative" -> BadgeVariant.GAIN
                            "Aggressive" -> BadgeVariant.LOSS
                            else -> BadgeVariant.INFO
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechBackground)
                        .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Conservative", "Moderate", "Aggressive").forEach { risk ->
                        val isSelected = riskAppetite == risk
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) FintechPrimary else Color.Transparent)
                                .clickable { riskAppetite = risk }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = risk,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (riskAppetite) {
                        "Conservative" -> "Requires Buy Score ≥ 80. Strict stop-loss at -3.5% with high win-rate confidence filter."
                        "Aggressive" -> "Requires Buy Score ≥ 60. Wider stop-loss at -7.5% capturing larger breakout volatility."
                        else -> "Requires Buy Score ≥ 70. Standard stop-loss at -5.0% and +15.0% target."
                    },
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Quant Engine Architecture Card (Audited Optimal Standards)
        item {
            StockticsCard(
                cornerRadius = 16.dp,
                contentPadding = 18.dp,
                modifier = Modifier.clickable {
                    isConfiguringWeights = true
                    onConfiguringWeightsChange(true)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Quant Engine Standard Weights",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Configure",
                                tint = FintechPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to configure & slide weights like before",
                            fontSize = 11.sp,
                            color = FintechPrimary
                        )
                    }
                    StockticsBadge(
                        text = if (quantWeights.isAuditedOptimal) "AUDITED OPTIMAL" else "CUSTOM (${quantWeights.totalWeightPct}%)",
                        variant = if (quantWeights.isAuditedOptimal) BadgeVariant.GAIN else BadgeVariant.INFO
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tech", fontSize = 10.sp, color = TextTertiary)
                        Text("${(quantWeights.techWeight * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ML", fontSize = 10.sp, color = TextTertiary)
                        Text("${(quantWeights.mlWeight * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Smart $", fontSize = 10.sp, color = TextTertiary)
                        Text("${(quantWeights.smartMoneyWeight * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Regime", fontSize = 10.sp, color = TextTertiary)
                        Text("${(quantWeights.regimeWeight * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Forensic", fontSize = 10.sp, color = TextTertiary)
                        Text("${(quantWeights.forensicWeight * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }

        // Logout Action Button (Clean Outlined Style)
        item {
            androidx.compose.material3.OutlinedButton(
                onClick = onLogout,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FintechBorder),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = com.stocktics.ui.theme.FintechSurface,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                    contentDescription = null,
                    tint = LossRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out from Stocktics",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }

    // Modal Dialog: Edit Profile Nickname
    if (showEditDialog) {
        Dialog(
            onDismissRequest = { if (!isSavingNickname) showEditDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FintechSurface)
                    .border(1.dp, FintechBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Edit Profile Nickname",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your nickname appears on the Home dashboard, personal reports, and greetings.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StockticsTextField(
                        value = editingNickname,
                        onValueChange = { editingNickname = it },
                        label = "Nickname / Display Name",
                        placeholder = "e.g. Shaik, Alexander, Trader",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StockticsSecondaryButton(
                            text = "Cancel",
                            onClick = { showEditDialog = false },
                            modifier = Modifier.weight(1f),
                            enabled = !isSavingNickname
                        )

                        StockticsPrimaryButton(
                            text = "Save",
                            onClick = {
                                val trimmed = editingNickname.trim()
                                if (trimmed.isNotBlank()) {
                                    scope.launch {
                                        isSavingNickname = true
                                        SupabaseProfileClient.updateUserNickname(context, trimmed)
                                        userNickname = trimmed
                                        isSavingNickname = false
                                        showEditDialog = false
                                    }
                                }
                            },
                            isLoading = isSavingNickname,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // In-App INDmoney OAuth Dialog
    indmoneyAuthUrl?.let { url ->
        IndmoneyAuthDialog(
            authUrl = url,
            onDismiss = { indmoneyAuthUrl = null },
            onAuthSuccess = {
                indmoneyAuthUrl = null
                scope.launch {
                    IndmoneySyncManager.performSync(context)
                }
            }
        )
    }
}


