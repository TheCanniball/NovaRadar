package com.novascanner.network.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.components.NovaButton
import com.novascanner.network.ui.theme.*
import androidx.compose.ui.graphics.Color
import com.novascanner.network.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun EasyInstallerScreen(viewModel: MainViewModel) {
    val isDeploying by viewModel.isDeploying.collectAsState()
    val workerUrl by viewModel.workerUrl.collectAsState()
    val deploySteps by viewModel.deploySteps.collectAsState()
    val isFa = Strings.isRtl
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<MainViewModel.DeployResult?>(null) }

    Column(Modifier.fillMaxSize().background(Background).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(if (isFa) "نصب خودکار نوا" else "Install Nova automatically",
            color = Primary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(if (isFa) "یک توکن Cloudflare بچسبان تا پنل را روی حساب خودت بسازیم — ورکر و دیتابیس، کاملاً آماده."
            else "Paste one Cloudflare token and we'll build your panel on your account — the worker and database, fully set up.",
            color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        if (!showResult) {
            // Step 1: Cloudflare account
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).background(Primary, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Text("1", color = Background, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(if (isFa) "حساب رایگان Cloudflare داری؟" else "Have a free Cloudflare account?",
                            fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(if (isFa) "اگر نداری، یک حساب رایگان بساز (۱ دقیقه). برای نگه‌داری نوا لازم است."
                        else "If not, make one free (takes 1 min). You'll need it to hold your Nova.",
                        color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    NovaButton(if (isFa) "ساخت حساب رایگان" else "Create a free account",
                        onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://dash.cloudflare.com/sign-up"))) },
                        modifier = Modifier.fillMaxWidth(), color = SurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Step 2: Get your token
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).background(Primary, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Text("2", color = Background, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(if (isFa) "توکنت را بگیر" else "Get your token",
                            fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(if (isFa) "دکمه را بزن — یک صفحه Cloudflare باز می‌شود که از قبل پر شده."
                        else "Tap the button — a Cloudflare page opens, already filled in.",
                        color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    NovaButton(if (isFa) "🔑 گرفتن توکن" else "🔑 Get my token",
                        onClick = {
                            val url = "https://dash.cloudflare.com/profile/api-tokens?permissionGroupKeys=%5B%7B%22key%22%3A%22workers_scripts%22%2C%22type%22%3A%22edit%22%7D%2C%7B%22key%22%3A%22workers_kv_storage%22%2C%22type%22%3A%22edit%22%7D%2C%7B%22key%22%3A%22d1%22%2C%22type%22%3A%22edit%22%7D%2C%7B%22key%22%3A%22account_settings%22%2C%22type%22%3A%22read%22%7D%5D&accountId=*&zoneId=all&name=Nova%20Installer"
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Column(Modifier.padding(start = 4.dp)) {
                        listOf(
                            if (isFa) "در آن صفحه، <b>تا ته پایین</b> برو ← <b>Continue to summary</b> را بزن." else "On that page, scroll <b>all the way down</b> → tap <b>Continue to summary</b>.",
                            if (isFa) "<b>Create Token</b> را بزن، بعد دکمه <b>Copy</b> را بزن تا کد بلند کپی شود." else "Tap <b>Create Token</b>, then tap the <b>Copy</b> button to copy the long code."
                        ).forEachIndexed { i, txt ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(Modifier.size(20.dp).background(SurfaceVariant, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                    Text(if (isFa) listOf("الف", "ب")[i] else listOf("a", "b")[i], color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(txt, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = WarningBg)) {
                        Text(if (isFa) "⚠️ کل کد را کپی کن قبل از اینکه از آن صفحه بیرون روی — فقط یک بار نشان داده می‌شود."
                            else "⚠️ Copy the whole code before leaving that page — it's shown only once.",
                            color = WarningText, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Step 3: Paste token
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).background(Primary, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Text("3", color = Background, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(if (isFa) "اینجا بچسبان" else "Paste it here",
                            fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(if (isFa) "دکمه چسباندن را بزن تا توکن کپی شده در کادر بیفتد."
                        else "Tap Paste to drop your copied token into the box.",
                        color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = token, onValueChange = { token = it },
                            placeholder = { Text("Cloudflare API Token", color = TextSecondary) },
                            modifier = Modifier.weight(1f).heightIn(min = 50.dp), singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor,
                                cursorColor = Primary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                        Spacer(Modifier.width(8.dp))
                        NovaButton(if (isFa) "چسباندن" else "Paste",
                            onClick = {
                                val clip = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                if (clip.hasPrimaryClip()) { token = clip.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: token }
                            }, modifier = Modifier.widthIn(min = 80.dp), color = SurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Step 4: Deploy
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).background(Primary, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Text("4", color = Background, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(if (isFa) "نوای خودت را بساز" else "Build your Nova",
                            fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(if (isFa) "دکمه را بزن و صبر کن — همه چیز را برایت آماده می‌کند (حدود ۳۰ ثانیه)."
                        else "Tap the button and wait — it sets up everything for you (about 30 seconds).",
                        color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    NovaButton(if (isFa) "پنل نوای من را بساز" else "🚀 Create my Nova panel",
                        onClick = {
                            scope.launch {
                                showResult = true
                                lastResult = viewModel.deployWorker(token.trim(), isDemo = false)
                            }
                        },
                        enabled = token.isNotBlank() && !isDeploying,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Progress / Result section
        if (isDeploying || showResult) {
            Spacer(Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.padding(16.dp)) {
                    if (isDeploying) {
                        deploySteps.forEach { step ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                val dotColor = when (step.state) {
                                    "done" -> Primary
                                    "error" -> Error
                                    "running" -> Primary
                                    else -> BorderColor
                                }
                                val dotContent = when (step.state) {
                                    "done" -> "✓"
                                    "error" -> "!"
                                    "running" -> "⟳"
                                    else -> ""
                                }
                                Box(Modifier.size(20.dp).background(if (step.state == "running") dotColor.copy(alpha = 0.2f) else dotColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center) {
                                    if (dotContent.isNotEmpty()) Text(dotContent, color = if (step.state == "done") Background else dotColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(if (isFa) step.labelFa else step.labelEn,
                                    color = if (step.state == "error") Error else TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }

                    if (workerUrl.isNotBlank() && lastResult != null) {
                        Spacer(Modifier.height(12.dp))
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))) {
                            Column(Modifier.padding(16.dp)) {
                                Text(if (isFa) "🎉 نوای تو آماده است!" else "🎉 Your Nova is ready!",
                                    color = Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (isFa) "آدرس: " else "Address: ", color = TextSecondary, fontSize = 13.sp)
                                    Text(workerUrl, color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(if (isFa) "اول، رمز ادمینت را در صفحه /install تنظیم کن:"
                                    else "First, set your admin password at /install:",
                                    color = TextSecondary, fontSize = 13.sp)
                                Spacer(Modifier.height(8.dp))
                                NovaButton(if (isFa) "باز کردن پنل" else "Open your panel",
                                    onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$workerUrl/install"))) },
                                    modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(6.dp))
                                NovaButton(if (isFa) "📋 کپی آدرس" else "📋 Copy address",
                                    onClick = {
                                        val clip = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        clip.setPrimaryClip(android.content.ClipData.newPlainText("NovaPanel", workerUrl))
                                    }, modifier = Modifier.fillMaxWidth(), color = SurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = WarningBg)) {
                                    Text(if (isFa) "🇮🇷 workers.dev در ایران فیلتر است — در Cloudflare یک دامنه اختصاصی اضافه کن (Workers ← ورکر تو ← Settings ← Domains & Routes) و از آن استفاده کن."
                                        else "🇮🇷 workers.dev is filtered in Iran — add a Custom Domain in Cloudflare (Workers → your worker → Settings → Domains & Routes) and use that instead.",
                                        color = WarningText, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                                }
                                Spacer(Modifier.height(10.dp))
                                NovaButton(if (isFa) "بازگشت" else "Back",
                                    onClick = { showResult = false; lastResult = null },
                                    modifier = Modifier.fillMaxWidth(), color = SurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(if (isFa) "توکن تو فقط در این دستگاه برای حرف زدن با Cloudflare استفاده می‌شود. هیچ چیزی سمت ما ذخیره نمی‌شود."
            else "Your token is used only on this device to talk to Cloudflare. Nothing is stored on our side.",
            color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

private val WarningBg = Color(0x1AF59E0B)
private val WarningText = Color(0xFFF59E0B)
