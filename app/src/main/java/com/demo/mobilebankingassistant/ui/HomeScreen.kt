@file:OptIn(ExperimentalMaterial3Api::class)

package com.demo.mobilebankingassistant.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demo.mobilebankingassistant.data.AddressChangeRequest
import com.demo.mobilebankingassistant.data.BankingUser
import com.demo.mobilebankingassistant.data.CardInfo
import com.demo.mobilebankingassistant.data.Transaction
import com.demo.mobilebankingassistant.ui.theme.ScBlue
import com.demo.mobilebankingassistant.ui.theme.ScGreen

@Composable
fun HomeScreen(
    user: BankingUser,
    serviceConnected: Boolean,
    onLogout: () -> Unit,
    onAssistantClick: () -> Unit,
    onHomeClick: () -> Unit,
    onDepositsClick: () -> Unit,
    onStatementClick: () -> Unit,
    onServicesClick: () -> Unit,
    onPinChangeClick: () -> Unit
) {
    Scaffold(
        topBar = { ScTopBar(title = "Morning", onLogout = onLogout) },
        bottomBar = { ScBottomBar(selected = "Home", onHomeClick = onHomeClick, onServicesClick = onServicesClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAssistantClick, containerColor = ScBlue, contentColor = Color.White, shape = CircleShape) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Assistant")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F6FA)),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { PromoBanner() }
            item { PagerDots() }
            item { QuickLinks(onPinChangeClick = onPinChangeClick) }
            item {
                AssetsCard(
                    user = user,
                    serviceConnected = serviceConnected,
                    expanded = false,
                    onDepositsClick = onDepositsClick,
                    onStatementClick = onStatementClick
                )
            }
        }
    }
}

@Composable
fun AccountDetailsScreen(
    user: BankingUser,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onHomeClick: () -> Unit,
    onStatementClick: () -> Unit,
    onServicesClick: () -> Unit
) {
    Scaffold(
        topBar = { ScTopBar(title = "Morning", onLogout = onLogout) },
        bottomBar = { ScBottomBar(selected = "Home", onHomeClick = onHomeClick, onServicesClick = onServicesClick) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F6FA)),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            item { PagerDots() }
            item { QuickLinks(onPinChangeClick = onServicesClick) }
            item {
                AssetsCard(
                    user = user,
                    serviceConnected = true,
                    expanded = true,
                    onDepositsClick = { },
                    onStatementClick = onStatementClick
                )
            }
        }
    }
}

@Composable
fun StatementDetailsScreen(user: BankingUser, onBack: () -> Unit) {
    Scaffold(topBar = { SimpleBackBar("Deposits", onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F6FA)),
            contentPadding = PaddingValues(18.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                    colors = CardDefaults.cardColors(containerColor = ScBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF064AF4), ScBlue, ScGreen)))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("Current & Savings", color = Color.White, fontSize = 11.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(user.savingsAccount.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(user.savingsAccount.maskedNumber, color = Color.White, fontSize = 12.sp)
                            Spacer(Modifier.height(14.dp))
                            Text("Current Balance", color = Color.White, fontSize = 11.sp)
                            Text(user.savingsAccount.balance, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { },
                            modifier = Modifier.align(Alignment.BottomEnd),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("View Available", fontSize = 11.sp) }
                    }
                }
            }
            item { TransactionSectionHeader() }
            user.transactions.forEach { transaction ->
                item { TransactionRow(transaction) }
            }
        }
    }
}

@Composable
fun ServicesScreen(
    user: BankingUser,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onHomeClick: () -> Unit,
    onCardsClick: () -> Unit,
    onPinChangeClick: () -> Unit,
    onAddressChangeClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Services & Settings", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                actions = { HeaderIcons(onLogout = onLogout, light = true) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0871E8))
            )
        },
        bottomBar = { ScBottomBar(selected = "Services", onHomeClick = onHomeClick, onServicesClick = { }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F6FA)),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(6.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold, color = Color(0xFF2D2935))
                            Text(user.clientType, fontSize = 12.sp, color = Color(0xFF5F5A66))
                            Text("Last Login: ${user.lastLogin}", fontSize = 10.sp, color = Color(0xFF6B6570))
                        }
                        ScLogoMark()
                    }
                }
            }
            item {
                Text("Digital Services", Modifier.padding(top = 18.dp, bottom = 8.dp), color = Color(0xFF55505C), fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallServiceButton("Accounts\nStatement\nRequest", Icons.Default.Description, onBack, Modifier.weight(1f))
                    SmallServiceButton("Debit Card\nPIN Change", Icons.Default.CreditCard, onPinChangeClick, Modifier.weight(1f))
                    SmallServiceButton("Cards\nManage Card\nUsage", Icons.Default.Style, onCardsClick, Modifier.weight(1f))
                }
            }
            item { ServiceTile(Icons.Default.PersonOutline, "Update Profile Details", "Manage and edit your personal information, contact details, address and more.", onAddressChangeClick) }
            item { ServiceTile(Icons.Default.AccountBalance, "Accounts & Deposits", "Manage all your account and deposit related services like cheque book request, nominee update and more.", onBack) }
            item { ServiceTile(Icons.Default.CreditCard, "Cards", "Control your card with ease - set limits, change pin, manage card usage and much more.", onCardsClick) }
            item { ServiceTile(Icons.Default.Apartment, "Loans", "Access all your loan services like statement request, copy of T&Cs and more.", onBack) }
        }
    }
}

@Composable
fun CardServicesScreen(user: BankingUser, onBack: () -> Unit, onPinChangeClick: () -> Unit) {
    Scaffold(topBar = { SimpleBackBar("Back", onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F6FA)),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Accounts & Deposits", fontSize = 12.sp, color = Color(0xFF2D2935))
                    AssistChip(onClick = { }, label = { Text("Cards", fontSize = 12.sp) })
                    Text("Loans", fontSize = 12.sp, color = Color(0xFF2D2935), modifier = Modifier.padding(top = 8.dp))
                }
                Text("Cards", Modifier.padding(vertical = 12.dp), fontSize = 13.sp)
            }
            user.creditCards.firstOrNull()?.let { card ->
                item {
                    Card(
                        Modifier.padding(bottom = 12.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(Color.White),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, null, tint = ScBlue)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(card.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2935))
                                Text("${card.maskedNumber} • ${card.type}", fontSize = 11.sp, color = Color(0xFF6B6570))
                            }
                            AssistChip(
                                onClick = { },
                                label = { Text(card.status, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (card.status == "BLOCKED") Color(0xFFFFE8E8) else Color(0xFFE8FFF2),
                                    labelColor = if (card.status == "BLOCKED") Color(0xFFB00020) else ScGreen
                                )
                            )
                        }
                    }
                }
            }
            val services = listOf(
                "Report Lost/Stolen Card",
                "Replace Card",
                "Debit/ATM Card PIN Change",
                "Credit Balance Refund",
                "Credit Card PIN Change",
                "Credit Card Transaction Dispute",
                "Activate my Credit Card",
                "Statement Request",
                "Manage Card Usage",
                "Statement Date Change",
                "Credit Card redelivery"
            )
            services.forEach { title ->
                item { ServiceListRow(title, if (title == "Debit/ATM Card PIN Change") onPinChangeClick else onBack) }
            }
        }
    }
}

@Composable
fun PinCardSelectScreen(cards: List<CardInfo>, onBack: () -> Unit, onNext: () -> Unit) {
    PinChangeScaffold(title = "Select a Debit Card", step = "1 of 2", onBack = onBack) {
        Text("SELECT A CARD TO SETUP A PIN", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
        cards.forEach { card ->
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { onNext() }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(2.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, null, tint = ScBlue)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(card.name, fontSize = 13.sp)
                        Text("${card.type} ${card.maskedNumber}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                }
            }
        }
        PinNotes()
    }
}

@Composable
fun PinChangeFormScreen(card: CardInfo, onBack: () -> Unit, onPinChanged: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    PinChangeScaffold(title = "Set Debit Card PIN", step = "2 of 2", onBack = onBack) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CreditCard, null, tint = ScBlue)
            Spacer(Modifier.width(12.dp))
            Text("${card.name}\n${card.maskedNumber}", fontSize = 12.sp)
        }
        Text("Set your new Debit Card PIN", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = ScBlue, fontSize = 13.sp)
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.take(4) },
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            label = { Text("ENTER YOUR NEW 4-DIGIT PIN *", fontSize = 11.sp) },
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = confirmPin,
            onValueChange = { confirmPin = it.take(4) },
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            label = { Text("RE-ENTER YOUR NEW 4-DIGIT PIN *", fontSize = 11.sp) },
            singleLine = true
        )
        PinNotes()
        Button(
            onClick = onPinChanged,
            enabled = pin.length == 4 && pin == confirmPin,
            modifier = Modifier.padding(16.dp).fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScBlue,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD8D2DC),
                disabledContentColor = Color(0xFF6F6875)
            ),
            shape = RoundedCornerShape(4.dp)
        ) { Text("Submit", fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun AddressChangeScreen(
    user: BankingUser,
    onBack: () -> Unit,
    onSubmit: (AddressChangeRequest) -> Unit
) {
    var line1 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    val canSubmit = line1.isNotBlank() && city.isNotBlank() && state.isNotBlank() && postalCode.isNotBlank()

    Scaffold(
        topBar = { SimpleBackBar("Update Profile Details", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F6FA)),
            contentPadding = PaddingValues(18.dp)
        ) {
            item {
                Text("Change Address", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2935))
                Spacer(Modifier.height(6.dp))
                Text(
                    "We will create a backend service request for ${user.name}. Do not enter confidential credentials or OTPs here.",
                    fontSize = 13.sp,
                    color = Color(0xFF6B6570),
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(18.dp))
            }
            item {
                Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = line1,
                            onValueChange = { line1 = it },
                            label = { Text("Address line") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = postalCode, onValueChange = { postalCode = it }, label = { Text("PIN / Postal code") }, modifier = Modifier.fillMaxWidth())
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        onSubmit(AddressChangeRequest(line1 = line1, city = city, state = state, postalCode = postalCode))
                    },
                    enabled = canSubmit,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScBlue),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Submit address change")
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmDialog(onConfirm: () -> Unit, onStay: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x880061C9)),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.padding(30.dp).fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(Color(0xFFF9F4F7))) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(22.dp)) {
                Surface(modifier = Modifier.size(112.dp), shape = CircleShape, color = Color.White, border = BorderStroke(10.dp, ScBlue)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("?", color = ScBlue, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Are you sure you want to logout?", color = Color(0xFF5B5565), fontSize = 14.sp)
                Spacer(Modifier.height(22.dp))
                TextButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = ScBlue)
                        Spacer(Modifier.width(14.dp))
                        Text("Log out", color = Color(0xFF4C4658), fontSize = 16.sp)
                    }
                }
                Button(
                    onClick = onStay,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF075DF2)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                    Spacer(Modifier.width(14.dp))
                    Text("Stay logged in", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ScTopBar(title: String, onLogout: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1A28)) },
        actions = { HeaderIcons(onLogout = onLogout, light = false) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F6FA))
    )
}

@Composable
private fun HeaderIcons(onLogout: () -> Unit, light: Boolean) {
    val tint = if (light) Color.White else Color(0xFF2E2937)
    IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, null, tint = tint) }
    IconButton(onClick = { }) { Icon(Icons.Default.ChatBubbleOutline, null, tint = tint) }
    Button(
        onClick = onLogout,
        colors = ButtonDefaults.buttonColors(containerColor = if (light) Color.White else Color.Black),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier.height(30.dp)
    ) {
        Text("Logout", color = if (light) ScBlue else Color.White, fontSize = 10.sp)
    }
}

@Composable
private fun SimpleBackBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
        actions = {
            IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, null) }
            IconButton(onClick = { }) { Icon(Icons.Default.ChatBubbleOutline, null) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F6FA))
    )
}

@Composable
private fun PromoBanner() {
    Card(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp).fillMaxWidth().height(150.dp), shape = RoundedCornerShape(8.dp)) {
        Row(
            Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color(0xFF0B1D88), Color(0xFF0B55F0), Color(0xFFDDEFFF))))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Box(Modifier.width(3.dp).height(58.dp).background(ScGreen))
                Text("Salary that\nbuilds a legacy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(6.dp))
                Text("Turn monthly income\ninto lasting wealth with\nSC Invest", color = Color.White, fontSize = 11.sp, lineHeight = 14.sp)
                Spacer(Modifier.height(8.dp))
                Surface(color = ScGreen, shape = RoundedCornerShape(10.dp)) {
                    Text("Explore Mutual Funds Now", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            Surface(modifier = Modifier.size(104.dp), color = Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(4.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = ScBlue, modifier = Modifier.size(54.dp))
                }
            }
        }
    }
}

@Composable
private fun PagerDots() {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.Center) {
        Box(Modifier.width(26.dp).height(4.dp).background(Color(0xFF075DF2), RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(5.dp))
        Box(Modifier.size(4.dp).background(Color(0xFFD3D0D8), CircleShape))
        Spacer(Modifier.width(5.dp))
        Box(Modifier.size(4.dp).background(Color(0xFFD3D0D8), CircleShape))
    }
}

@Composable
private fun QuickLinks(onPinChangeClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        QuickLinkItem(Icons.Default.AccountBalance, "SCInvest", { })
        QuickLinkItem(Icons.Default.CurrencyRupee, "Pay Bills", { })
        QuickLinkItem(Icons.Default.Diamond, "Upgrade\nNow", { })
        QuickLinkItem(Icons.Default.Badge, "Card PIN\nChange", onPinChangeClick)
    }
}

@Composable
private fun QuickLinkItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp).clickable { onClick() }) {
        Surface(modifier = Modifier.size(48.dp), color = Color(0xFF075DF2), shape = RoundedCornerShape(13.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(25.dp)) }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 13.sp, color = Color(0xFF2D2935))
    }
}

@Composable
private fun AssetsCard(
    user: BankingUser,
    serviceConnected: Boolean,
    expanded: Boolean,
    onDepositsClick: () -> Unit,
    onStatementClick: () -> Unit
) {
    Card(Modifier.padding(18.dp).fillMaxWidth(), shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Assets (i)", color = Color(0xFF6E6876), fontSize = 13.sp)
                Text(user.totalAssets, color = Color(0xFF2D2935), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Text(if (serviceConnected) "Mock service connected" else "Using offline fallback data", color = if (serviceConnected) ScGreen else Color.Gray, fontSize = 10.sp)
            AccountSection(user = user, expanded = expanded, onDepositsClick = onDepositsClick, onStatementClick = onStatementClick)
            Divider(Modifier.padding(vertical = 8.dp))
            AssetSummaryRow(Icons.Default.BarChart, "Investments", "Total Balance", user.investments)
            Divider(Modifier.padding(vertical = 8.dp))
            CreditCardLiabilitiesSection(user)
        }
    }
}

@Composable
private fun AccountSection(user: BankingUser, expanded: Boolean, onDepositsClick: () -> Unit, onStatementClick: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().clickable { onDepositsClick() }, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PieChart, null, tint = ScGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Deposits", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D2935))
                Text("Total Balance", fontSize = 12.sp, color = Color(0xFF6B6570))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(user.savingsAccount.balance, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2935))
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = ScBlue)
            }
        }
        if (expanded) {
            Card(
                Modifier.padding(top = 12.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(Color(0xFFE8FFD0)),
                shape = RoundedCornerShape(2.dp)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) { Text("One time transfer", fontSize = 12.sp) }
                        OutlinedButton(onClick = { }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) { Text("Pay to my Payees", fontSize = 12.sp) }
                    }
                    Card(Modifier.padding(top = 12.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFFFBF7FA)), shape = RoundedCornerShape(8.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(72.dp), color = ScBlue, shape = RoundedCornerShape(4.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Savings, null, tint = ScGreen) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${user.savingsAccount.name} ${user.savingsAccount.maskedNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2D2935))
                                Spacer(Modifier.height(8.dp))
                                Text("Current Balance", fontSize = 13.sp, color = Color(0xFF2D2935))
                                Text(user.savingsAccount.balance, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2D2935))
                            }
                        }
                    }
                    OutlinedButton(onClick = onStatementClick, Modifier.padding(top = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
                        Icon(Icons.Default.Description, null, tint = ScBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("Open statement details", color = ScBlue, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetSummaryRow(icon: ImageVector, title: String, subtitle: String, amount: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ScGreen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2D2935))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF6B6570))
        }
        Text(amount, fontSize = 12.sp, color = Color(0xFF2D2935))
        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
    }
}

@Composable
private fun CreditCardLiabilitiesSection(user: BankingUser) {
    val card = user.creditCards.firstOrNull()
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = ScGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Total Liabilities (i)", fontSize = 15.sp, color = Color(0xFF2D2935))
            }
            Text(user.totalLiabilities, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2935))
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CreditCard, null, tint = ScGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Credit Cards", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D2935))
                Text("Outstanding Balance", fontSize = 12.sp, color = Color(0xFF6B6570))
            }
            Text(user.totalLiabilities, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2935))
            Icon(Icons.Default.KeyboardArrowUp, null, tint = ScBlue)
        }
        Card(
            Modifier.padding(top = 12.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(Color(0xFFE2FFF6)),
            shape = RoundedCornerShape(2.dp)
        ) {
            Column(Modifier.padding(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) {
                        Text("Pay SC Credit Cards", fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = { }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) {
                        Text("360 Rewards", fontSize = 12.sp)
                    }
                }
                Card(Modifier.padding(top = 12.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFFFBF7FA)), shape = RoundedCornerShape(8.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(72.dp), color = ScBlue, shape = RoundedCornerShape(4.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CreditCard, null, tint = ScGreen, modifier = Modifier.size(34.dp)) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Financial Account ${card?.maskedNumber.orEmpty()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2D2935))
                            Text("Due 02 Aug: ${user.totalLiabilities}", fontSize = 13.sp, color = Color(0xFF2D2935))
                            Spacer(Modifier.height(8.dp))
                            Text("Outstanding Balance", fontSize = 13.sp, color = Color(0xFF2D2935))
                            Text(user.totalLiabilities, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D2935))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScBottomBar(selected: String, onHomeClick: () -> Unit, onServicesClick: () -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 2.dp) {
        val items = listOf(
            Triple("Home", Icons.Default.Home, onHomeClick),
            Triple("Pay & Transfer", Icons.Default.SyncAlt, {}),
            Triple("Discover", Icons.Default.ShoppingCart, {}),
            Triple("Services", Icons.Default.AccountBalanceWallet, onServicesClick)
        )
        items.forEach { item ->
            NavigationBarItem(
                selected = item.first == selected,
                onClick = item.third,
                icon = { Icon(item.second, contentDescription = item.first) },
                label = { Text(item.first, fontSize = 9.sp) }
            )
        }
    }
}

@Composable
private fun TransactionSectionHeader() {
    Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Transactions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2935))
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    Column(Modifier.padding(top = 14.dp)) {
        Text(transaction.date, color = Color(0xFF5E5966), fontSize = 12.sp)
        Card(Modifier.padding(top = 6.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(4.dp)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (transaction.credit) Icons.Default.SouthWest else Icons.Default.NorthEast, null, tint = if (transaction.credit) ScGreen else ScBlue)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(transaction.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(transaction.subtitle, fontSize = 11.sp, color = Color.Gray)
                }
                Text(transaction.amount, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SmallServiceButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.clickable { onClick() }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color(0xFFE5E1E9))) {
        Column(Modifier.padding(8.dp).height(62.dp)) {
            Icon(icon, null, tint = ScBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(text, fontSize = 10.sp, lineHeight = 12.sp, color = Color(0xFF2D2935))
        }
    }
}

@Composable
private fun ServiceTile(icon: ImageVector, title: String, body: String, onClick: () -> Unit) {
    Card(Modifier.padding(top = 10.dp).fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(6.dp)) {
        Row(Modifier.padding(14.dp)) {
            Icon(icon, null, tint = ScGreen, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2D2935))
                Text(body, fontSize = 11.sp, color = Color(0xFF6A6370), lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun ServiceListRow(title: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(0.dp)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), fontSize = 14.sp, color = Color(0xFF2D2935))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
private fun PinChangeScaffold(title: String, step: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debit/ATM Card Pin Change", color = Color.White, fontSize = 15.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                actions = { IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0788E8))
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8F6FA))) {
            Row(Modifier.fillMaxWidth().background(Color(0xFF0788E8)).padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color.White, fontSize = 13.sp)
                Text(step, color = Color.White, fontSize = 12.sp)
            }
            content()
        }
    }
}

@Composable
private fun PinNotes() {
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Divider(Modifier.weight(1f))
            Text("  NOTES  ", color = Color.Gray, fontSize = 11.sp)
            Divider(Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        val notes = listOf(
            "Please take note that 4-digit PIN creation/reset can only be performed on activated cards.",
            "To change your 4-digit PIN, you will be required to enter the one-time password (OTP) that was sent to your registered mobile number.",
            "DO NOT reveal your 4-digit PIN to anyone. 4-digit PIN must be memorized and not recorded anywhere.",
            "DO NOT use easily recognized numbers such as your birthday, anniversary, National ID, telephone number etc. as your 4-digit PIN.",
            "Avoid using sequential numbers such as 1234 or same number more than twice such as 2222.",
            "4-Digit PIN must be kept confidential at all times and not be divulged to anyone."
        )
        notes.forEachIndexed { index, note ->
            Text("${index + 1}. $note", color = Color(0xFF8B8490), fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun ScLogoMark() {
    Column(horizontalAlignment = Alignment.End) {
        Box(Modifier.width(38.dp).height(12.dp).background(ScBlue, RoundedCornerShape(8.dp)))
        Spacer(Modifier.height(4.dp))
        Box(Modifier.width(38.dp).height(12.dp).background(ScGreen, RoundedCornerShape(8.dp)))
    }
}

